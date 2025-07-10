package org.example.test_runner;

import java.util.Objects;

public class Assertions {
    public static void assertEquals(Object expected, Object actual) {
        if (expected instanceof Number && actual instanceof Number) {
            long expectedLong = ((Number) expected).longValue();
            long actualLong = ((Number) actual).longValue();
            if (expectedLong != actualLong) {
                throw new AssertionError("Expected " + expected + " but got " + actual);
            }
        } else {
            if (!Objects.equals(expected, actual)) {
                throw new AssertionError("Expected " + expected + " but got " + actual);
            }
        }
    }


    public static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but got false");
        }
    }

    public static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but got true");
        }
    }

    public static void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Expected object to be not null");
        }
    }
}
