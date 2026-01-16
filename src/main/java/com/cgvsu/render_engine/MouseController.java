package com.cgvsu.render_engine;

import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import com.cgvsu.math.Vector3f;

/**
 * Контроллер для управления камерой с помощью мыши
 */
public class MouseController {
    private Camera camera;
    private Scene scene;

    // Состояние мыши
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isRightButtonPressed = false;
    private boolean isMiddleButtonPressed = false;

    // Чувствительность
    private float rotationSensitivity = 0.005f;
    private float panSensitivity = 0.01f;
    private float zoomSensitivity = 0.1f;

    public MouseController(Camera camera, Scene scene) {
        this.camera = camera;
        this.scene = scene;
        setupMouseListeners();
    }

    private void setupMouseListeners() {
        // Нажатие кнопки мыши
        scene.setOnMousePressed(this::onMousePressed);

        // Отпускание кнопки мыши
        scene.setOnMouseReleased(this::onMouseReleased);

        // Перетаскивание мыши
        scene.setOnMouseDragged(this::onMouseDragged);

        // Прокрутка колесика
        scene.setOnScroll(this::onMouseScrolled);
    }

    private void onMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();

        if (event.getButton() == MouseButton.SECONDARY) {
            isRightButtonPressed = true;
        } else if (event.getButton() == MouseButton.MIDDLE) {
            isMiddleButtonPressed = true;
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isRightButtonPressed = false;
        } else if (event.getButton() == MouseButton.MIDDLE) {
            isMiddleButtonPressed = false;
        }
    }

    private void onMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;

        if (isRightButtonPressed) {
            // Вращение камеры
            camera.rotate(
                    (float)(-deltaX * rotationSensitivity),
                    (float)(-deltaY * rotationSensitivity)
            );
        } else if (isMiddleButtonPressed) {
            // Панорамирование (движение камеры в плоскости)
            Vector3f forward = camera.getTarget().subtract(camera.getPosition()).normalized();
            Vector3f right = forward.cross(new Vector3f(0, 1, 0)).normalized();
            Vector3f up = right.cross(forward).normalized();

            Vector3f panMovement = right.multiply((float)(-deltaX * panSensitivity))
                    .add(up.multiply((float)(deltaY * panSensitivity)));

            camera.move(panMovement);
        }

        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void onMouseScrolled(javafx.scene.input.ScrollEvent event) {
        // Приближение/отдаление
        Vector3f forward = camera.getTarget().subtract(camera.getPosition()).normalized();
        float zoomAmount = (float)(event.getDeltaY() * zoomSensitivity);

        camera.move(forward.multiply(zoomAmount));
    }

    // Настройка чувствительности
    public void setRotationSensitivity(float sensitivity) {
        this.rotationSensitivity = sensitivity;
    }

    public void setPanSensitivity(float sensitivity) {
        this.panSensitivity = sensitivity;
    }

    public void setZoomSensitivity(float sensitivity) {
        this.zoomSensitivity = sensitivity;
    }
}