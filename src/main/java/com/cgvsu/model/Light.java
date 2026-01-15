package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f color;
    private float intensity;

    public Light() {
        this.position = new Vector3f(0, 100, 100);
        this.color = new Vector3f(1, 1, 1);
        this.intensity = 1.0f;
    }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Vector3f getColor() { return color; }
    public void setColor(Vector3f color) { this.color = color; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
}
