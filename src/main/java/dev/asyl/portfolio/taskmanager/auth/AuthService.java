package dev.asyl.portfolio.taskmanager.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request) {
        String username = request.username().trim();
        if (username.length() < 3) {
            throw new InvalidUsernameException("Username must contain at least 3 non-blank characters");
        }
        if (users.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        try {
            AppUser user = users.saveAndFlush(new AppUser(
                    username,
                    passwordEncoder.encode(request.password()),
                    AccountRole.USER
            ));
            return tokenFor(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameAlreadyExistsException(username);
        }
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        AppUser user = users.findByUsername(request.username().trim())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse me(String username) {
        return users.findByUsername(username)
                .map(AuthDtos.UserResponse::from)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private AuthDtos.TokenResponse tokenFor(AppUser user) {
        return new AuthDtos.TokenResponse(jwtService.issue(user), "Bearer", jwtService.expirationSeconds());
    }

    public static class UsernameAlreadyExistsException extends RuntimeException {
        public UsernameAlreadyExistsException(String username) {
            super("Username is already registered: " + username);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid username or password");
        }
    }

    public static class InvalidUsernameException extends RuntimeException {
        public InvalidUsernameException(String message) {
            super(message);
        }
    }
}
