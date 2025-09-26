package com.example.tp_lr1.module;

import com.example.tp_lr1.module.Shape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TriangleShape extends Shape {
    public TriangleShape(double x, double y, double size, Color color) {
        super(x, y, size, color);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        double[] xPoints = {x, x - size / 2, x + size / 2};
        double[] yPoints = {y - size / 2, y + size / 2, y + size / 2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }
}