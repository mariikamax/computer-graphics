package com.cgvsu.rasterization;


/**
 * Интерфейс, который позволяет получить доступ к координатам x и y во время каждой из итераций.
 * Позволяет перебирать координаты некоторого координатного пути.
 */
public interface BorderIterator {
    int getX();

    int getY();

    void next();

    boolean hasNext();
}