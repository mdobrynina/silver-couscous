package com.example.tp_lr1.module;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Shape {
    protected double x, y, size;
    public Color color;

    public Shape(double x, double y, double size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public Color getColor() { return color; }

    public abstract void draw(GraphicsContext gc);

    public static Shape create(String shapeType, double x, double y, double size, Color color) {
        switch (shapeType) {
            case "Квадрат":
            case "Square":
                return new SquareShape(x, y, size, color);
            case "Треугольник":
            case "Triangle":
                return new TriangleShape(x, y, size, color);
            case "Круг":
            case "Circle":
            default:
                return new CircleShape(x, y, size, color);
        }
    }

    public static Shape createPolygon(int numberOfSides, double x, double y, double size, Color color) {
        switch (numberOfSides) {
            case 3:
                return new TriangleShape(x, y, size, color);
            case 4:
                return new SquareShape(x, y, size, color);
            case 0:
            default:
                return new CircleShape(x, y, size, color);
        }
    }
}