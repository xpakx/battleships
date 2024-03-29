package io.github.xpakx.battleships.user;

import io.github.xpakx.battleships.security.JwtUtils;
import io.github.xpakx.battleships.user.dto.AuthenticationRequest;
import io.github.xpakx.battleships.user.dto.AuthenticationResponse;
import io.github.xpakx.battleships.user.dto.RegistrationRequest;
import io.github.xpakx.battleships.user.error.AuthenticationException;
import io.github.xpakx.battleships.user.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegistrationRequest request) {
        testRequest(request);
        User user = createNewUser(request);
        authenticate(request.getUsername(), request.getPassword());
        final String token = jwtUtils.generateToken(userService.userAccountToUserDetails(user));
        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .moderator_role(false)
                .build();
    }

    private User createNewUser(RegistrationRequest request) {
        Set<UserRole> roles = new HashSet<>();
        User userToAdd = new User();
        userToAdd.setPassword(passwordEncoder.encode(request.getPassword()));
        userToAdd.setUsername(request.getUsername());
        userToAdd.setRoles(roles);
        return userRepository.save(userToAdd);
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new AuthenticationException("User " +username+" disabled!");
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid password!");
        }
    }

    private void testRequest(RegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ValidationException("Username exists!");
        }
    }

    public AuthenticationResponse generateAuthenticationToken(AuthenticationRequest authenticationRequest) {
        final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final String token = jwtUtils.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(token)
                .username(authenticationRequest.getUsername())
                .moderator_role(
                        userDetails.getAuthorities().stream()
                                .anyMatch((a) -> a.getAuthority().equals("MODERATOR"))
                )
                .build();
    }
}
