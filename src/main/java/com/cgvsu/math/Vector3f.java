package com.cgvsu.math;

/**
 * Трехмерный вектор с основными математическими операциями
 */
public class Vector3f {
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    // Основные операции
    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3f divide(float scalar) {
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    // Скалярное произведение
    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    // Векторное произведение
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    // Длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    // Квадрат длины (для оптимизации)
    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    // Нормализация
    public Vector3f normalized() {
        float len = length();
        if (len == 0) return new Vector3f(0, 0, 0);
        return new Vector3f(x / len, y / len, z / len);
    }

    public void normalize() {
        float len = length();
        if (len != 0) {
            x /= len;
            y /= len;
            z /= len;
        }
    }

    // Равенство с учетом погрешности
    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps;
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.3f, %.3f, %.3f)", x, y, z);
    }
}
