package com.cgvsu.rasterization;

import static java.lang.Math.abs;

public class BresenhamBorderIterator implements BorderIterator {
    private int cntSteps;

    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final int dx;
    private final int dy;
    private final int step;
    private final boolean wasChangeY;
    private final boolean wasChangeX;

    private int currentX;
    private int currentY;
    private int error;

    public BresenhamBorderIterator(int x0, int y0, int x1, int y1) {
        cntSteps = Math.max(abs(y1 - y0), abs(x1 - x0)) + 1;
        wasChangeY = abs(y1 - y0) > abs(x1 - x0);
        if (wasChangeY) {
            int tmp = y0;
            y0 = x0;
            x0 = tmp;

            tmp = y1;
            y1 = x1;
            x1 = tmp;
        }
        wasChangeX = x1 < x0;
        if (wasChangeX) {
            int tmp = x1;
            x1 = x0;
            x0 = tmp;

            tmp = y1;
            y1 = y0;
            y0 = tmp;
        }

        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        dx = x1 - x0;
        dy = y1 - y0;
        step = dy > 0 ? 1 : -1;
        currentY = y0;
        currentX = x0;
        error = 0;
    }

    @Override
    public int getX() {
        if (!wasChangeY)
            return wasChangeX ? x1 - (currentX - x0) : currentX;
        return wasChangeX ? y1 - (currentY - y0) : currentY;
    }

    @Override
    public int getY() {
        if (wasChangeY)
            return wasChangeX ? x1 - (currentX - x0) : currentX;
        return wasChangeX ? y1 - (currentY - y0) : currentY;
    }

    public void next() {
        error += abs(2 * dy);
        currentX++;
        if (error > dx) {
            currentY += step;
            error = -(2 * dx - error);
        }
    }

    @Override
    public boolean hasNext() {
        return currentX != x1 || currentY != y1;
    }
}