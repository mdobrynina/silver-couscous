package com.example.tp_lr1;

import com.example.tp_lr1.module.CircleShape;
import com.example.tp_lr1.module.Shape;
import com.example.tp_lr1.module.SquareShape;
import com.example.tp_lr1.module.TriangleShape;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML private AnchorPane rootPane;
    @FXML private Canvas mainCanvas;
    @FXML private ComboBox<String> shapeComboBox;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider sizeSlider;
    @FXML private Button clearButton;
    @FXML private Button eraserButton;
    @FXML private Label coordsLabel;
    @FXML private ToggleButton brushToggle;

    private GraphicsContext gc;
    private final List<Shape> shapes = new ArrayList<>();
    private boolean isEraserMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        shapeComboBox.getItems().addAll("Круг", "Квадрат", "Треугольник");
        shapeComboBox.setValue("Круг");
        sizeSlider.setValue(20);
        colorPicker.setValue(Color.BLUE);
        brushToggle.setSelected(true);

        gc = mainCanvas.getGraphicsContext2D();


        double left = AnchorPane.getLeftAnchor(mainCanvas) != null ? AnchorPane.getLeftAnchor(mainCanvas) : 0.0;
        double right = AnchorPane.getRightAnchor(mainCanvas) != null ? AnchorPane.getRightAnchor(mainCanvas) : 0.0;
        double top = AnchorPane.getTopAnchor(mainCanvas) != null ? AnchorPane.getTopAnchor(mainCanvas) : 0.0;
        double bottom = AnchorPane.getBottomAnchor(mainCanvas) != null ? AnchorPane.getBottomAnchor(mainCanvas) : 0.0;

        DoubleBinding widthBind = rootPane.widthProperty().subtract(left + right);
        DoubleBinding heightBind = rootPane.heightProperty().subtract(top + bottom);
        mainCanvas.widthProperty().bind(widthBind);
        mainCanvas.heightProperty().bind(heightBind);

        mainCanvas.widthProperty().addListener((o, oldV, newV) -> redrawCanvas());
        mainCanvas.heightProperty().addListener((o, oldV, newV) -> redrawCanvas());


        mainCanvas.setOnMousePressed(this::onMousePressed);
        mainCanvas.setOnMouseDragged(this::onMouseDragged);
        mainCanvas.setOnMouseMoved(this::onMouseMoved);

        Platform.runLater(this::clearCanvas);
    }


    private void onMousePressed(MouseEvent e) {
        updateCoords(e.getX(), e.getY());
        if (brushToggle.isSelected()) {
            if (isEraserMode) addEraserDot(e.getX(), e.getY());
            else addBrushDot(e.getX(), e.getY());
        } else {

            if (isEraserMode) removeShapeAt(e.getX(), e.getY());
            else {
                Shape s = createShape(shapeComboBox.getValue(), e.getX(), e.getY(), sizeSlider.getValue(), colorPicker.getValue());
                shapes.add(s);
                redrawCanvas();
            }
        }
    }

    private void onMouseDragged(MouseEvent e) {
        updateCoords(e.getX(), e.getY());
        if (brushToggle.isSelected()) {
            if (isEraserMode) addEraserDot(e.getX(), e.getY());
            else addBrushDot(e.getX(), e.getY());
        } else {
        }
    }

    private void onMouseMoved(MouseEvent e) {
        updateCoords(e.getX(), e.getY());
    }

    private void updateCoords(double x, double y) {
        coordsLabel.setText(String.format("X=%.0f Y=%.0f", x, y));
    }

    private void addBrushDot(double x, double y) {
        double size = sizeSlider.getValue();
        Color color = colorPicker.getValue();
        shapes.add(new CircleShape(x, y, size, color));
        redrawCanvas();
    }

    private void addEraserDot(double x, double y) {
        double size = sizeSlider.getValue();
        shapes.add(new CircleShape(x, y, size, Color.WHITE));
        redrawCanvas();
    }

    private void removeShapeAt(double x, double y) {
        shapes.removeIf(shape -> {
            double dx = shape.getX() - x;
            double dy = shape.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            return dist < shape.getSize(); // порог — размер фигуры
        });
        redrawCanvas();
    }

    private Shape createShape(String shapeType, double x, double y, double size, Color color) {
        switch (shapeType) {
            case "Квадрат": return new SquareShape(x, y, size, color);
            case "Треугольник": return new TriangleShape(x, y, size, color);
            case "Круг":
            default: return new CircleShape(x, y, size, color);
        }
    }

    @FXML
    private void handleClear() {
        clearCanvas();
    }

    @FXML
    private void handleEraser() {
        isEraserMode = !isEraserMode;
        eraserButton.setStyle(isEraserMode ? "-fx-background-color: #ff4444;" : "");
    }

    private void clearCanvas() {
        shapes.clear();
        redrawCanvas();
    }

    private void redrawCanvas() {
        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        for (Shape s : shapes) {
            s.draw(gc);
        }
    }
}