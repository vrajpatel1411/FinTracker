# UserAuthService — Technical & Product Documentation

| Field            | Value                                      |
|------------------|--------------------------------------------|
| **Service Name** | UserAuthService                            |
| **Version**      | 0.0.1                                      |
| **Status**       | Active                                     |
| **Owner**        | Vraj Patel                                 |
| **Stack**        | Java 21 · Spring Boot 4 · PostgreSQL · Redis |
| **Last Updated** | April 2026                                 |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Service Overview](#2-service-overview)
3. [Architecture Overview](#3-architecture-overview)
4. [Authentication Flows](#4-authentication-flows)
   - 4.1 [Username & Password Registration](#41-username--password-registration)
   - 4.2 [Username & Password Login](#42-username--password-login)
   - 4.3 [Email OTP Verification](#43-email-otp-verification)
   - 4.4 [OAuth2 Social Login](#44-oauth2-social-login)
   - 4.5 [Token Refresh](#45-token-refresh)
   - 4.6 [Logout](#46-logout)
5. [API Reference](#5-api-reference)
6. [Security Model](#6-security-model)
7. [Data Model](#7-data-model)
8. [Configuration Reference](#8-configuration-reference)
9. [Deployment Guide](#9-deployment-guide)
10. [Observability & Monitoring](#10-observability--monitoring)
11. [Error Codes & Troubleshooting](#11-error-codes--troubleshooting)
12. [Rate Limiting](#12-rate-limiting)
13. [Glossary](#13-glossary)

---

## 1. Executive Summary

> **For non-technical readers:** Think of UserAuthService as the front door of the FinTracker application. Every user who wants to access FinTracker must pass through this service. It checks who they are (authentication), issues them a digital key (token), and keeps that key secure. It also supports signing in via Google, GitHub, and Facebook so users don't need to create a new password.

UserAuthService is a standalone microservice responsible for all identity and authentication concerns within the FinTracker platform. It handles:

- New user registration with email verification
- Secure login via username/password and social OAuth2 providers (Google, GitHub, Facebook)
- Issuing, validating, and rotating short-lived access tokens and long-lived refresh tokens
- Email OTP verification for new accounts and unverified logins
- Token validation on behalf of other internal services (e.g., the API Gateway)

The service is stateless — it holds no session state in memory. All state is stored in PostgreSQL (user records) and Redis (tokens, OTPs, rate-limit counters).

---

## 2. Service Overview

### 2.1 Responsibilities

| Responsibility              | Description                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| User registration           | Create a new user account and trigger email OTP verification                |
| User login                  | Verify credentials, issue tokens, handle unverified email gracefully        |
| OAuth2 social login         | Delegate authentication to Google, GitHub, or Facebook                      |
| Email OTP verification      | Confirm user email ownership before granting full access                    |
| Token issuance              | Generate signed JWT access tokens (5 min) and refresh tokens (10 days)     |
| Token validation            | Validate tokens for the API Gateway and browser clients                     |
| Token refresh               | Issue a new access token when the old one expires, using a refresh token    |
| Logout                      | Invalidate the user's refresh token in Redis                                |
| Multi-device session cap    | Enforce a maximum of 3 concurrent sessions per user                         |

### 2.2 Out of Scope

This service does **not** handle:

- Authorization (what a user can do) — that is each downstream service's responsibility
- Password reset flows
- User profile updates beyond what is set at registration/OAuth2 callback
- Billing, subscription, or financial data

### 2.3 Upstream & Downstream Dependencies

```
Browser / Mobile Client
        │
        ▼
   API Gateway  ──────────── calls POST /userauth/api/auth/validate
        │                    to verify tokens on every request
        ▼
 UserAuthService
        │
        ├──► PostgreSQL  (user records)
        ├──► Redis       (refresh tokens, OTPs, rate-limit counters)
        └──► SMTP/Gmail  (OTP emails)
```

---

## 3. Architecture Overview

### 3.1 Runtime

| Property        | Value                                         |
|-----------------|-----------------------------------------------|
| Port (dev)      | `8084`                                        |
| Port (Docker)   | `8080`                                        |
| Spring Profile  | `dev` (default) · `prod` · `test`             |
| Session         | Stateless (`SessionCreationPolicy.STATELESS`) |
| API Docs (dev)  | `/user/api/swagger-ui-custom.html`            |

### 3.2 Internal Request Pipeline

Every HTTP request to UserAuthService passes through the following pipeline:

```
Incoming Request
      │
      ▼
 CORS Filter  ──── rejects cross-origin requests from unauthorized domains
      │
      ▼
 TokenAuthenticationFilter  ──── reads accessToken cookie
      │                          skips public endpoints (/userauth/api/auth/**, /oauth2/**)
      │                          validates JWT → populates SecurityContext
      ▼
 Spring Security Filter Chain
      │
      ▼
 Controller  ──► Service  ──► Repository / Redis
      │
      ▼
 Response (JSON + Set-Cookie headers)
```

### 3.3 Package Structure

```
org.vrajpatel.userauthservice
├── config/              Spring Security, password encoder
├── controller/          HTTP layer (AuthController, UserController)
├── service/             Business logic (AuthService, UserService)
├── model/               JPA entities (User), Redis hash (UserTokens)
├── Repository/          Spring Data JPA interfaces
├── requestDTO/          Inbound request bodies
├── ResponseDTO/         Outbound response bodies
├── Exception/           Typed exceptions + GlobalExceptionHandler
└── utils/
    ├── JwtUtils/        Token generation, validation, auth filter
    ├── OTPService/      OTP generation, email dispatch
    ├── Oauth2Handler/   OAuth2 success/failure callbacks
    ├── Oauth2UserInfo/  Per-provider user-info adapters
    └── config/          AppProperties (binds app.auth.* and app.oauth2.*)
```

---

## 4. Authentication Flows

### 4.1 Username & Password Registration

> **Plain language:** A user fills in their name, email, and password. The service saves the account, then sends a one-time code to their email. They must enter that code before they can log in.

```
Client                      UserAuthService              PostgreSQL        Redis            Email
  │                               │                          │               │                │
  │  POST /register               │                          │               │                │
  │  { email, password,           │                          │               │                │
  │    firstName }                │                          │               │                │
  │──────────────────────────────►│                          │               │                │
  │                               │  existsByEmail?          │               │                │
  │                               │─────────────────────────►│               │                │
  │                               │  false                   │               │                │
  │                               │◄─────────────────────────│               │                │
  │                               │  save(user)              │               │                │
  │                               │─────────────────────────►│               │                │
  │                               │  generateOTP()           │               │                │
  │                               │  SET "OTP : email" → otp │               │                │
  │                               │  (TTL 120s)              │               │                │
  │                               │────────────────────────────────────────►│                │
  │                               │  sendEmail(otp)          │               │                │
  │                               │─────────────────────────────────────────────────────────►│
  │  200 OK                       │                          │               │                │
  │  { status: false,             │                          │               │                │
  │    needEmailVerification: true│                          │               │                │
  │    email: "user@example.com" }│                          │               │                │
  │◄──────────────────────────────│                          │               │                │
```

**Request**
```http
POST /userauth/api/auth/register
Content-Type: application/json

{
  "firstName": "Jane",
  "email": "jane.doe@example.com",
  "password": "SecurePass123!"
}
```

**Successful Response** `200 OK`
```json
{
  "status": false,
  "needEmailVerification": true,
  "email": "jane.doe@example.com",
  "message": "Email needs to be verified"
}
```

> Note: `status: false` here does not mean an error — it signals that the next step (OTP verification) is required before tokens are issued.

---

### 4.2 Username & Password Login

> **Plain language:** A registered user enters their email and password. If the email is already verified, they are immediately issued digital keys (tokens) stored as cookies. If not, a one-time code is sent to their email first.

```
Client                      UserAuthService              PostgreSQL        Redis
  │                               │                          │               │
  │  POST /login                  │                          │               │
  │  { email, password }          │                          │               │
  │──────────────────────────────►│                          │               │
  │                               │  findByEmail()           │               │
  │                               │─────────────────────────►│               │
  │                               │  check rateLimit (Redis) │               │
  │                               │────────────────────────────────────────►│
  │                               │  passwordEncoder.matches()               │
  │                               │                          │               │
  │                               │──── email verified? ─────│               │
  │                               │                          │               │
  │         ┌─────────────────────┤                          │               │
  │         │ YES                 │ NO → trigger OTP flow    │               │
  │         ▼                     │                          │               │
  │  200 OK + Set-Cookie          │                          │               │
  │  accessToken (5 min)          │                          │               │
  │  refreshToken (10 days)       │                          │               │
  │◄──────────────────────────────│                          │               │
```

**Request**
```http
POST /userauth/api/auth/login
Content-Type: application/json

{
  "email": "jane.doe@example.com",
  "password": "SecurePass123!"
}
```

**Successful Response** `200 OK`
```json
{
  "status": true,
  "message": "Successfully logged in"
}
```
```
Set-Cookie: accessToken=eyJhbGciOiJIUzUxMiJ9...; Max-Age=300; Path=/; Domain=localhost; HttpOnly; Secure; SameSite=None
Set-Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9...; Max-Age=864000; Path=/; Domain=localhost; HttpOnly; Secure; SameSite=None
```

**Unverified Email Response** `200 OK`
```json
{
  "status": false,
  "needEmailVerification": true,
  "email": "jane.doe@example.com",
  "message": "Email needs to be verified"
}
```

---

### 4.3 Email OTP Verification

> **Plain language:** The user receives a 6-digit code by email. They submit it to the service. On success, they are logged in and receive their session cookies. Wrong codes are tracked — after 5 wrong attempts the OTP is invalidated and they must request a new one.

**Verify OTP**

```http
POST /userauth/api/auth/verifyOtp
Content-Type: application/json

{
  "userEmail": "jane.doe@example.com",
  "otp": "847291"
}
```

**Success** `200 OK`
```json
{
  "status": true,
  "message": "Successfully logged in"
}
```
Cookies are set identically to a successful login.

**Resend OTP**

```http
POST /userauth/api/auth/resendOtp
Content-Type: application/json

{
  "email": "jane.doe@example.com"
}
```

**Success** `200 OK`
```json
{
  "status": true,
  "email": "jane.doe@example.com",
  "message": "Resend OTP successfully"
}
```

**OTP Lifecycle**

| Event                          | Redis Key                     | TTL     | Limit           |
|--------------------------------|-------------------------------|---------|-----------------|
| OTP generated/stored           | `OTP : {email}`               | 120 s   | —               |
| Wrong attempt counter          | `OTPAttempt : {email}`        | 120 s   | 5 attempts max  |
| Resend attempt counter         | `resendOTPCount : {email}`    | 600 s   | 5 resends max   |

After 5 failed OTP attempts, both the attempt counter key and the OTP key are deleted from Redis. The user must request a fresh OTP.

---

### 4.4 OAuth2 Social Login

> **Plain language:** The user clicks "Sign in with Google" (or GitHub/Facebook). They are redirected to that provider's login page. When they return, the service either logs them in directly (if they have logged in before) or sends an OTP for first-time email verification.

**Supported Providers**

| Provider | Registration ID |
|----------|----------------|
| Google   | `google`       |
| GitHub   | `github`       |
| Facebook | `facebook`     |

**Step 1 — Initiate**

The client redirects the browser to:
```
GET /oauth2/authorize/{provider}?redirect_uri=https://app.example.com/oauth2/redirects
```

Example for Google:
```
GET /oauth2/authorize/google?redirect_uri=https://localhost:5173/oauth2/redirects
```

The `redirect_uri` is stored in a short-lived cookie (`oauth2_redirect_uri`) and validated against the whitelist in `app.oauth2.authorized-redirect-uris` before use.

**Step 2 — Provider Callback**

The provider redirects to `/oauth2/callback/{provider}`. `CustomUserService` then:
1. Fetches user info from the provider.
2. Looks up the email in PostgreSQL.
   - **New user** → creates a record with `authProvider = google` (or `github`/`facebook`), marks email as **unverified**.
   - **Returning user** → updates `firstName` and `photoUrl` from latest provider data.
3. Provider mismatch (e.g., account exists with `github` but user tries `google`) → throws `AuthenticationOauth2ProcessingException`.

**Step 3 — Success Redirect**

`Oauth2SuccessHandler` inspects `emailVerified`:

- **Verified** → sets `accessToken` + `refreshToken` cookies, redirects to:
  ```
  https://app.example.com/oauth2/redirects?status=true
  ```
- **Unverified** → sends OTP to email, redirects to:
  ```
  https://app.example.com/oauth2/redirects?status=false&email=jane.doe@example.com
  ```
  Client uses the `email` query param to drive the OTP entry UI.

---

### 4.5 Token Refresh

There are two ways to refresh tokens depending on the caller.

#### Browser clients — Cookie-based (GET)

> **Plain language:** When a browser makes a request and the access token has expired, the service automatically checks the refresh token cookie and silently issues a new access token without requiring the user to log in again.

```http
GET /userauth/api/auth/validate
Cookie: accessToken=<expired>; refreshToken=<valid>
```

If `accessToken` is expired:
1. The refresh token is validated against Redis.
2. A new access token is generated.
3. A new `accessToken` cookie is set in the response.

**Success** `200 OK`
```json
{
  "valid": true,
  "message": "Successfully logged in"
}
```
```
Set-Cookie: accessToken=eyJhbGci...; Max-Age=300; ...
```

#### API Gateway — Body-based (POST)

The API Gateway calls this endpoint to verify a token passed explicitly in the request body:

```http
POST /userauth/api/auth/validate
Content-Type: application/json

{
  "jwt": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Success** `200 OK`
```json
{
  "valid": true,
  "message": "Token is valid",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "userEmail": "jane.doe@example.com"
}
```

---

### 4.6 Logout

> **Plain language:** When a user logs out, their refresh token is deleted from the system. Even if someone stole that refresh token, it can no longer be used to generate new access tokens.

```http
POST /userauth/api/auth/logout
Cookie: refreshToken=eyJhbGci...
```

**Response** `200 OK`
```json
{
  "status": true,
  "message": "Logged out successfully"
}
```

Both `accessToken` and `refreshToken` cookies are cleared (set to empty with `Max-Age=0`). The refresh token is removed from Redis.

---

## 5. API Reference

**Base Path:** `/userauth/api`

All request bodies are `application/json`. All responses are `application/json`.

### 5.1 Endpoint Summary

| Method | Path                          | Auth Required | Description                            |
|--------|-------------------------------|:-------------:|----------------------------------------|
| POST   | `/auth/register`              | No            | Register a new user                    |
| POST   | `/auth/login`                 | No            | Login with email + password            |
| POST   | `/auth/verifyOtp`             | No            | Verify email OTP                       |
| POST   | `/auth/resendOtp`             | No            | Resend OTP to email                    |
| GET    | `/auth/validate`              | No (cookie)   | Validate/refresh token via cookies     |
| POST   | `/auth/validate`              | No            | Validate JWT (API Gateway use)         |
| POST   | `/auth/getNewAccessToken`     | No            | Exchange refresh token for access token|
| POST   | `/auth/logout`                | No (cookie)   | Logout and clear tokens                |
| GET    | `/user/`                      | Yes (cookie)  | Get current authenticated user profile |

---

### 5.2 Detailed Endpoint Reference

#### `POST /auth/register`

Registers a new user. The user is **not** logged in immediately — an OTP is sent to verify the email first.

**Request Body**

| Field       | Type   | Required | Constraints                       |
|-------------|--------|:--------:|-----------------------------------|
| `firstName` | string | Yes      | Non-empty                         |
| `email`     | string | Yes      | Valid email format                |
| `password`  | string | Yes      | Non-empty                         |

**Example Request**
```json
{
  "firstName": "Jane",
  "email": "jane.doe@example.com",
  "password": "SecurePass123!"
}
```

**Responses**

| Status | Condition             | Body                                                                                  |
|--------|-----------------------|---------------------------------------------------------------------------------------|
| `200`  | Registered, needs OTP | `{ "status": false, "needEmailVerification": true, "email": "...", "message": "..." }` |
| `400`  | Validation failure    | `{ "status": false, "message": "<field error>" }`                                     |
| `409`  | Email already exists  | `{ "status": false, "message": "Email Is Already Registered" }`                       |
| `500`  | OTP delivery failure  | `{ "status": false, "message": "Issue sending OTP" }`                                 |

---

#### `POST /auth/login`

Authenticates an existing user.

**Request Body**

| Field      | Type   | Required |
|------------|--------|:--------:|
| `email`    | string | Yes      |
| `password` | string | Yes      |

**Example Request**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePass123!"
}
```

**Responses**

| Status | Condition                  | Cookies Set         | Body                                                 |
|--------|----------------------------|---------------------|------------------------------------------------------|
| `200`  | Login success              | `accessToken`, `refreshToken` | `{ "status": true, "message": "Successfully logged in" }` |
| `200`  | Email not verified         | None                | `{ "status": false, "needEmailVerification": true, "email": "..." }` |
| `401`  | Wrong password             | None                | `{ "status": false, "message": "Unauthorized, Wrong Password" }` |
| `404`  | User not found             | None                | `{ "status": false, "message": "User not found" }` |
| `429`  | Too many login attempts    | None                | `{ "status": false, "message": "Too many requests" }` |
| `500`  | Internal error             | None                | `{ "status": false, "message": "Internal Server Error" }` |

---

#### `POST /auth/verifyOtp`

Verifies the OTP sent to the user's email and completes login.

**Request Body**

| Field       | Type   | Required |
|-------------|--------|:--------:|
| `userEmail` | string | Yes      |
| `otp`       | string | Yes      |

**Example Request**
```json
{
  "userEmail": "jane.doe@example.com",
  "otp": "847291"
}
```

**Responses**

| Status | Condition              | Cookies Set                    | Body                                                |
|--------|------------------------|--------------------------------|-----------------------------------------------------|
| `200`  | OTP correct            | `accessToken`, `refreshToken`  | `{ "status": true, "message": "Successfully logged in" }` |
| `400`  | Invalid OTP            | None                           | `{ "status": false, "message": "Invalid OTP." }` |
| `400`  | OTP expired            | None                           | `{ "status": false, "message": "OTP has expired. Please request a new one." }` |
| `429`  | Too many wrong attempts| None                           | `{ "status": false, "message": "Too many attempts, Request New OTP" }` |

---

#### `POST /auth/resendOtp`

Resends an OTP to the provided email. Maximum 5 resend attempts per 10-minute window.

**Request Body**

| Field   | Type   | Required |
|---------|--------|:--------:|
| `email` | string | Yes      |

**Responses**

| Status | Condition              | Body                                                              |
|--------|------------------------|-------------------------------------------------------------------|
| `200`  | OTP sent               | `{ "status": true, "email": "...", "message": "Resend OTP successfully" }` |
| `400`  | Delivery failed        | `{ "status": false, "message": "Failed to send OTP" }` |
| `429`  | Rate limit exceeded    | `{ "status": false, "message": "Too many resend attempts. Try again later." }` |

---

#### `GET /auth/validate`

Validates the access token from cookies. If expired, silently refreshes it using the refresh token cookie.

**Cookie Parameters**

| Cookie         | Required |
|----------------|:--------:|
| `accessToken`  | No       |
| `refreshToken` | No       |

**Responses**

| Status | Condition                           | Body                                                      |
|--------|-------------------------------------|-----------------------------------------------------------|
| `200`  | Access token valid                  | `{ "valid": true, "message": "Successfully logged in" }` |
| `200`  | Refreshed via refresh token         | `{ "valid": true, "message": "Successfully logged in" }` + new `accessToken` cookie |
| `401`  | Both tokens absent or invalid       | `{ "valid": false, "message": "Unauthorized access" }` |
| `404`  | User not found during refresh       | `{ "valid": false, "message": "User not found" }` |

---

#### `POST /auth/validate`

Validates a JWT passed explicitly in the request body. Used by the API Gateway to verify user identity.

**Request Body**

| Field | Type   | Required |
|-------|--------|:--------:|
| `jwt` | string | Yes      |

**Example Request**
```json
{
  "jwt": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBlODQwMC4uLiJ9.signature"
}
```

**Responses**

| Status | Condition       | Body                                                                                   |
|--------|-----------------|----------------------------------------------------------------------------------------|
| `200`  | Token valid     | `{ "valid": true, "message": "Token is valid", "userId": "uuid", "userEmail": "..." }` |
| `401`  | Token invalid   | `{ "valid": false, "message": "Invalid access token" }` |

---

#### `POST /auth/getNewAccessToken`

Exchanges a valid refresh token for a new access token. For non-browser clients.

**Request Body**

| Field | Type   | Required |
|-------|--------|:--------:|
| `jwt` | string | Yes      | ← Pass the refresh token here

**Responses**

| Status | Condition             | Body                                                                              |
|--------|-----------------------|-----------------------------------------------------------------------------------|
| `200`  | New token issued      | `{ "accessToken": "eyJ...", "userId": "uuid", "userEmail": "jane@..." }` |
| `401`  | Invalid refresh token | `{ "status": false, "message": "Not Authorized..." }` |
| `404`  | User not found        | `{ "status": false, "message": "User Not Found" }` |

---

#### `POST /auth/logout`

Invalidates the user's refresh token. The browser's cookies are also cleared.

**Cookie Parameters**

| Cookie          | Required |
|-----------------|:--------:|
| `refreshToken`  | No       |

**Response** `200 OK`
```json
{
  "status": true,
  "message": "Logged out successfully"
}
```
Both `accessToken` and `refreshToken` cookies are expired in the response headers.

---

#### `GET /user/`

Returns the authenticated user's profile. Requires a valid `accessToken` cookie.

**Response** `200 OK`
```json
{
  "status": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Jane",
  "email": "jane.doe@example.com",
  "photoUrl": "https://lh3.googleusercontent.com/a/example",
  "authProvider": "google"
}
```

| Status | Condition              |
|--------|------------------------|
| `200`  | User found             |
| `401`  | No valid access token  |
| `404`  | User record not found  |

---

## 6. Security Model

### 6.1 Token Design

| Token          | Algorithm  | Expiry   | Transport       | Storage (server-side)     |
|----------------|------------|----------|-----------------|---------------------------|
| Access Token   | HMAC-SHA   | 5 min    | `HttpOnly` cookie | None — stateless          |
| Refresh Token  | HMAC-SHA   | 10 days  | `HttpOnly` cookie | Redis (keyed by token)    |

Both tokens are signed JWTs. They carry the user's UUID (`sub` claim) and email. The access and refresh tokens are signed with **separate secrets** (`ACCESS_TOKEN_SECRET` / `REFRESH_TOKEN_SECRET`), so a compromised access token cannot be used to forge a refresh token.

**JWT Payload Example (Access Token)**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "jane.doe@example.com",
  "iat": 1712700000,
  "exp": 1712700300
}
```

### 6.2 Cookie Security

All cookies are set with:

| Attribute    | Value    | Purpose                                                             |
|--------------|----------|---------------------------------------------------------------------|
| `HttpOnly`   | true     | JavaScript cannot read the cookie — mitigates XSS token theft      |
| `Secure`     | true     | Cookie only sent over HTTPS                                         |
| `SameSite`   | `None`   | Required for cross-site requests (frontend on different subdomain)  |
| `Path`       | `/`      | Cookie sent on all requests to the domain                           |
| `Domain`     | Configurable | `localhost` (dev) · `vrajpatelproject.software` (prod)          |

> **Why SameSite=None?** The FinTracker frontend and backend run on different subdomains. `SameSite=None` combined with `Secure=true` is the standard way to share cookies cross-site safely (equivalent to how Google Analytics or Stripe Checkout cookies work in production).

### 6.3 Multi-Device Session Management

A user may have a maximum of **3 active sessions** concurrently. Redis tracks all active refresh tokens per user in a sorted set (`user_email : {email}`), ordered by creation time. When a 4th session is created, the oldest session's refresh token is automatically revoked from Redis.

```
user_email : jane@example.com → ZSet
  ├── refreshToken_A  (score: 1712600000)  ← oldest, will be evicted at 4th login
  ├── refreshToken_B  (score: 1712650000)
  └── refreshToken_C  (score: 1712700000)  ← newest
```

### 6.4 Password Storage

Passwords are stored using **BCrypt** hashing (Spring Security `PasswordEncoder`). Plain-text passwords are never stored or logged.

### 6.5 OAuth2 Provider Binding

Each user account is bound to exactly one authentication provider (`usernamepassword`, `google`, `github`, `facebook`). Attempting to log in with a different provider for the same email results in a `401` error with a message indicating the correct provider. This prevents account takeover via provider switching.

### 6.6 CORS Policy

Only one origin is permitted per environment:

| Environment | Allowed Origin                                 |
|-------------|------------------------------------------------|
| Development | `https://localhost:5173`                       |
| Production  | `https://fintracker.vrajpatelproject.software` |

Requests from any other origin are rejected at the CORS filter before reaching any controller.

---

## 7. Data Model

### 7.1 `users` Table (PostgreSQL)

| Column            | Type        | Constraints              | Description                                  |
|-------------------|-------------|--------------------------|----------------------------------------------|
| `userId`          | `UUID`      | PK, NOT NULL, UNIQUE     | Auto-generated UUID                          |
| `first_name`      | `VARCHAR`   | NOT NULL                 | User's first name                            |
| `avatar_url`      | `VARCHAR`   | Nullable                 | Profile photo URL (populated by OAuth2)      |
| `email`           | `VARCHAR`   | NOT NULL, UNIQUE         | User's email address (always lowercase)      |
| `password`        | `VARCHAR`   | Nullable                 | BCrypt hash; null for OAuth2 users           |
| `isemailverified` | `BOOLEAN`   |                          | True once OTP is verified                    |
| `authProvider`    | `VARCHAR`   |                          | `usernamepassword` · `google` · `github` · `facebook` |
| `providerId`      | `VARCHAR`   | Nullable                 | Provider's unique ID for the user            |
| `createdAt`       | `TIMESTAMP` | NOT NULL                 | UTC instant of account creation              |

### 7.2 Redis Key Schema

| Key Pattern                       | Value Type | TTL       | Purpose                                     |
|-----------------------------------|------------|-----------|---------------------------------------------|
| `refresh_token : {token}`         | String     | 10 days   | Maps refresh token → user email             |
| `user_email : {email}`            | Sorted Set | 10 days   | All active refresh tokens for a user        |
| `OTP : {email}`                   | String     | 120 s     | One-time password pending verification      |
| `OTPAttempt : {email}`            | String     | 120 s     | Wrong OTP attempt counter (max 5)           |
| `resendOTPCount : {email}`        | String     | 600 s     | OTP resend request counter (max 5)          |

---

## 8. Configuration Reference

### 8.1 Application Properties

The service uses Spring Boot's profile system. `application.properties` contains common settings; `application-dev.properties` and `application-prod.properties` contain environment-specific overrides.

#### Common (`application.properties`)

| Property                              | Default      | Description                           |
|---------------------------------------|--------------|---------------------------------------|
| `app.auth.access-token-secret`        | *(env var)*  | HMAC secret for access tokens         |
| `app.auth.access-token-expiration-msec` | `300000`   | Access token TTL: 5 minutes           |
| `app.auth.refresh-token-secret`       | *(env var)*  | HMAC secret for refresh tokens        |
| `app.auth.refresh-token-expiration-msec` | `864000000` | Refresh token TTL: 10 days          |
| `springdoc.api-docs.path`             | `/user/api/api-docs` | OpenAPI spec path            |
| `springdoc.swagger-ui.path`           | `/user/api/swagger-ui-custom.html` | Swagger UI  |

#### Dev (`application-dev.properties`)

| Property                    | Description                                         |
|-----------------------------|-----------------------------------------------------|
| `server.port`               | `8084`                                              |
| `spring.datasource.url`     | PostgreSQL JDBC URL (`${POSTGRES_DATABASE_URL}`)    |
| `spring.data.redis.host`    | Redis host (`${REDIS_HOST_URL}`)                    |
| `otel.exporter.otlp.endpoint` | Jaeger OTLP endpoint (`${JAEGER_HOST_URL}`)       |
| `authorizedUrl`             | `https://localhost:5173`                            |
| `domain`                    | `localhost`                                         |
| `app.oauth2.authorized-redirect-uris` | `https://localhost:5173/oauth2/redirects` |

#### Prod (`application-prod.properties`)

| Property                           | Description                                              |
|------------------------------------|----------------------------------------------------------|
| `spring.datasource.url`            | `${DB_URL}`                                              |
| `spring.datasource.hikari.maximum-pool-size` | `50`                                         |
| `server.tomcat.threads.max`        | `400`                                                    |
| `spring.jpa.hibernate.ddl-auto`    | `validate` — schema must match exactly, no auto-migration |
| `authorizedUrl`                    | `https://fintracker.vrajpatelproject.software`           |
| `domain`                           | `vrajpatelproject.software`                              |

### 8.2 Required Environment Variables

| Variable                 | Used In   | Description                                     |
|--------------------------|-----------|-------------------------------------------------|
| `ACCESS_TOKEN_SECRET`    | Both      | Min 32 chars. Signs access JWTs                 |
| `REFRESH_TOKEN_SECRET`   | Both      | Min 32 chars. Signs refresh JWTs                |
| `POSTGRES_DATABASE_URL`  | Dev       | Full JDBC URL                                   |
| `DB_URL`                 | Prod      | Full JDBC URL                                   |
| `DB_USERNAME`            | Prod      | DB username                                     |
| `DB_PASSWORD`            | Both      | DB password                                     |
| `REDIS_HOST_URL`         | Dev       | Redis hostname                                  |
| `REDIS_URL`              | Prod      | Redis hostname                                  |
| `REDIS_PASSWORD`         | Prod      | Redis password                                  |
| `MAIL_PASSWORD`          | Both      | Gmail app password for SMTP                     |
| `GOOGLE_CLIENT_SECRET`   | Both      | Google OAuth2 client secret                     |
| `GITHUB_CLIENT_SECRET`   | Both      | GitHub OAuth2 client secret                     |
| `FACEBOOK_CLIENT_SECRET` | Both      | Facebook OAuth2 client secret                   |
| `JAEGER_HOST_URL`        | Dev       | OpenTelemetry collector endpoint (OTLP/gRPC)    |

---

## 9. Deployment Guide

### 9.1 Local Development

**Prerequisites**
- Java 21
- Docker (for PostgreSQL and Redis)
- A `.env` file at the project root with required environment variables

**Start dependencies**
```bash
# PostgreSQL
docker run -d --name fintracker-pg \
  -e POSTGRES_DB=fintracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -p 5432:5432 postgres:16

# Redis
docker run -d --name fintracker-redis -p 6379:6379 redis:7
```

**Run the service**
```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8084`. Swagger UI is available at:
```
http://localhost:8084/user/api/swagger-ui-custom.html
```

### 9.2 Building a Production Image

```bash
# 1. Package the JAR
./mvnw clean package -DskipTests

# 2. Build the Docker image
docker build -t userauthservice:0.0.1 .
```

The `Dockerfile` uses `amazoncorretto:21-alpine3.20` as the base image. The container exposes port `8080` and starts with `spring.profiles.active=dev` by default — override this for production:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e ACCESS_TOKEN_SECRET=... \
  -e REFRESH_TOKEN_SECRET=... \
  --env-file prod.env \
  userauthservice:0.0.1
```

### 9.3 Kubernetes

Kubernetes manifests are in `k8s/`:

| File              | Purpose                                              |
|-------------------|------------------------------------------------------|
| `Deployment.yaml` | Pod spec, replica count, resource limits, probes     |
| `service.yaml`    | ClusterIP service definition                         |
| `secret.yaml`     | Kubernetes Secret for environment variables          |

Apply in order:
```bash
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/Deployment.yaml
```

### 9.4 Database Schema Management

- **Dev:** `spring.jpa.hibernate.ddl-auto=update` — Hibernate automatically applies schema changes.
- **Prod:** `spring.jpa.hibernate.ddl-auto=validate` — Hibernate only validates that the existing schema matches the entities. Schema migrations must be applied manually before deploying a new version.

---

## 10. Observability & Monitoring

### 10.1 Health Check

```
GET /actuator/health
```

Returns service health including database and Redis connectivity:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

Used by Kubernetes liveness and readiness probes.

### 10.2 Metrics (Prometheus)

```
GET /actuator/prometheus
```

Exposes JVM, HTTP request, and custom metrics in Prometheus format. Tagged with `application=userauthservice`.

Integrate with your Prometheus scrape config:
```yaml
- job_name: 'userauthservice'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['userauthservice:8084']
```

### 10.3 Distributed Tracing (OpenTelemetry + Jaeger)

Traces are exported via OTLP/gRPC to the configured Jaeger endpoint (`otel.exporter.otlp.endpoint`). All incoming requests are traced with `always_on` sampling. Trace context is propagated using the W3C `traceparent` / `tracestate` headers.

In dev, point `JAEGER_HOST_URL` to a local Jaeger instance:
```
docker run -p 16686:16686 -p 4317:4317 jaegertracing/all-in-one
```

View traces at `http://localhost:16686`.

---

## 11. Error Codes & Troubleshooting

### 11.1 HTTP Error Reference

| HTTP Status | Internal Cause                        | User-Facing Message                                      |
|-------------|---------------------------------------|----------------------------------------------------------|
| `400`       | `BadRequestException`                 | Context-specific (e.g., "Invalid JWT signature")         |
| `401`       | `UnAuthorizedException`               | "Unauthorized, Wrong Password" / "Unauthorized access"  |
| `401`       | `IllegalArgumentException` (OAuth2)   | "Try login with an Auth Provider"                        |
| `401`       | `AuthenticationOauth2ProcessingException` | "Looks like you're signed up with {provider}…"       |
| `404`       | `UserNotFound`                        | "User not found"                                         |
| `409`       | `UserExistException`                  | "Email Is Already Registered"                            |
| `429`       | `TooManyRequestException`             | "Too many resend attempts. Try again later."             |
| `429`       | `TooManyAttemptException`             | "Too many attempts, Request New OTP"                     |
| `500`       | `InternalServerError` / `RuntimeException` | "Something went wrong. Please try again later."     |

### 11.2 Common Issues

**Issue: OTP email not received**
- Check the `MAIL_PASSWORD` environment variable is set to a valid Gmail app password (not the account password).
- Ensure `spring.mail.properties.mail.smtp.starttls.enable=true` is set.
- Check logs for `Failed to send OTP to {email}`.

**Issue: `401 Unauthorized` on protected endpoints after login**
- The `accessToken` cookie may have expired (TTL is 5 minutes). The client should call `GET /auth/validate` to silently refresh it.
- Verify the cookie `Domain` matches the domain of the request. In dev, ensure requests go to `localhost`, not `127.0.0.1`.

**Issue: OAuth2 login returns provider mismatch error**
- The user previously registered with a different provider. The error message tells them which provider to use: `"Looks like you're signed up with github account. Please use your github account to login."`

**Issue: `429 Too Many Requests` on login**
- The login rate limiter has been triggered. The client should present a message to wait before retrying.

**Issue: Token validation fails immediately after generation**
- `ACCESS_TOKEN_SECRET` or `REFRESH_TOKEN_SECRET` may differ between service instances (e.g., rolling deployment with mismatched secrets). Ensure these values are consistent across all replicas.

---

## 12. Rate Limiting

Rate limiting is implemented in Redis without an external library.

| Action             | Redis Key                     | Window  | Limit | Consequence on Breach        |
|--------------------|-------------------------------|---------|-------|------------------------------|
| OTP resend         | `resendOTPCount : {email}`    | 10 min  | 5     | `429 Too Many Requests`      |
| OTP verification   | `OTPAttempt : {email}`        | 2 min   | 5     | OTP invalidated + `429`      |

> Login rate limiting is tracked as a `TooManyRequestException`. See `AuthService.loginService()` for implementation details.

---

## 13. Glossary

| Term                | Definition                                                                                                      |
|---------------------|-----------------------------------------------------------------------------------------------------------------|
| **Access Token**    | A short-lived JWT (5 min) that proves a user is authenticated. Sent as an `HttpOnly` cookie on every request.  |
| **Refresh Token**   | A long-lived JWT (10 days) used only to obtain a new access token when the current one expires.                 |
| **OTP**             | One-Time Password. A 6-digit numeric code emailed to a user to verify their email address.                     |
| **OAuth2**          | An open standard for delegating authentication to a third party (Google, GitHub, Facebook).                    |
| **JWT**             | JSON Web Token — a compact, signed token that encodes claims (user ID, email, expiry).                         |
| **HttpOnly Cookie** | A browser cookie that cannot be read by JavaScript — protects against XSS attacks.                             |
| **SameSite=None**   | A cookie attribute that allows the cookie to be sent on cross-site requests (required when frontend and backend are on different domains). |
| **BCrypt**          | A password hashing algorithm designed to be slow by default — makes brute-force attacks expensive.             |
| **Redis**           | An in-memory key-value store used here for refresh token storage, OTP storage, and rate-limit counters.        |
| **Eureka**          | A service discovery registry. Disabled (`eureka.client.enabled=false`) in all current environments.           |
| **OTLP**            | OpenTelemetry Protocol — the protocol used to export traces to Jaeger.                                         |
| **API Gateway**     | An upstream service that routes all client traffic and uses `POST /auth/validate` to verify user identity.     |
