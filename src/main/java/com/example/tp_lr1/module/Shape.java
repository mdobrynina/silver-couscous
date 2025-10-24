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
    public Color getColor() { return color; }

    public abstract void draw(GraphicsContext gc);

    // ФАБРИЧНЫЙ МЕТОД
    public static Shape create(String shapeType, double x, double y, double size, Color color) {
        switch (shapeType) {
            case "Квадрат": return new SquareShape(x, y, size, color);
            case "Треугольник": return new TriangleShape(x, y, size, color);
            case "Круг":
            default: return new CircleShape(x, y, size, color);
        }
    }

    // ХРАНИТЕЛЬ (MEMENTO)
    public static class ShapeMemento {
        public final String type;
        public final double x, y, size;
        public final Color color;

        public ShapeMemento(String type, double x, double y, double size, Color color) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }

    public ShapeMemento createMemento() {
        String type;
        if (this instanceof CircleShape) type = "Circle";
        else if (this instanceof SquareShape) type = "Square";
        else type = "Triangle";

        return new ShapeMemento(type, x, y, size, color);
    }

    public static Shape restoreFromMemento(ShapeMemento memento) {
        return create(memento.type, memento.x, memento.y, memento.size, memento.color);
    }
}