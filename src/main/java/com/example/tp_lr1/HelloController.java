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
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            // Можно добавить функционал для перетаскивания фигур если нужно
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

        Shape shape = createShape(shapeComboBox.getValue(), x, y, size, color);
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

    @FXML
    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить рисунок");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Устанавливаем локаль с точкой как разделителем
                writer.println("// Drawing File - Use dot as decimal separator");
                writer.println();

                for (Shape shape : shapes) {
                    String type;
                    if (shape instanceof SquareShape) {
                        type = "Square";
                    } else if (shape instanceof TriangleShape) {
                        type = "Triangle";
                    } else if (shape instanceof CircleShape) {
                        type = "Circle";
                    } else {
                        type = "Circle";
                    }

                    writer.println(type);
                    writer.printf(Locale.US, "%.2f%n", shape.getX());
                    writer.printf(Locale.US, "%.2f%n", shape.getY());
                    writer.printf(Locale.US, "%.2f%n", shape.getSize());
                    writer.printf(Locale.US, "%.6f%n", shape.getColor().getRed());
                    writer.printf(Locale.US, "%.6f%n", shape.getColor().getGreen());
                    writer.printf(Locale.US, "%.6f%n", shape.getColor().getBlue());
                    writer.printf(Locale.US, "%.6f%n", shape.getColor().getOpacity());
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
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                shapes.clear();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // Определяем тип фигуры
                    String type;
                    if (line.equals("Square") || line.equals("Квадрат")) {
                        type = "Square";
                    } else if (line.equals("Triangle") || line.equals("Треугольник")) {
                        type = "Triangle";
                    } else if (line.equals("Circle") || line.equals("Круг")) {
                        type = "Circle";
                    } else {
                        continue;
                    }

                    // Читаем остальные параметры с обработкой запятых
                    double x = parseNumber(reader.readLine());
                    double y = parseNumber(reader.readLine());
                    double size = parseNumber(reader.readLine());
                    double red = parseNumber(reader.readLine());
                    double green = parseNumber(reader.readLine());
                    double blue = parseNumber(reader.readLine());
                    double alpha = parseNumber(reader.readLine());

                    Color color = new Color(red, green, blue, alpha);
                    Shape shape = createShape(type, x, y, size, color);
                    shapes.add(shape);

                    // Пропускаем пустую строку если есть
                    reader.readLine();
                }

                redrawCanvas();
                showAlert("Успех", "Рисунок загружен! Фигур: " + shapes.size());
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось загрузить файл: " + e.getMessage());
            } catch (Exception e) {
                showAlert("Ошибка", "Ошибка в данных: " + e.getMessage());
            }
        }
    }

    // Метод для преобразования чисел с запятыми в точки
    private double parseNumber(String text) {
        if (text == null) throw new RuntimeException("Пустая строка числа");
        text = text.trim();
        // Заменяем запятые на точки для корректного парсинга
        text = text.replace(',', '.');
        return Double.parseDouble(text);
    }

    private double readDouble(BufferedReader reader, int lineNum) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Неожиданный конец файла на строке " + lineNum);
        }
        line = line.trim();
        System.out.println("Строка " + lineNum + " (число): " + line); // Отладочный вывод
        return Double.parseDouble(line);
    }

    // Вспомогательный метод для чтения double
    private double readDouble(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) throw new IOException("Неожиданный конец файла");
        return Double.parseDouble(line.trim());
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

    // Внутренний класс для сериализации данных фигур
    private static class ShapeData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String type;
        private final double x, y, size;
        private final double red, green, blue, alpha;

        public ShapeData(Shape shape) {
            this.x = shape.getX();
            this.y = shape.getY();
            this.size = shape.getSize();
            this.red = shape.color.getRed();
            this.green = shape.color.getGreen();
            this.blue = shape.color.getBlue();
            this.alpha = shape.color.getOpacity();

            // Определяем тип фигуры
            if (shape instanceof CircleShape) {
                this.type = "CIRCLE";
            } else if (shape instanceof SquareShape) {
                this.type = "SQUARE";
            } else if (shape instanceof TriangleShape) {
                this.type = "TRIANGLE";
            } else {
                this.type = "CIRCLE";
            }
        }

        public Shape toShape() {
            Color color = new Color(red, green, blue, alpha);
            switch (type) {
                case "SQUARE": return new SquareShape(x, y, size, color);
                case "TRIANGLE": return new TriangleShape(x, y, size, color);
                case "CIRCLE":
                default: return new CircleShape(x, y, size, color);
            }
        }
    }
}