package com.example.tp_lr1.module;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SquareShape extends Shape {
    public SquareShape(double x, double y, double size, Color color) {
        super(x, y, size, color);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x - size / 2, y - size / 2, size, size);
    }
}