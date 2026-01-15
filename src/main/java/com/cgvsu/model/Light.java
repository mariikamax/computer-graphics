package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f color;
    private float intensity;

    public enum LightType {
        DIRECTIONAL,
        POINT,
        SPOT
    }

    private LightType type = LightType.POINT;

    public Light() {
        this.position = new Vector3f(0, 100, 100);
        this.color = new Vector3f(1, 1, 1);
        this.intensity = 1.0f;
        this.type = LightType.POINT;
    }

    public Light(Vector3f position, Vector3f color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
        this.type = LightType.POINT;
    }

    public Light(Vector3f position, Vector3f color, float intensity, LightType type) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
        this.type = type;
    }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Vector3f getColor() { return color; }
    public void setColor(Vector3f color) { this.color = color; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }

    public LightType getType() { return type; }
    public void setType(LightType type) { this.type = type; }

    public Vector3f getDirectionTo(Vector3f point) {
        Vector3f direction = new Vector3f(
                position.x - point.x,
                position.y - point.y,
                position.z - point.z
        );
        return direction.normalize();
    }

    public float getDistanceTo(Vector3f point) {
        float dx = position.x - point.x;
        float dy = position.y - point.y;
        float dz = position.z - point.z;
        return (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}