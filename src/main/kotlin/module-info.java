module org.example.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires com.almasb.fxgl.all;

    opens org.example.javafx to javafx.fxml;
    exports org.example.javafx;
}