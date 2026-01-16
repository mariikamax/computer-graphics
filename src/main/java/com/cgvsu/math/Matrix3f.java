package com.cgvsu.math;

/**
 * Матрица 3x3 для линейных преобразований
 * Работа с векторами-столбцами
 */
public class Matrix3f {
    private float[][] m;

    public Matrix3f() {
        m = new float[3][3];
        setIdentity();
    }

    public Matrix3f(float[][] values) {
        if (values.length != 3 || values[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        m = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(values[i], 0, m[i], 0, 3);
        }
    }

    public Matrix3f(float m00, float m01, float m02,
                    float m10, float m11, float m12,
                    float m20, float m21, float m22) {
        m = new float[3][3];
        m[0][0] = m00; m[0][1] = m01; m[0][2] = m02;
        m[1][0] = m10; m[1][1] = m11; m[1][2] = m12;
        m[2][0] = m20; m[2][1] = m21; m[2][2] = m22;
    }

    // Единичная матрица
    public void setIdentity() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }

    // Нулевая матрица
    public void setZero() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = 0.0f;
            }
        }
    }

    // Умножение на другую матрицу (this * other)
    public Matrix3f multiply(Matrix3f other) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.m[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    result.m[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return result;
    }

    // Умножение матрицы на вектор-столбец
    public Vector3f multiply(Vector3f v) {
        float x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z;
        float y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z;
        float z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z;
        return new Vector3f(x, y, z);
    }

    // Сложение матриц
    public Matrix3f add(Matrix3f other) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.m[i][j] = this.m[i][j] + other.m[i][j];
            }
        }
        return result;
    }

    // Вычитание матриц
    public Matrix3f subtract(Matrix3f other) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.m[i][j] = this.m[i][j] - other.m[i][j];
            }
        }
        return result;
    }

    // Умножение на скаляр
    public Matrix3f multiply(float scalar) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.m[i][j] = this.m[i][j] * scalar;
            }
        }
        return result;
    }

    // Транспонирование
    public Matrix3f transpose() {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.m[i][j] = this.m[j][i];
            }
        }
        return result;
    }

    // Определитель
    public float determinant() {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }

    // Обратная матрица
    public Matrix3f inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-10f) {
            throw new ArithmeticException("Matrix is not invertible");
        }

        float invDet = 1.0f / det;

        return new Matrix3f(
                invDet * (m[1][1] * m[2][2] - m[1][2] * m[2][1]),
                invDet * (m[0][2] * m[2][1] - m[0][1] * m[2][2]),
                invDet * (m[0][1] * m[1][2] - m[0][2] * m[1][1]),

                invDet * (m[1][2] * m[2][0] - m[1][0] * m[2][2]),
                invDet * (m[0][0] * m[2][2] - m[0][2] * m[2][0]),
                invDet * (m[0][2] * m[1][0] - m[0][0] * m[1][2]),

                invDet * (m[1][0] * m[2][1] - m[1][1] * m[2][0]),
                invDet * (m[0][1] * m[2][0] - m[0][0] * m[2][1]),
                invDet * (m[0][0] * m[1][1] - m[0][1] * m[1][0])
        );
    }

    // Получение значения по индексам
    public float get(int row, int col) {
        return m[row][col];
    }

    // Установка значения по индексам
    public void set(int row, int col, float value) {
        m[row][col] = value;
    }

    // Получение строки
    public Vector3f getRow(int row) {
        return new Vector3f(m[row][0], m[row][1], m[row][2]);
    }

    // Получение столбца
    public Vector3f getColumn(int col) {
        return new Vector3f(m[0][col], m[1][col], m[2][col]);
    }

    // Создание матрицы масштабирования
    public static Matrix3f createScaleMatrix(float sx, float sy, float sz) {
        return new Matrix3f(
                sx, 0, 0,
                0, sy, 0,
                0, 0, sz
        );
    }

    // Создание матрицы поворота вокруг оси X
    public static Matrix3f createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix3f(
                1, 0, 0,
                0, cos, -sin,
                0, sin, cos
        );
    }

    // Создание матрицы поворота вокруг оси Y
    public static Matrix3f createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix3f(
                cos, 0, sin,
                0, 1, 0,
                -sin, 0, cos
        );
    }

    // Создание матрицы поворота вокруг оси Z
    public static Matrix3f createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix3f(
                cos, -sin, 0,
                sin, cos, 0,
                0, 0, 1
        );
    }

    // Создание матрицы поворота вокруг произвольной оси
    public static Matrix3f createRotationMatrix(Vector3f axis, float angle) {
        Vector3f normAxis = axis.normalized();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float oneMinusCos = 1 - cos;

        float x = normAxis.x;
        float y = normAxis.y;
        float z = normAxis.z;

        return new Matrix3f(
                cos + x*x*oneMinusCos,   x*y*oneMinusCos - z*sin, x*z*oneMinusCos + y*sin,
                y*x*oneMinusCos + z*sin, cos + y*y*oneMinusCos,   y*z*oneMinusCos - x*sin,
                z*x*oneMinusCos - y*sin, z*y*oneMinusCos + x*sin, cos + z*z*oneMinusCos
        );
    }

    // Создание матрицы из кватерниона
    public static Matrix3f fromQuaternion(float x, float y, float z, float w) {
        float xx = x * x;
        float xy = x * y;
        float xz = x * z;
        float xw = x * w;

        float yy = y * y;
        float yz = y * z;
        float yw = y * w;

        float zz = z * z;
        float zw = z * w;

        return new Matrix3f(
                1 - 2 * (yy + zz), 2 * (xy - zw),     2 * (xz + yw),
                2 * (xy + zw),     1 - 2 * (xx + zz), 2 * (yz - xw),
                2 * (xz - yw),     2 * (yz + xw),     1 - 2 * (xx + yy)
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix3f:\n" +
                        "[%.3f, %.3f, %.3f]\n" +
                        "[%.3f, %.3f, %.3f]\n" +
                        "[%.3f, %.3f, %.3f]",
                m[0][0], m[0][1], m[0][2],
                m[1][0], m[1][1], m[1][2],
                m[2][0], m[2][1], m[2][2]
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Matrix3f other = (Matrix3f) obj;
        final float eps = 1e-6f;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(m[i][j] - other.m[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result = 31 * result + Float.floatToIntBits(m[i][j]);
            }
        }
        return result;
    }
}