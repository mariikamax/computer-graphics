package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Matrix4fTest {

    @Test
    void testIdentityMatrix() {
        Matrix4f m = new Matrix4f();

        assertTrue(m.isIdentity());
        assertEquals(1, m.get(0, 0), 1e-6f);
        assertEquals(0, m.get(0, 1), 1e-6f);
        assertEquals(1, m.get(3, 3), 1e-6f);
    }

    @Test
    void testMultiplication() {
        Matrix4f a = new Matrix4f(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        Matrix4f b = new Matrix4f(
                16, 15, 14, 13,
                12, 11, 10, 9,
                8, 7, 6, 5,
                4, 3, 2, 1
        );

        Matrix4f result = a.multiply(b);

        // Проверяем элемент (0,0)
        // 1*16 + 2*12 + 3*8 + 4*4 = 16 + 24 + 24 + 16 = 80
        assertEquals(80, result.get(0, 0), 1e-6f);
    }

    @Test
    void testVectorMultiplication() {
        Matrix4f m = new Matrix4f(
                1, 0, 0, 5,
                0, 1, 0, 10,
                0, 0, 1, 15,
                0, 0, 0, 1
        );

        Vector3f v = new Vector3f(2, 3, 4);
        Vector4f result = m.multiply(v);

        // Матрица переноса на (5, 10, 15)
        assertEquals(7, result.x, 1e-6f);  // 2 + 5 = 7
        assertEquals(13, result.y, 1e-6f); // 3 + 10 = 13
        assertEquals(19, result.z, 1e-6f); // 4 + 15 = 19
        assertEquals(1, result.w, 1e-6f);  // w = 1
    }

    @Test
    void testTranslationMatrix() {
        Matrix4f translation = Matrix4f.createTranslationMatrix(10, 20, 30);

        assertEquals(1, translation.get(0, 0), 1e-6f);
        assertEquals(10, translation.get(0, 3), 1e-6f);  // tx
        assertEquals(20, translation.get(1, 3), 1e-6f);  // ty
        assertEquals(30, translation.get(2, 3), 1e-6f);  // tz
    }

    @Test
    void testScaleMatrix() {
        Matrix4f scale = Matrix4f.createScaleMatrix(2, 3, 4);

        assertEquals(2, scale.get(0, 0), 1e-6f);
        assertEquals(3, scale.get(1, 1), 1e-6f);
        assertEquals(4, scale.get(2, 2), 1e-6f);
        assertEquals(1, scale.get(3, 3), 1e-6f);
    }

    @Test
    void testRotationMatrix() {
        float angle = (float) Math.PI / 2;  // 90 градусов

        Matrix4f rotX = Matrix4f.createRotationXMatrix(angle);
        Matrix4f rotY = Matrix4f.createRotationYMatrix(angle);
        Matrix4f rotZ = Matrix4f.createRotationZMatrix(angle);

        // Проверяем матрицу поворота вокруг Y
        // Для поворота на 90° вокруг Y:
        // cos(90) = 0, sin(90) = 1
        // Матрица должна быть:
        // [0, 0, 1, 0]
        // [0, 1, 0, 0]
        // [-1, 0, 0, 0]
        // [0, 0, 0, 1]

        assertEquals(0, rotY.get(0, 0), 1e-6f);  // cos(90) = 0
        assertEquals(0, rotY.get(0, 1), 1e-6f);  // 0
        assertEquals(1, rotY.get(0, 2), 1e-6f);  // sin(90) = 1
        assertEquals(0, rotY.get(0, 3), 1e-6f);  // 0

        assertEquals(0, rotY.get(1, 0), 1e-6f);  // 0
        assertEquals(1, rotY.get(1, 1), 1e-6f);  // 1
        assertEquals(0, rotY.get(1, 2), 1e-6f);  // 0
        assertEquals(0, rotY.get(1, 3), 1e-6f);  // 0

        assertEquals(-1, rotY.get(2, 0), 1e-6f); // -sin(90) = -1
        assertEquals(0, rotY.get(2, 1), 1e-6f);  // 0
        assertEquals(0, rotY.get(2, 2), 1e-6f);  // cos(90) = 0
        assertEquals(0, rotY.get(2, 3), 1e-6f);  // 0

        assertEquals(0, rotY.get(3, 0), 1e-6f);  // 0
        assertEquals(0, rotY.get(3, 1), 1e-6f);  // 0
        assertEquals(0, rotY.get(3, 2), 1e-6f);  // 0
        assertEquals(1, rotY.get(3, 3), 1e-6f);  // 1
    }

    @Test
    void testLookAtMatrix() {
        Vector3f eye = new Vector3f(0, 0, 5);
        Vector3f target = new Vector3f(0, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);

        Matrix4f view = Matrix4f.createLookAtMatrix(eye, target, up);

        // Камера смотрит вдоль оси -Z (из точки (0,0,5) в (0,0,0))
        // zAxis = (0,0,-1) (нормализованный)
        // xAxis = up.cross(zAxis) = (0,1,0).cross(0,0,-1) = (-1,0,0)
        // yAxis = zAxis.cross(xAxis) = (0,0,-1).cross(-1,0,0) = (0,1,0)

        // Проверяем базисные векторы в матрице
        // xAxis = (-1, 0, 0)
        assertEquals(-1, view.get(0, 0), 1e-6f);  // xAxis.x
        assertEquals(0, view.get(1, 0), 1e-6f);   // xAxis.y
        assertEquals(0, view.get(2, 0), 1e-6f);   // xAxis.z

        // yAxis = (0, 1, 0)
        assertEquals(0, view.get(0, 1), 1e-6f);   // yAxis.x
        assertEquals(1, view.get(1, 1), 1e-6f);   // yAxis.y
        assertEquals(0, view.get(2, 1), 1e-6f);   // yAxis.z

        // zAxis = (0, 0, -1)
        assertEquals(0, view.get(0, 2), 1e-6f);   // zAxis.x
        assertEquals(0, view.get(1, 2), 1e-6f);   // zAxis.y
        assertEquals(-1, view.get(2, 2), 1e-6f);  // zAxis.z

        // Перевод
        assertEquals(0, view.get(3, 0), 1e-6f);   // -xAxis.dot(eye) = -(-1)*0 + 0*0 + 0*5 = 0
        assertEquals(0, view.get(3, 1), 1e-6f);   // -yAxis.dot(eye) = -(0*0 + 1*0 + 0*5) = 0
        assertEquals(5, view.get(3, 2), 1e-6f);   // -zAxis.dot(eye) = -(0*0 + 0*0 + (-1)*5) = 5
    }

    @Test
    void testPerspectiveMatrix() {
        float fov = (float) Math.toRadians(45);
        float aspect = 16.0f / 9.0f;
        float near = 0.1f;
        float far = 100.0f;

        Matrix4f proj = Matrix4f.createPerspectiveMatrix(fov, aspect, near, far);

        // Проверяем основные свойства
        assertNotEquals(0, proj.get(0, 0), 1e-6f);  // f/aspect
        assertNotEquals(0, proj.get(1, 1), 1e-6f);  // f
        assertTrue(proj.get(2, 2) < 0);  // В левосторонней системе это отрицательное
        assertTrue(proj.get(2, 3) < 0);  // И это тоже отрицательное
        assertEquals(-1, proj.get(3, 2), 1e-6f);  // Для перспективного деления
    }

    @Test
    void testInverseOfTranslation() {
        Matrix4f translation = Matrix4f.createTranslationMatrix(10, 20, 30);
        Matrix4f inverse = translation.inverse();

        // Обратная матрица переноса = перенос в обратную сторону
        assertEquals(1, inverse.get(0, 0), 1e-6f);
        assertEquals(-10, inverse.get(0, 3), 1e-6f);
        assertEquals(-20, inverse.get(1, 3), 1e-6f);
        assertEquals(-30, inverse.get(2, 3), 1e-6f);
    }

    @Test
    void testTranspose() {
        Matrix4f m = new Matrix4f(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        Matrix4f transposed = m.transpose();

        // Проверяем несколько элементов
        assertEquals(1, transposed.get(0, 0), 1e-6f);
        assertEquals(5, transposed.get(0, 1), 1e-6f);  // Было 2, стало 5
        assertEquals(9, transposed.get(0, 2), 1e-6f);  // Было 3, стало 9
        assertEquals(2, transposed.get(1, 0), 1e-6f);  // Было 5, стало 2
    }

    @Test
    void testModelMatrixCreation() {
        Vector3f position = new Vector3f(10, 20, 30);
        Vector3f rotation = new Vector3f((float)Math.PI/4, (float)Math.PI/3, (float)Math.PI/6);
        Vector3f scale = new Vector3f(2, 3, 4);

        Matrix4f model = Matrix4f.createModelMatrix(position, rotation, scale);

        // Проверяем, что матрица не нулевая
        assertFalse(model.isIdentity());
        // Проверяем позицию в матрице
        assertEquals(10, model.get(0, 3), 1e-6f);
        assertEquals(20, model.get(1, 3), 1e-6f);
        assertEquals(30, model.get(2, 3), 1e-6f);
    }
}