package com.cshp.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String studentId = "20240001";
        String token = jwtUtil.generateToken(studentId);

        assertNotNull(token);
        assertEquals(studentId, jwtUtil.getStudentIdFromToken(token));
        assertTrue(jwtUtil.validateToken(token, studentId));
    }

    @Test
    void shouldDetectInvalidToken() {
        String studentId = "20240001";
        String token = jwtUtil.generateToken(studentId);

        assertFalse(jwtUtil.validateToken(token, "20240002"));
    }
}


