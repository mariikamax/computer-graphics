package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector2fTest {

    @Test
    void testConstructorAndGetters() {
        Vector2f v = new Vector2f(1.5f, 2.5f);
        assertEquals(1.5f, v.x, 1e-6f);
        assertEquals(2.5f, v.y, 1e-6f);
    }

    @Test
    void testCopyConstructor() {
        Vector2f original = new Vector2f(3.0f, 4.0f);
        Vector2f copy = new Vector2f(original);
        assertEquals(original.x, copy.x, 1e-6f);
        assertEquals(original.y, copy.y, 1e-6f);
        assertNotSame(original, copy);
    }

    @Test
    void testAdd() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        Vector2f result = v1.add(v2);

        assertEquals(4.0f, result.x, 1e-6f);
        assertEquals(6.0f, result.y, 1e-6f);
        // Original vectors unchanged
        assertEquals(1.0f, v1.x, 1e-6f);
        assertEquals(2.0f, v1.y, 1e-6f);
    }

    @Test
    void testSubtract() {
        Vector2f v1 = new Vector2f(5.0f, 6.0f);
        Vector2f v2 = new Vector2f(2.0f, 3.0f);
        Vector2f result = v1.subtract(v2);

        assertEquals(3.0f, result.x, 1e-6f);
        assertEquals(3.0f, result.y, 1e-6f);
    }

    @Test
    void testMultiplyByScalar() {
        Vector2f v = new Vector2f(2.0f, 3.0f);
        Vector2f result = v.multiply(2.0f);

        assertEquals(4.0f, result.x, 1e-6f);
        assertEquals(6.0f, result.y, 1e-6f);
    }

    @Test
    void testDivideByScalar() {
        Vector2f v = new Vector2f(6.0f, 8.0f);
        Vector2f result = v.divide(2.0f);

        assertEquals(3.0f, result.x, 1e-6f);
        assertEquals(4.0f, result.y, 1e-6f);
    }

    @Test
    void testDivideByZero() {
        Vector2f v = new Vector2f(1.0f, 2.0f);
        Vector2f result = v.divide(0.0f);

        assertTrue(Float.isInfinite(result.x) || Float.isNaN(result.x));
    }

    @Test
    void testDotProduct() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        float result = v1.dot(v2);

        assertEquals(11.0f, result, 1e-6f); // 1*3 + 2*4 = 11
    }

    @Test
    void testLength() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        float length = v.length();

        assertEquals(5.0f, length, 1e-6f); // sqrt(3² + 4²) = 5
    }

    @Test
    void testNormalized() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        Vector2f normalized = v.normalized();

        assertEquals(0.6f, normalized.x, 1e-6f); // 3/5 = 0.6
        assertEquals(0.8f, normalized.y, 1e-6f); // 4/5 = 0.8
        assertEquals(1.0f, normalized.length(), 1e-6f);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector2f v = new Vector2f(0.0f, 0.0f);
        Vector2f normalized = v.normalized();

        assertEquals(0.0f, normalized.x, 1e-6f);
        assertEquals(0.0f, normalized.y, 1e-6f);
    }

    @Test
    void testNormalize() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        v.normalize();

        assertEquals(0.6f, v.x, 1e-6f);
        assertEquals(0.8f, v.y, 1e-6f);
        assertEquals(1.0f, v.length(), 1e-6f);
    }

    @Test
    void testEquals() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(1.0f, 2.0f);
        Vector2f v3 = new Vector2f(1.1f, 2.0f);

        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }

}