package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f color;
    private float intensity;
    private LightType type;

    public enum LightType {
        DIRECTIONAL,
        POINT,
        SPOT
    }

    public Light() {
        this.position = new Vector3f(0, 100, 100);
        this.color = new Vector3f(1, 1, 1);
        this.intensity = 1.0f;
        this.type = LightType.POINT;
    }

    // Вычисление освещенности по модели Фонга
    public Vector3f calculatePhongLighting(Vector3f point, Vector3f normal,
                                           Vector3f viewDir, Vector3f materialColor) {
        Vector3f lightDir;

        if (type == LightType.DIRECTIONAL) {
            lightDir = new Vector3f(position.x, position.y, position.z).normalize();
        } else {
            // Точечный источник
            lightDir = new Vector3f(
                    position.x - point.x,
                    position.y - point.y,
                    position.z - point.z
            ).normalize();
        }

        // Диффузная составляющая
        float diff = Math.max(Vector3f.dot(normal, lightDir), 0.0f);
        Vector3f diffuse = new Vector3f(
                color.x * diff * intensity,
                color.y * diff * intensity,
                color.z * diff * intensity
        );

        // Отраженная составляющая (упрощенно)
        Vector3f reflectDir = Vector3f.reflect(
                new Vector3f(-lightDir.x, -lightDir.y, -lightDir.z),
                normal
        );
        float spec = (float)Math.pow(Math.max(Vector3f.dot(viewDir, reflectDir), 0.0f), 32);
        Vector3f specular = new Vector3f(
                color.x * spec * intensity,
                color.y * spec * intensity,
                color.z * spec * intensity
        );

        // Комбинируем
        Vector3f result = new Vector3f(
                materialColor.x * (diffuse.x + specular.x + 0.1f), // 0.1f - ambient
                materialColor.y * (diffuse.y + specular.y + 0.1f),
                materialColor.z * (diffuse.z + specular.z + 0.1f)
        );

        // Ограничиваем значения [0, 1]
        result.x = Math.min(1.0f, Math.max(0.0f, result.x));
        result.y = Math.min(1.0f, Math.max(0.0f, result.y));
        result.z = Math.min(1.0f, Math.max(0.0f, result.z));

        return result;
    }
}