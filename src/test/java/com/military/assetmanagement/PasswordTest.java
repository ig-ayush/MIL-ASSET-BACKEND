package com.military.assetmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordTest {

    @Test
    void generateAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Admin@123";
        String hash = encoder.encode(password);

        System.out.println("NEW HASH = " + hash);
        System.out.println("MATCH = " + encoder.matches(password, hash));
    }
}