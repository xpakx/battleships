package io.github.xpakx.battleships.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthenticationResponse {
    private String token;
    private String refresh_token;
    private String username;
    private boolean moderator_role;
}
