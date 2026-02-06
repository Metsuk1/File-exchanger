package com.file_exchange.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 *  Password hashing and verification
 */
public class PasswordEncoder {
    private static final int BCRYPT_COST = 12;


    // This prevents timing attacks that could reveal whether an email exists
    private static final String DUMMY_HASH = BCrypt.hashpw("dummy", BCrypt.gensalt(BCRYPT_COST));

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * Performs constant-time password check even when user doesn't exist.
     * Prevents timing attacks for user enumeration.
     */
    public boolean matchesSafely(String rawPassword, String encodedPassword) {
        String hashToCheck = (encodedPassword != null) ? encodedPassword : DUMMY_HASH;
        return BCrypt.checkpw(rawPassword, hashToCheck) && encodedPassword != null;
    }
}
