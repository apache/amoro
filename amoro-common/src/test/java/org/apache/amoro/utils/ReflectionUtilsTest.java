package org.apache.amoro.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilsTest {

    @Test
    void testInvokeValidClassAndMethod() {
        try {
            // Invoke the static method in TestClass
            Object result = ReflectionUtils.invoke("java.lang.System", "currentTimeMillis");
            assertNotNull(result);
        } catch (Exception e) {
            fail("Invocation failed: " + e.getMessage());
        }
    }

    @Test
    void testInvokeNonExistentClass() {
        try {
            Object result = ReflectionUtils.invoke("com.example.nonexistent.NonExistentClass", "testMethod");
            assertNull(result); // The method should return null for non-existent class
        } catch (Exception e) {
            fail("Exception should not be thrown for non-existent class: " + e.getMessage());
        }
    }
}