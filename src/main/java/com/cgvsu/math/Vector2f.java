package com.cgvsu.math;

/**
 * Двумерный вектор с основными математическими операциями
 */
public class Vector2f {
    public float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f other) {
        this.x = other.x;
        this.y = other.y;
    }

    // Основные операции
    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f divide(float scalar) {
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    // Скалярное произведение
    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    // Длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    // Нормализация
    public Vector2f normalized() {
        float len = length();
        if (len == 0) return new Vector2f(0, 0);
        return new Vector2f(x / len, y / len);
    }

    public void normalize() {
        float len = length();
        if (len != 0) {
            x /= len;
            y /= len;
        }
    }

    // Равенство с учетом погрешности
    public boolean equals(Vector2f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }

    @Override
    public String toString() {
        return String.format("Vector2f(%.3f, %.3f)", x, y);
    }
}