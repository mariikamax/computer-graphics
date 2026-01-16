package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Matrix3fTest {

    @Test
    void testIdentityMatrix() {
        Matrix3f m = new Matrix3f();

        assertTrue(m.equals(new Matrix3f(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        )));
    }

    @Test
    void testMultiplication() {
        Matrix3f a = new Matrix3f(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        );

        Matrix3f b = new Matrix3f(
                9, 8, 7,
                6, 5, 4,
                3, 2, 1
        );

        Matrix3f result = a.multiply(b);

        // Проверяем первую строку
        assertEquals(30, result.get(0, 0), 1e-6f);
        assertEquals(24, result.get(0, 1), 1e-6f);
        assertEquals(18, result.get(0, 2), 1e-6f);
    }

    @Test
    void testVectorMultiplication() {
        Matrix3f m = new Matrix3f(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        );

        Vector3f v = new Vector3f(1, 2, 3);
        Vector3f result = m.multiply(v);

        assertEquals(14, result.x, 1e-6f);  // 1*1 + 2*2 + 3*3 = 14
        assertEquals(32, result.y, 1e-6f);  // 4*1 + 5*2 + 6*3 = 32
        assertEquals(50, result.z, 1e-6f);  // 7*1 + 8*2 + 9*3 = 50
    }

    @Test
    void testDeterminant() {
        Matrix3f m = new Matrix3f(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        );

        float det = m.determinant();
        assertEquals(0, det, 1e-6f);  // Строки линейно зависимы
    }

    @Test
    void testInverse() {
        Matrix3f m = new Matrix3f(
                1, 0, 0,
                0, 2, 0,
                0, 0, 3
        );

        Matrix3f inv = m.inverse();

        // Обратная диагональная матрица = 1/диагональные элементы
        assertEquals(1, inv.get(0, 0), 1e-6f);
        assertEquals(0.5f, inv.get(1, 1), 1e-6f);
        assertEquals(1/3f, inv.get(2, 2), 1e-6f);
    }

    @Test
    void testTranspose() {
        Matrix3f m = new Matrix3f(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        );

        Matrix3f transposed = m.transpose();

        assertEquals(1, transposed.get(0, 0), 1e-6f);
        assertEquals(4, transposed.get(0, 1), 1e-6f);  // Было 2, стало 4
        assertEquals(7, transposed.get(0, 2), 1e-6f);
    }

    @Test
    void testRotationMatrix() {
        float angle = (float) Math.PI / 2;  // 90 градусов

        Matrix3f rotX = Matrix3f.createRotationXMatrix(angle);
        Matrix3f rotY = Matrix3f.createRotationYMatrix(angle);
        Matrix3f rotZ = Matrix3f.createRotationZMatrix(angle);

        // Проверяем матрицу поворота вокруг Z
        assertEquals(0, rotZ.get(0, 0), 1e-6f);  // cos(90) = 0
        assertEquals(-1, rotZ.get(0, 1), 1e-6f); // -sin(90) = -1
        assertEquals(1, rotZ.get(1, 0), 1e-6f);  // sin(90) = 1
        assertEquals(0, rotZ.get(1, 1), 1e-6f);  // cos(90) = 0
    }
}