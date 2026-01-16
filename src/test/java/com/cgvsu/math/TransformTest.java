package com.cgvsu.math;

import com.cgvsu.model.Transform;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransformTest {

    @Test
    void testDefaultConstructor() {
        Transform transform = new Transform();

        assertEquals(0.0f, transform.getPosition().x, 1e-6f);
        assertEquals(0.0f, transform.getPosition().y, 1e-6f);
        assertEquals(0.0f, transform.getPosition().z, 1e-6f);

        assertEquals(0.0f, transform.getRotation().x, 1e-6f);
        assertEquals(0.0f, transform.getRotation().y, 1e-6f);
        assertEquals(0.0f, transform.getRotation().z, 1e-6f);

        assertEquals(1.0f, transform.getScale().x, 1e-6f);
        assertEquals(1.0f, transform.getScale().y, 1e-6f);
        assertEquals(1.0f, transform.getScale().z, 1e-6f);
    }

    @Test
    void testParameterizedConstructor() {
        Vector3f position = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f rotation = new Vector3f(0.5f, 1.0f, 1.5f);
        Vector3f scale = new Vector3f(2.0f, 3.0f, 4.0f);

        Transform transform = new Transform(position, rotation, scale);

        assertEquals(1.0f, transform.getPosition().x, 1e-6f);
        assertEquals(2.0f, transform.getPosition().y, 1e-6f);
        assertEquals(3.0f, transform.getPosition().z, 1e-6f);

        assertEquals(0.5f, transform.getRotation().x, 1e-6f);
        assertEquals(1.0f, transform.getRotation().y, 1e-6f);
        assertEquals(1.5f, transform.getRotation().z, 1e-6f);

        assertEquals(2.0f, transform.getScale().x, 1e-6f);
        assertEquals(3.0f, transform.getScale().y, 1e-6f);
        assertEquals(4.0f, transform.getScale().z, 1e-6f);
    }

    @Test
    void testCopyConstructor() {
        Transform original = new Transform(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(0.5f, 1.0f, 1.5f),
                new Vector3f(2.0f, 3.0f, 4.0f)
        );
        Transform copy = new Transform(original);

        assertEquals(original.getPosition().x, copy.getPosition().x, 1e-6f);
        assertEquals(original.getPosition().y, copy.getPosition().y, 1e-6f);
        assertEquals(original.getPosition().z, copy.getPosition().z, 1e-6f);

        assertEquals(original.getRotation().x, copy.getRotation().x, 1e-6f);
        assertEquals(original.getRotation().y, copy.getRotation().y, 1e-6f);
        assertEquals(original.getRotation().z, copy.getRotation().z, 1e-6f);

        assertEquals(original.getScale().x, copy.getScale().x, 1e-6f);
        assertEquals(original.getScale().y, copy.getScale().y, 1e-6f);
        assertEquals(original.getScale().z, copy.getScale().z, 1e-6f);

        assertNotSame(original, copy);
    }

    @Test
    void testCopyMethod() {
        Transform original = new Transform(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(0.5f, 1.0f, 1.5f),
                new Vector3f(2.0f, 3.0f, 4.0f)
        );
        Transform copy = original.copy();

        assertTrue(original.getPosition().equals(copy.getPosition()));
        assertTrue(original.getRotation().equals(copy.getRotation()));
        assertTrue(original.getScale().equals(copy.getScale()));
        assertNotSame(original, copy);
    }

    @Test
    void testGetTransformationMatrix() {
        Transform transform = new Transform(
                new Vector3f(10.0f, 20.0f, 30.0f), // translation
                new Vector3f(0.0f, 0.0f, 0.0f),    // no rotation
                new Vector3f(2.0f, 3.0f, 4.0f)     // scale
        );

        Matrix4f matrix = transform.getTransformationMatrix();

        // Model matrix = T * R * S
        // With no rotation, R = identity, so matrix = T * S

        // Check scale (diagonal elements)
        assertEquals(2.0f, matrix.get(0, 0), 1e-6f);
        assertEquals(3.0f, matrix.get(1, 1), 1e-6f);
        assertEquals(4.0f, matrix.get(2, 2), 1e-6f);

        // Check translation (last column)
        assertEquals(10.0f, matrix.get(0, 3), 1e-6f);
        assertEquals(20.0f, matrix.get(1, 3), 1e-6f);
        assertEquals(30.0f, matrix.get(2, 3), 1e-6f);

        // Check homogeneous coordinate
        assertEquals(1.0f, matrix.get(3, 3), 1e-6f);
    }

    @Test
    void testGetTransformationMatrixWithRotation() {
        Transform transform = new Transform(
                new Vector3f(0.0f, 0.0f, 0.0f), // no translation
                new Vector3f(0.0f, (float) Math.PI / 2, 0.0f), // 90° around Y
                new Vector3f(1.0f, 1.0f, 1.0f)  // no scale
        );

        Matrix4f matrix = transform.getTransformationMatrix();

        // Test rotation around Y by 90°
        // For a point (1, 0, 0), rotation should give (0, 0, -1)
        Vector3f point = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f transformed = matrix.multiply(point).toVector3f();

        assertEquals(0.0f, transformed.x, 1e-6f);
        assertEquals(0.0f, transformed.y, 1e-6f);
        assertEquals(-1.0f, transformed.z, 1e-6f);
    }

    @Test
    void testGetTransformationMatrixAlternative() {
        Transform transform = new Transform(
                new Vector3f(10.0f, 20.0f, 30.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(2.0f, 3.0f, 4.0f)
        );

        Matrix4f matrix1 = transform.getTransformationMatrix();
        Matrix4f matrix2 = transform.getTransformationMatrixAlternative();

        // Both methods should produce the same result for this simple case
        assertTrue(matrix1.equals(matrix2));
    }

    @Test
    void testSetGetPosition() {
        Transform transform = new Transform();
        Vector3f newPosition = new Vector3f(5.0f, 6.0f, 7.0f);

        transform.setPosition(newPosition);
        Vector3f retrieved = transform.getPosition();

        assertEquals(5.0f, retrieved.x, 1e-6f);
        assertEquals(6.0f, retrieved.y, 1e-6f);
        assertEquals(7.0f, retrieved.z, 1e-6f);
    }

    @Test
    void testSetGetRotation() {
        Transform transform = new Transform();
        Vector3f newRotation = new Vector3f(0.5f, 1.0f, 1.5f);

        transform.setRotation(newRotation);
        Vector3f retrieved = transform.getRotation();

        assertEquals(0.5f, retrieved.x, 1e-6f);
        assertEquals(1.0f, retrieved.y, 1e-6f);
        assertEquals(1.5f, retrieved.z, 1e-6f);
    }

    @Test
    void testSetGetScale() {
        Transform transform = new Transform();
        Vector3f newScale = new Vector3f(2.0f, 3.0f, 4.0f);

        transform.setScale(newScale);
        Vector3f retrieved = transform.getScale();

        assertEquals(2.0f, retrieved.x, 1e-6f);
        assertEquals(3.0f, retrieved.y, 1e-6f);
        assertEquals(4.0f, retrieved.z, 1e-6f);
    }

    @Test
    void testTranslate() {
        Transform transform = new Transform();

        // Test with individual components
        transform.translate(1.0f, 2.0f, 3.0f);
        assertEquals(1.0f, transform.getPosition().x, 1e-6f);
        assertEquals(2.0f, transform.getPosition().y, 1e-6f);
        assertEquals(3.0f, transform.getPosition().z, 1e-6f);

        // Test with Vector3f
        transform.translate(new Vector3f(4.0f, 5.0f, 6.0f));
        assertEquals(5.0f, transform.getPosition().x, 1e-6f); // 1 + 4
        assertEquals(7.0f, transform.getPosition().y, 1e-6f); // 2 + 5
        assertEquals(9.0f, transform.getPosition().z, 1e-6f); // 3 + 6
    }

    @Test
    void testRotate() {
        Transform transform = new Transform();

        // Test with individual components
        transform.rotate(0.1f, 0.2f, 0.3f);
        assertEquals(0.1f, transform.getRotation().x, 1e-6f);
        assertEquals(0.2f, transform.getRotation().y, 1e-6f);
        assertEquals(0.3f, transform.getRotation().z, 1e-6f);

        // Test with Vector3f
        transform.rotate(new Vector3f(0.4f, 0.5f, 0.6f));
        assertEquals(0.5f, transform.getRotation().x, 1e-6f); // 0.1 + 0.4
        assertEquals(0.7f, transform.getRotation().y, 1e-6f); // 0.2 + 0.5
        assertEquals(0.9f, transform.getRotation().z, 1e-6f); // 0.3 + 0.6
    }

    @Test
    void testScale() {
        Transform transform = new Transform();

        // Test with individual components
        transform.scale(2.0f, 3.0f, 4.0f);
        assertEquals(2.0f, transform.getScale().x, 1e-6f); // 1 * 2
        assertEquals(3.0f, transform.getScale().y, 1e-6f); // 1 * 3
        assertEquals(4.0f, transform.getScale().z, 1e-6f); // 1 * 4

        // Test with Vector3f
        transform.scale(new Vector3f(0.5f, 2.0f, 1.0f));
        assertEquals(1.0f, transform.getScale().x, 1e-6f); // 2 * 0.5
        assertEquals(6.0f, transform.getScale().y, 1e-6f); // 3 * 2
        assertEquals(4.0f, transform.getScale().z, 1e-6f); // 4 * 1

        // Test uniform scaling
        transform.scaleUniform(2.0f);
        assertEquals(2.0f, transform.getScale().x, 1e-6f); // 1 * 2
        assertEquals(12.0f, transform.getScale().y, 1e-6f); // 6 * 2
        assertEquals(8.0f, transform.getScale().z, 1e-6f); // 4 * 2
    }

    @Test
    void testReset() {
        Transform transform = new Transform(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(0.5f, 1.0f, 1.5f),
                new Vector3f(2.0f, 3.0f, 4.0f)
        );

        transform.reset();

        assertEquals(0.0f, transform.getPosition().x, 1e-6f);
        assertEquals(0.0f, transform.getPosition().y, 1e-6f);
        assertEquals(0.0f, transform.getPosition().z, 1e-6f);

        assertEquals(0.0f, transform.getRotation().x, 1e-6f);
        assertEquals(0.0f, transform.getRotation().y, 1e-6f);
        assertEquals(0.0f, transform.getRotation().z, 1e-6f);

        assertEquals(1.0f, transform.getScale().x, 1e-6f);
        assertEquals(1.0f, transform.getScale().y, 1e-6f);
        assertEquals(1.0f, transform.getScale().z, 1e-6f);
    }

}