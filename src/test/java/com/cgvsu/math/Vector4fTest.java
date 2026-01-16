package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector4fTest {

    @Test
    void testConstructor() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(1.0f, v.x, 1e-6f);
        assertEquals(2.0f, v.y, 1e-6f);
        assertEquals(3.0f, v.z, 1e-6f);
        assertEquals(4.0f, v.w, 1e-6f);
    }

    @Test
    void testConstructorFromVector3f() {
        Vector3f v3 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector4f v4 = new Vector4f(v3, 1.0f);

        assertEquals(1.0f, v4.x, 1e-6f);
        assertEquals(2.0f, v4.y, 1e-6f);
        assertEquals(3.0f, v4.z, 1e-6f);
        assertEquals(1.0f, v4.w, 1e-6f);
    }

    @Test
    void testCopyConstructor() {
        Vector4f original = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f copy = new Vector4f(original);

        assertEquals(original.x, copy.x, 1e-6f);
        assertEquals(original.y, copy.y, 1e-6f);
        assertEquals(original.z, copy.z, 1e-6f);
        assertEquals(original.w, copy.w, 1e-6f);
        assertNotSame(original, copy);
    }

    @Test
    void testToVector3f() {
        Vector4f v4 = new Vector4f(2.0f, 4.0f, 6.0f, 2.0f);
        Vector3f v3 = v4.toVector3f();

        assertEquals(1.0f, v3.x, 1e-6f); // 2/2 = 1
        assertEquals(2.0f, v3.y, 1e-6f); // 4/2 = 2
        assertEquals(3.0f, v3.z, 1e-6f); // 6/2 = 3
    }

    @Test
    void testToVector3fWithZeroW() {
        Vector4f v4 = new Vector4f(2.0f, 4.0f, 6.0f, 0.0f);
        Vector3f v3 = v4.toVector3f();

        // When w=0, no division should occur
        assertEquals(2.0f, v3.x, 1e-6f);
        assertEquals(4.0f, v3.y, 1e-6f);
        assertEquals(6.0f, v3.z, 1e-6f);
    }

    @Test
    void testNormalized() {
        Vector4f v = new Vector4f(2.0f, 4.0f, 6.0f, 2.0f);
        Vector4f normalized = v.normalized();

        assertEquals(1.0f, normalized.x, 1e-6f);
        assertEquals(2.0f, normalized.y, 1e-6f);
        assertEquals(3.0f, normalized.z, 1e-6f);
        assertEquals(1.0f, normalized.w, 1e-6f);
    }

    @Test
    void testNormalizedWhenWIsOne() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 1.0f);
        Vector4f normalized = v.normalized();

        assertEquals(1.0f, normalized.x, 1e-6f);
        assertEquals(2.0f, normalized.y, 1e-6f);
        assertEquals(3.0f, normalized.z, 1e-6f);
        assertEquals(1.0f, normalized.w, 1e-6f);
    }

    @Test
    void testNormalizedWhenWIsZero() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 0.0f);
        Vector4f normalized = v.normalized();

        assertEquals(1.0f, normalized.x, 1e-6f);
        assertEquals(2.0f, normalized.y, 1e-6f);
        assertEquals(3.0f, normalized.z, 1e-6f);
        assertEquals(0.0f, normalized.w, 1e-6f);
    }

    @Test
    void testNormalize() {
        Vector4f v = new Vector4f(2.0f, 4.0f, 6.0f, 2.0f);
        v.normalize();

        assertEquals(1.0f, v.x, 1e-6f);
        assertEquals(2.0f, v.y, 1e-6f);
        assertEquals(3.0f, v.z, 1e-6f);
        assertEquals(1.0f, v.w, 1e-6f);
    }

}