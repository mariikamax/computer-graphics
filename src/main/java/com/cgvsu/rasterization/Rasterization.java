package com.cgvsu.rasterization;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import static java.lang.Math.*;

public class Rasterization {

    public static void drawRectangle(
            final GraphicsContext graphicsContext,
            final int x, final int y,
            final int width, final int height,
            final Color color) {
        final PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        for (int row = y; row < y + height; ++row)
            for (int col = x; col < x + width; ++col)
                pixelWriter.setColor(col, row, color);
    }

    public static void drawLineBresenham(PixelWriter pixelWriter, int x0, int y0, int x1, int y1) {
        if (y1 < y0) {
            int tmp = y0;
            y0 = y1;
            y1 = tmp;

            tmp = x0;
            x0 = x1;
            x1 = tmp;
        }
        BorderIterator iterator = new BresenhamBorderIterator(x0, y0, x1, y1);
        while (iterator.hasNext()) {
            int x = iterator.getX();
            int y = iterator.getY();
            pixelWriter.setColor(x, y, Color.BLACK);
            iterator.next();
        }
    }

    /**
     * Классический алгоритм Брезенхейма. Для каждого y из отрезка на входе находит для него x.
     */
    public static int[] interpolateBresenham(int x0, int y0, int x1, int y1) {
        int sizeOut = Math.abs(y1 - y0) + 1;
        boolean change = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        boolean changeDirection = false;
        if (change) {
            int tmp = y0;
            y0 = x0;
            x0 = tmp;

            tmp = y1;
            y1 = x1;
            x1 = tmp;
        }

        if (x1 < x0) {
            changeDirection = true;
            int tmp = x1;
            x1 = x0;
            x0 = tmp;

            tmp = y1;
            y1 = y0;
            y0 = tmp;
        }

        int[] values = new int[sizeOut];
        int dx = x1 - x0;
        int dy = y1 - y0;
        int step = dy < 0 ? -1 : 1;
        int error = 0;
        int y = y0;
        int index = 0;
        for (int x = x0; x <= x1; x++) {
            if (change)
                values[index++] = y;
            error += Math.abs(2 * dy);
            if (error > dx) {
                if (!change) values[index++] = changeDirection ? x1 - (x - x0) : x;
                y += step;
                error = -(2 * dx - error);
            }
        }
        return values;
    }

    /**
     * Метод для растеризации треугольника с использованием идеи scanline и нахождения границ через алгоритм
     * Брезенхейма, реализованный в виде итератора по границам треугольника.
     */
    public static void drawTriangleByIterator(
            final PixelWriter pixelWriter,
            int x0, int y0,
            int x1, int y1,
            int x2, int y2,
            Color color
    ) {
        int tmp;
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;
        }
        if (y1 > y2) {
            tmp = y2;
            y2 = y1;
            y1 = tmp;

            tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;
        }
        BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1);
        BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2);
        BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2);

        while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
            int y = borderIterator1.getY();
            int leftX = min(borderIterator1.getX(), borderIterator2.getX());
            int rightX = max(borderIterator1.getX(), borderIterator2.getX());
            for (int x = leftX; x <= rightX; x++) {
                pixelWriter.setColor(x, y, color);
            }
            borderIterator1.next();
            borderIterator2.next();
        }

        while (borderIterator3.hasNext() && borderIterator2.hasNext()) {
            int y = borderIterator2.getY();
            int leftX = min(borderIterator2.getX(), borderIterator3.getX());
            int rightX = max(borderIterator2.getX(), borderIterator3.getX());
            for (int x = leftX; x <= rightX; x++) {
                pixelWriter.setColor(x, y, color);
            }
            borderIterator2.next();
            borderIterator3.next();
        }
    }
}
