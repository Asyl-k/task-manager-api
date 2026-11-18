package dev.asyl.portfolio.taskmanager.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci10YXNrLW1hbmFnZXItYXBpLTIxIQ==";

    private final JwtService jwt = new JwtService(SECRET, 900);

    @Test
    void issuedTokenContainsSubjectRoleAndIssuer() {
        String token = jwt.issue(new AppUser("alice", "hash", AccountRole.USER));

        Claims claims = jwt.parse(token);

        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getIssuer()).isEqualTo("task-manager-api");
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwt.issue(new AppUser("alice", "hash", AccountRole.USER));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwt.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1);
        String token = shortLived.issue(new AppUser("alice", "hash", AccountRole.USER));

        Thread.sleep(1_100);

        assertThatThrownBy(() -> shortLived.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsBlankSecret() {
        assertThatThrownBy(() -> new JwtService(" ", 900))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectsKeyShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService("c2hvcnQ=", 900))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
