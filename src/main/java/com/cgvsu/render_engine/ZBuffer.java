package com.cgvsu.render_engine;

public class ZBuffer {
    private float[][] buffer;
    private int width, height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new float[height][width];
        clear();
    }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = Float.MAX_VALUE;
            }
        }
    }

    public boolean testAndSet(int x, int y, float z) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        if (z < buffer[y][x]) {
            buffer[y][x] = z;
            return true;
        }
        return false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}