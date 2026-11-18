package dev.asyl.portfolio.taskmanager.config;

import dev.asyl.portfolio.taskmanager.auth.AccountRole;
import dev.asyl.portfolio.taskmanager.auth.AppUser;
import dev.asyl.portfolio.taskmanager.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String userPassword;

    public DemoDataInitializer(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_ADMIN_PASSWORD:admin12345}") String adminPassword,
            @Value("${DEMO_USER_PASSWORD:demo12345}") String userPassword
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("admin", adminPassword, AccountRole.ADMIN);
        createIfMissing("demo", userPassword, AccountRole.USER);
    }

    private void createIfMissing(String username, String password, AccountRole role) {
        if (users.existsByUsername(username)) {
            return;
        }
        users.save(new AppUser(username, passwordEncoder.encode(password), role));
        log.info("Created demo {} account '{}'", role, username);
    }
}
