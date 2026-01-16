package com.cgvsu.render_engine;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.File;

public class Texture {
    private Image image;
    private int width, height;
    private PixelReader pixelReader;

    public Texture(String filePath) {
        try {
            File file = new File(filePath);
            this.image = new Image(file.toURI().toString());

            if (image.isError()) {
                System.err.println("Texture load error: " + image.getException());
                return;
            }

            this.width = (int)image.getWidth();
            this.height = (int)image.getHeight();
            this.pixelReader = image.getPixelReader();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Color getColor(float u, float v) {
        if (pixelReader == null) return Color.WHITE;

        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));

        int x = (int)(u * (width - 1));
        int y = (int)(v * (height - 1));

        return pixelReader.getColor(x, y);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isLoaded() { return pixelReader != null; }
}