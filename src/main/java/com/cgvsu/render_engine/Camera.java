package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import javax.vecmath.Matrix4f;

public class Camera {

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio, // ДОБАВИТЬ
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio; // Теперь это параметр
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public void movePosition(final Vector3f translation) {
        this.position.x += translation.x;
        this.position.y += translation.y;
        this.position.z += translation.z;
    }

    public void moveTarget(final Vector3f translation) {
        this.target.x += translation.x;
        this.target.y += translation.y;
        this.target.z += translation.z;
    }

    Matrix4f getViewMatrix() {
        javax.vecmath.Vector3f eye = new javax.vecmath.Vector3f(position.x, position.y, position.z);
        javax.vecmath.Vector3f targetVec = new javax.vecmath.Vector3f(this.target.x, this.target.y, this.target.z);
        return GraphicConveyor.lookAt(eye, targetVec);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
}