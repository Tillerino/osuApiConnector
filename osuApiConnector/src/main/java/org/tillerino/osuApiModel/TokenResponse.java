package org.tillerino.osuApiModel;

public class TokenResponse {
    public final String accessToken;
    public final int expiresIn;

    public TokenResponse(String accessToken, int expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
