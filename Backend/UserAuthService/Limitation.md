Limitations of the Current Design

Critical Bugs First

1. Login rate limiting is wired up but never triggered

AuthController.login() catches TooManyRequestException, but AuthService.loginService() never throws it. There is no rate limiting on password guessing at all.
An attacker can make unlimited login attempts against any email address.

// AuthController.java:105 — this catch block is dead code
catch(TooManyRequestException e){ ... }

2. lastName is collected but silently discarded

RegisterUserDto has a lastName field. The User entity has no lastName column. The value is taken from the user at registration and thrown away without warning.

3. Hardcoded Redis TTL in RefreshToken is out of sync with AppProperties

// RefreshToken.java:12 — hardcoded to 10 days
private static final long REFRESH_TOKEN_TTL_SECONDS = 864000;

AppProperties binds app.auth.refresh-token-expiration-msec (also 10 days). These two values are maintained independently. Change one property without updating
the constant and the JWT expiry and Redis TTL will diverge — the token will be valid JWT-wise but either already evicted or lingering in Redis.

4. OTP is stored as plain text in Redis

stringRedisTemplate.opsForValue().set("OTP : " + email, otp, 120, TimeUnit.SECONDS);

Anyone with read access to Redis can see every pending OTP. It should be stored as a hash (e.g., BCrypt or SHA-256).

  ---
Security Gaps

5. No refresh token rotation

When a refresh token is used to get a new access token, it is not replaced. The same refresh token works until it expires in 10 days. If it is stolen (e.g.,   
via a network log or a compromised device), the attacker can silently generate new access tokens for the full 10-day window with no signal to the real user.

Industry practice (used by Google, Stripe, Auth0): issue a new refresh token on every use and invalidate the old one. A second use of an already-rotated token
should trigger a full session revocation as a theft signal.

6. Logout does not fully invalidate a session

Logout deletes the refresh token from Redis. But any access token issued before logout remains valid for up to 5 minutes. For a financial application this     
matters — a stolen access token grants full API access after the user has explicitly logged out.

7. No account lockout after repeated failed password attempts

As noted in point 1, there is no brute-force protection on the login endpoint. A real-world auth service for a financial app should lock an account (or require
CAPTCHA/OTP) after 5–10 consecutive wrong passwords.

8. Password has no complexity requirement

// RegisterUserDto.java:16
@Size(min = 8, message = "Password must be at least 8 characters")

Only a minimum length of 8 characters is enforced. password and 12345678 both pass validation. A financial app should require at least one digit, one uppercase
letter, and one special character.

9. OAuth2 failure handler leaks exception messages to the browser

// OAuth2FailureHandler.java:33
.queryParam("error", exception.getMessage())

Internal Spring Security exception messages (including stack details in some cases) are appended to the redirect URL as a query parameter. This is visible in  
browser history, server logs, and referrer headers on the destination page.

10. OTP verification required for Google/GitHub OAuth2 users

Google and GitHub already verify email ownership before issuing an OAuth2 token. Requiring a second OTP for first-time OAuth2 sign-ups adds friction without   
meaningful security benefit for those providers. Facebook is an exception where email verification cannot be fully trusted, but it should not apply uniformly.

  ---
Scalability & Operational Limitations

11. OTP email is sent via a personal Gmail account

# application-dev.properties
spring.mail.username=vp14.dev@gmail.com

Gmail's SMTP limit is 500 emails/day for a free account. At even modest scale (hundreds of signups or password resets per day), this hits the ceiling. A       
transactional email provider (SendGrid, AWS SES, Postmark) is the standard for production — they provide delivery receipts, bounce handling, and throughput    
measured in millions per day.

12. OTP email body is a plain string

emailService.sendEmail(email, "OTP Verification", "Your OTP is: " + otp);

No HTML template, no branding, no expiry notice, no "if you didn't request this" footer. This looks like a phishing email, not a product email. Users are      
conditioned to distrust plain-text OTP emails from unknown senders.

13. MAX_DEVICES = 3 is hardcoded and not configurable

// RefreshToken.java:11
private static final int MAX_DEVICES = 3;

This cannot be changed without a code deployment. It should be a configurable property.

14. No database migration tool

There is no Flyway or Liquibase. In prod, ddl-auto=validate means any schema change requires manually running SQL before deploying a new JAR. There is no      
version history, no rollback path, and no audit trail of what changed when. This is the most common cause of production outages on teams that do not use       
migration tooling.

15. UserTokens Redis entity exists but is dead code

// model/UserTokens.java — @RedisHash entity, never written to or read from

The actual token storage uses plain StringRedisTemplate string keys. This class appears to be a design artifact that was never wired up. It adds confusion for
anyone reading the data model.

  ---
Missing Features for Real Users

┌──────────────────────────────┬──────────────────────────────────────────────────────────────────────────┐
│           Feature            │                                  Impact                                  │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ Forgot password / reset flow │ Locked-out users have no self-service recovery path                      │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ Email change                 │ Users cannot update their login email                                    │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ Account deletion             │ Non-compliant with GDPR Article 17 (right to erasure)                    │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ Audit log                    │ No record of logins, token use, or failed attempts per user              │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ Suspicious login alerts      │ No email when login occurs from a new device or location                 │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ MFA beyond email OTP         │ No TOTP (Google Authenticator), no SMS fallback                          │
├──────────────────────────────┼──────────────────────────────────────────────────────────────────────────┤
│ "Remember this device"       │ Refresh token TTL is fixed at 10 days with no opt-in for longer sessions │
└──────────────────────────────┴──────────────────────────────────────────────────────────────────────────┘

  ---
API Design Issues

16. No API versioning

All paths use /userauth/api/auth/... with no version segment. Once clients are in production, breaking changes cannot be deployed without coordinating every   
consumer simultaneously. Standard practice is /userauth/api/v1/auth/....

17. GET /auth/validate and POST /auth/getNewAccessToken overlap

Both refresh an expired access token using a refresh token. The GET variant does it via cookies; the POST variant takes a body. For browser clients there is no
reason to use POST /getNewAccessToken. The two endpoints should be consolidated into one with clear documentation on which callers use which path.

18. Response DTO shape is inconsistent

Successful login and register return AuthResponse (with status, message). Successful validate returns ValidationResponseDto (with valid, message, userId,      
userEmail). getNewAccessToken returns AccessTokenResponse. Clients must handle three different shapes for what is conceptually one category of response — "here
is a token." A unified envelope would reduce client complexity.

  ---
Summary Verdict

The service is well-structured for a personal project or MVP — the core flows work, JWT handling is correct, multi-device capping is a nice touch, and the use
of separate access/refresh secrets is good. But for real users in a financial application, the blocking issues are:

1. No brute-force protection on login (the existing handler is wired to dead code)
2. No refresh token rotation (stolen tokens cannot be detected or revoked)
3. No password reset — locked-out users simply cannot access the system
4. Personal Gmail as the email transport — will hit a hard limit at any meaningful scale
5. No database migration tooling — every schema change is a manual production risk

These are not cosmetic — items 1, 2, and 3 directly affect user security and trust.

