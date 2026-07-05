package org.vrajpatel.fintrackergateway.ResponseDto;

public class JwtValidationData {
    private String userId;
    private String userEmail;

    public String getUserId()              { return userId; }
    public void   setUserId(String v)      { this.userId = v; }
    public String getUserEmail()           { return userEmail; }
    public void   setUserEmail(String v)   { this.userEmail = v; }
}
