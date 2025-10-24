package com.example.tp_lr1;

import com.example.tp_lr1.module.*;
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
import javafx.stage.FileChooser;

import java.io.*;
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
    @FXML private Button saveButton;
    @FXML private Button openButton;

    private GraphicsContext gc;
    private final List<Shape> shapes = new ArrayList<>();
    private final List<Shape.ShapeMemento> shapeMementos = new ArrayList<>();
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
                Shape s = Shape.create(shapeComboBox.getValue(), e.getX(), e.getY(), sizeSlider.getValue(), colorPicker.getValue());
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
        Shape shape = Shape.create(shapeComboBox.getValue(), x, y, size, color);
        shapes.add(shape);
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
            return dist < shape.getSize();
        });
        redrawCanvas();
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

    @FXML
    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить рисунок");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Shape shape : shapes) {
                    Shape.ShapeMemento memento = shape.createMemento();
                    writer.println(memento.type);
                    writer.printf("%.2f%n", memento.x);
                    writer.printf("%.2f%n", memento.y);
                    writer.printf("%.2f%n", memento.size);
                    writer.printf("%.6f%n", memento.color.getRed());
                    writer.printf("%.6f%n", memento.color.getGreen());
                    writer.printf("%.6f%n", memento.color.getBlue());
                    writer.printf("%.6f%n", memento.color.getOpacity());
                    writer.println();
                }
                showAlert("Успех", "Рисунок сохранен! Фигур: " + shapes.size());
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось сохранить файл: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть рисунок");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                shapes.clear();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String type = line;
                    double x = parseNumber(reader.readLine());
                    double y = parseNumber(reader.readLine());
                    double size = parseNumber(reader.readLine());
                    double red = parseNumber(reader.readLine());
                    double green = parseNumber(reader.readLine());
                    double blue = parseNumber(reader.readLine());
                    double alpha = parseNumber(reader.readLine());

                    Color color = new Color(red, green, blue, alpha);
                    Shape shape = Shape.create(type, x, y, size, color);
                    shapes.add(shape);

                    reader.readLine();
                }

                redrawCanvas();
                showAlert("Успех", "Рисунок загружен! Фигур: " + shapes.size());
            } catch (Exception e) {
                showAlert("Ошибка", "Ошибка: " + e.getMessage());
            }
        }
    }

    private double parseNumber(String text) {
        if (text == null) throw new RuntimeException("Пустая строка числа");
        return Double.parseDouble(text.trim().replace(',', '.'));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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