package com.cgvsu.math;

// Это заготовка для собственной библиотеки для работы с линейной алгеброй
public class Vector3f {
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Vector3f other) {
        // todo: желательно, чтобы это была глобальная константа
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps && Math.abs(z - other.z) < eps;
    }

    public float x, y, z;

    public Vector3f normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length > 0.00001f) {
            x /= length;
            y /= length;
            z /= length;
        }
        return this;
    }

    public Vector3f add(Vector3f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vector3f multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public static Vector3f crossProduct(Vector3f a, Vector3f b) {
        return new Vector3f(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
        );
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
