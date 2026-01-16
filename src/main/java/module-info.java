module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;

    exports com.cgvsu;
    exports com.cgvsu.model;
    exports com.cgvsu.math;
    exports com.cgvsu.objreader;
    exports com.cgvsu.objwriter;
    exports com.cgvsu.render_engine;
    exports com.cgvsu.ui;

    opens com.cgvsu to javafx.fxml;
}