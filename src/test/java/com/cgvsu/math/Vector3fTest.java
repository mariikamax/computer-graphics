package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector3fTest {

    @Test
    void testConstructorAndGetters() {
        Vector3f v = new Vector3f(1.0f, 2.0f, 3.0f);
        assertEquals(1.0f, v.x, 1e-6f);
        assertEquals(2.0f, v.y, 1e-6f);
        assertEquals(3.0f, v.z, 1e-6f);
    }

    @Test
    void testCopyConstructor() {
        Vector3f original = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f copy = new Vector3f(original);

        assertEquals(original.x, copy.x, 1e-6f);
        assertEquals(original.y, copy.y, 1e-6f);
        assertEquals(original.z, copy.z, 1e-6f);
        assertNotSame(original, copy);
    }

    @Test
    void testAdd() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        Vector3f result = v1.add(v2);

        assertEquals(5.0f, result.x, 1e-6f);
        assertEquals(7.0f, result.y, 1e-6f);
        assertEquals(9.0f, result.z, 1e-6f);
    }

    @Test
    void testSubtract() {
        Vector3f v1 = new Vector3f(5.0f, 6.0f, 7.0f);
        Vector3f v2 = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v1.subtract(v2);

        assertEquals(3.0f, result.x, 1e-6f);
        assertEquals(3.0f, result.y, 1e-6f);
        assertEquals(3.0f, result.z, 1e-6f);
    }

    @Test
    void testMultiplyByScalar() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v.multiply(2.0f);

        assertEquals(4.0f, result.x, 1e-6f);
        assertEquals(6.0f, result.y, 1e-6f);
        assertEquals(8.0f, result.z, 1e-6f);
    }

    @Test
    void testDivideByScalar() {
        Vector3f v = new Vector3f(6.0f, 8.0f, 10.0f);
        Vector3f result = v.divide(2.0f);

        assertEquals(3.0f, result.x, 1e-6f);
        assertEquals(4.0f, result.y, 1e-6f);
        assertEquals(5.0f, result.z, 1e-6f);
    }

    @Test
    void testDotProduct() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        float result = v1.dot(v2);

        assertEquals(32.0f, result, 1e-6f); // 1*4 + 2*5 + 3*6 = 32
    }

    @Test
    void testCrossProduct() {
        Vector3f v1 = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f v2 = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f result = v1.cross(v2);

        assertEquals(0.0f, result.x, 1e-6f);
        assertEquals(0.0f, result.y, 1e-6f);
        assertEquals(1.0f, result.z, 1e-6f);
    }

    @Test
    void testCrossProductAntiCommutative() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        Vector3f cross1 = v1.cross(v2);
        Vector3f cross2 = v2.cross(v1);

        // a × b = -(b × a)
        assertEquals(cross1.x, -cross2.x, 1e-6f);
        assertEquals(cross1.y, -cross2.y, 1e-6f);
        assertEquals(cross1.z, -cross2.z, 1e-6f);
    }

    @Test
    void testLength() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 6.0f);
        float length = v.length();

        assertEquals(7.0f, length, 1e-6f); // sqrt(2² + 3² + 6²) = sqrt(49) = 7
    }

    @Test
    void testLengthSquared() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 6.0f);
        float lengthSquared = v.lengthSquared();

        assertEquals(49.0f, lengthSquared, 1e-6f); // 2² + 3² + 6² = 49
    }

    @Test
    void testNormalized() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 6.0f);
        Vector3f normalized = v.normalized();

        float expectedLength = 7.0f;
        assertEquals(2.0f / expectedLength, normalized.x, 1e-6f);
        assertEquals(3.0f / expectedLength, normalized.y, 1e-6f);
        assertEquals(6.0f / expectedLength, normalized.z, 1e-6f);
        assertEquals(1.0f, normalized.length(), 1e-6f);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector3f v = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f normalized = v.normalized();

        assertEquals(0.0f, normalized.x, 1e-6f);
        assertEquals(0.0f, normalized.y, 1e-6f);
        assertEquals(0.0f, normalized.z, 1e-6f);
    }

    @Test
    void testNormalize() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 6.0f);
        v.normalize();

        float expectedLength = 7.0f;
        assertEquals(2.0f / expectedLength, v.x, 1e-6f);
        assertEquals(3.0f / expectedLength, v.y, 1e-6f);
        assertEquals(6.0f / expectedLength, v.z, 1e-6f);
        assertEquals(1.0f, v.length(), 1e-6f);
    }

    @Test
    void testEquals() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v3 = new Vector3f(1.1f, 2.0f, 3.0f);

        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }

}