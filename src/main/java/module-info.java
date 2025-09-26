module com.example.tp_lr1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tp_lr1 to javafx.fxml;
    exports com.example.tp_lr1;
}