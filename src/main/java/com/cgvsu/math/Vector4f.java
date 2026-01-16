package com.cgvsu.math;

/**
 * Четырехмерный вектор (однородные координаты)
 */
public class Vector4f {
    public float x, y, z, w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector3f v, float w) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = w;
    }

    public Vector4f(Vector4f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }

    // Приведение к трехмерному вектору (деление на w)
    public Vector3f toVector3f() {
        if (w == 0) return new Vector3f(x, y, z);
        return new Vector3f(x / w, y / w, z / w);
    }

    // Нормализация (деление на w)
    public Vector4f normalized() {
        if (w == 0 || w == 1) return new Vector4f(this);
        return new Vector4f(x / w, y / w, z / w, 1.0f);
    }

    public void normalize() {
        if (w != 0 && w != 1) {
            x /= w;
            y /= w;
            z /= w;
            w = 1.0f;
        }
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%.3f, %.3f, %.3f, %.3f)", x, y, z, w);
    }
}