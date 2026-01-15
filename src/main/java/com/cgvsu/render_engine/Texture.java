package com.cgvsu.render_engine;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class Texture {
    private Image image;
    private int width, height;
    private PixelReader pixelReader;

    public Texture(String filePath) {
        this.image = new Image("file:" + filePath);
        this.width = (int)image.getWidth();
        this.height = (int)image.getHeight();
        this.pixelReader = image.getPixelReader();
    }

    public javafx.scene.paint.Color getColor(float u, float v) {
        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);

        int x = Math.max(0, Math.min(width - 1, (int)(u * width)));
        int y = Math.max(0, Math.min(height - 1, (int)((1 - v) * height))); // Инвертируем v

        return pixelReader.getColor(x, y);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isLoaded() { return image != null; }
}
