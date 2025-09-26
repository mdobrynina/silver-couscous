package com.example.tp_lr1.module;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Shape {
    protected double x, y, size;
    protected Color color;

    public Shape(double x, double y, double size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }

    public abstract void draw(GraphicsContext gc);
}