package com.cgvsu.math;

/**
 * Матрица 4x4 для аффинных преобразований и перспективной проекции
 * Работа с векторами-столбцами
 */
public class Matrix4f {
    private float[][] m;

    public Matrix4f() {
        m = new float[4][4];
        setIdentity();
    }

    public Matrix4f(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        m = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, m[i], 0, 4);
        }
    }

    public Matrix4f(float m00, float m01, float m02, float m03,
                    float m10, float m11, float m12, float m13,
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        m = new float[4][4];
        m[0][0] = m00; m[0][1] = m01; m[0][2] = m02; m[0][3] = m03;
        m[1][0] = m10; m[1][1] = m11; m[1][2] = m12; m[1][3] = m13;
        m[2][0] = m20; m[2][1] = m21; m[2][2] = m22; m[2][3] = m23;
        m[3][0] = m30; m[3][1] = m31; m[3][2] = m32; m[3][3] = m33;
    }

    // Конструктор из Matrix3f (добавляется однородная координата)
    public Matrix4f(Matrix3f mat3) {
        m = new float[4][4];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = mat3.get(i, j);
            }
        }
        m[3][0] = m[3][1] = m[3][2] = 0;
        m[0][3] = m[1][3] = m[2][3] = 0;
        m[3][3] = 1;
    }

    // Копирующий конструктор
    public Matrix4f(Matrix4f other) {
        m = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(other.m[i], 0, m[i], 0, 4);
        }
    }

    // Единичная матрица
    public void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }

    // Нулевая матрица
    public void setZero() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = 0.0f;
            }
        }
    }

    // Умножение на другую матрицу (this * other)
    public Matrix4f multiply(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result.m[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return result;
    }

    // Умножение матрицы на вектор-столбец (4D)
    public Vector4f multiply(Vector4f v) {
        float x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z + m[0][3] * v.w;
        float y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z + m[1][3] * v.w;
        float z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z + m[2][3] * v.w;
        float w = m[3][0] * v.x + m[3][1] * v.y + m[3][2] * v.z + m[3][3] * v.w;
        return new Vector4f(x, y, z, w);
    }

    // Умножение матрицы на трехмерный вектор (добавляется w=1)
    public Vector4f multiply(Vector3f v) {
        return multiply(new Vector4f(v, 1.0f));
    }

    // Сложение матриц
    public Matrix4f add(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = this.m[i][j] + other.m[i][j];
            }
        }
        return result;
    }

    // Вычитание матриц
    public Matrix4f subtract(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = this.m[i][j] - other.m[i][j];
            }
        }
        return result;
    }

    // Умножение на скаляр
    public Matrix4f multiply(float scalar) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = this.m[i][j] * scalar;
            }
        }
        return result;
    }

    // Транспонирование
    public Matrix4f transpose() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.m[i][j] = this.m[j][i];
            }
        }
        return result;
    }

    // Определитель матрицы (реализация через разложение по строке)
    public float determinant() {
        float det = 0;

        // Разложение по первой строке
        for (int j = 0; j < 4; j++) {
            float[][] minor = new float[3][3];

            // Создание минора 3x3
            for (int row = 1; row < 4; row++) {
                int minorCol = 0;
                for (int col = 0; col < 4; col++) {
                    if (col == j) continue;
                    minor[row-1][minorCol] = m[row][col];
                    minorCol++;
                }
            }

            Matrix3f minorMatrix = new Matrix3f(minor);
            float minorDet = minorMatrix.determinant();

            det += (j % 2 == 0 ? 1 : -1) * m[0][j] * minorDet;
        }

        return det;
    }

    // Обратная матрица (методом алгебраических дополнений)
    public Matrix4f inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-10f) {
            throw new ArithmeticException("Matrix is not invertible (det = " + det + ")");
        }

        float invDet = 1.0f / det;
        Matrix4f result = new Matrix4f();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Создаем минор 3x3
                float[][] minor = new float[3][3];
                int minorRow = 0;

                for (int row = 0; row < 4; row++) {
                    if (row == i) continue;
                    int minorCol = 0;

                    for (int col = 0; col < 4; col++) {
                        if (col == j) continue;
                        minor[minorRow][minorCol] = m[row][col];
                        minorCol++;
                    }
                    minorRow++;
                }

                Matrix3f minorMatrix = new Matrix3f(minor);
                float cofactor = ((i + j) % 2 == 0 ? 1 : -1) * minorMatrix.determinant();

                // Транспонируем при заполнении (алгебраическое дополнение)
                result.m[j][i] = cofactor * invDet;
            }
        }

        return result;
    }

    // Получение значения по индексам
    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Matrix indices must be between 0 and 3");
        }
        return m[row][col];
    }

    // Установка значения по индексам
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Matrix indices must be between 0 and 3");
        }
        m[row][col] = value;
    }

    // Получение верхней левой матрицы 3x3 (линейная часть)
    public Matrix3f getRotationScale() {
        return new Matrix3f(
                m[0][0], m[0][1], m[0][2],
                m[1][0], m[1][1], m[1][2],
                m[2][0], m[2][1], m[2][2]
        );
    }

    // Получение вектора трансляции
    public Vector3f getTranslation() {
        return new Vector3f(m[0][3], m[1][3], m[2][3]);
    }

    // Установка вектора трансляции
    public void setTranslation(Vector3f translation) {
        m[0][3] = translation.x;
        m[1][3] = translation.y;
        m[2][3] = translation.z;
    }

    // Создание матрицы масштабирования
    public static Matrix4f createScaleMatrix(float sx, float sy, float sz) {
        return new Matrix4f(
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        );
    }

    // Создание матрицы поворота вокруг оси X
    public static Matrix4f createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                1, 0, 0, 0,
                0, cos, -sin, 0,
                0, sin, cos, 0,
                0, 0, 0, 1
        );
    }

    // Создание матрицы поворота вокруг оси Y
    public static Matrix4f createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        );
    }

    // Создание матрицы поворота вокруг оси Z
    public static Matrix4f createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                cos, -sin, 0, 0,
                sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    // Создание матрицы поворота вокруг произвольной оси
    public static Matrix4f createRotationMatrix(Vector3f axis, float angle) {
        Vector3f normAxis = axis.normalized();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float oneMinusCos = 1 - cos;

        float x = normAxis.x;
        float y = normAxis.y;
        float z = normAxis.z;

        return new Matrix4f(
                cos + x*x*oneMinusCos,   x*y*oneMinusCos - z*sin, x*z*oneMinusCos + y*sin, 0,
                y*x*oneMinusCos + z*sin, cos + y*y*oneMinusCos,   y*z*oneMinusCos - x*sin, 0,
                z*x*oneMinusCos - y*sin, z*y*oneMinusCos + x*sin, cos + z*z*oneMinusCos,   0,
                0, 0, 0, 1
        );
    }

    // Создание матрицы переноса
    public static Matrix4f createTranslationMatrix(float tx, float ty, float tz) {
        return new Matrix4f(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    public static Matrix4f createTranslationMatrix(Vector3f translation) {
        return createTranslationMatrix(translation.x, translation.y, translation.z);
    }

    // Создание матрицы вида (lookAt) - для векторов-столбцов
    public static Matrix4f createLookAtMatrix(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = target.subtract(eye).normalized();
        Vector3f xAxis = up.cross(zAxis).normalized();
        Vector3f yAxis = zAxis.cross(xAxis);

        // Для векторов-столбцов матрица имеет вид:
        // [ R^T | -R^T * eye ]
        // где R = [x, y, z] - базис камеры

        return new Matrix4f(
                xAxis.x, yAxis.x, zAxis.x, 0,
                xAxis.y, yAxis.y, zAxis.y, 0,
                xAxis.z, yAxis.z, zAxis.z, 0,
                -xAxis.dot(eye), -yAxis.dot(eye), -zAxis.dot(eye), 1
        );
    }

    // Создание перспективной проекционной матрицы (левосторонняя система координат)
    public static Matrix4f createPerspectiveMatrix(float fov, float aspect, float near, float far) {
        if (near <= 0 || far <= 0 || near >= far) {
            throw new IllegalArgumentException("Invalid near/far plane values: near=" + near + ", far=" + far);
        }

        float tanHalfFov = (float) Math.tan(fov / 2.0f);
        float range = far - near;

        return new Matrix4f(
                1.0f / (aspect * tanHalfFov), 0, 0, 0,
                0, 1.0f / tanHalfFov, 0, 0,
                0, 0, -(far + near) / range, -2 * far * near / range,
                0, 0, -1, 0
        );
    }

    // Создание ортографической проекционной матрицы
    public static Matrix4f createOrthographicMatrix(float left, float right,
                                                    float bottom, float top,
                                                    float near, float far) {
        if (left >= right || bottom >= top || near >= far) {
            throw new IllegalArgumentException("Invalid orthographic parameters");
        }

        return new Matrix4f(
                2.0f / (right - left), 0, 0, -(right + left) / (right - left),
                0, 2.0f / (top - bottom), 0, -(top + bottom) / (top - bottom),
                0, 0, -2.0f / (far - near), -(far + near) / (far - near),
                0, 0, 0, 1
        );
    }

    // Создание матрицы модели (T * R * S)
    public static Matrix4f createModelMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMatrix = createScaleMatrix(scale.x, scale.y, scale.z);
        Matrix4f rotX = createRotationXMatrix(rotation.x);
        Matrix4f rotY = createRotationYMatrix(rotation.y);
        Matrix4f rotZ = createRotationZMatrix(rotation.z);
        Matrix4f rotationMatrix = rotZ.multiply(rotY).multiply(rotX);
        Matrix4f translationMatrix = createTranslationMatrix(translation);

        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    // Преобразование в массив (удобно для OpenGL) - построчно
    public float[] toArray() {
        float[] array = new float[16];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                array[index++] = m[i][j];
            }
        }
        return array;
    }

    // Преобразование в транспонированный массив (для некоторых API) - по столбцам
    public float[] toArrayTransposed() {
        float[] array = new float[16];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                array[index++] = m[j][i];
            }
        }
        return array;
    }

    // Проверка на единичную матрицу
    public boolean isIdentity() {
        final float eps = 1e-6f;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float expected = (i == j) ? 1.0f : 0.0f;
                if (Math.abs(m[i][j] - expected) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix4f:\n" +
                        "[%8.4f, %8.4f, %8.4f, %8.4f]\n" +
                        "[%8.4f, %8.4f, %8.4f, %8.4f]\n" +
                        "[%8.4f, %8.4f, %8.4f, %8.4f]\n" +
                        "[%8.4f, %8.4f, %8.4f, %8.4f]",
                m[0][0], m[0][1], m[0][2], m[0][3],
                m[1][0], m[1][1], m[1][2], m[1][3],
                m[2][0], m[2][1], m[2][2], m[2][3],
                m[3][0], m[3][1], m[3][2], m[3][3]
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Matrix4f other = (Matrix4f) obj;
        final float eps = 1e-6f;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
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
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result = 31 * result + Float.floatToIntBits(m[i][j]);
            }
        }
        return result;
    }
}