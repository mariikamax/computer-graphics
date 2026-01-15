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

            System.out.println("Texture loaded: " + width + "x" + height + " from " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Color getColor(float u, float v) {
        if (pixelReader == null) return Color.WHITE;

        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);

        int x = Math.max(0, Math.min(width - 1, (int)(u * width)));
        int y = Math.max(0, Math.min(height - 1, (int)(v * height)));

        return pixelReader.getColor(x, y);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isLoaded() { return pixelReader != null; }
}