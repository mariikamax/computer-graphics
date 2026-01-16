package com.cgvsu.ui;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;

public class FileLoader {
    public static File showOpenDialog(Window parent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть 3D модель");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );
        return fileChooser.showOpenDialog(parent);
    }

    public static File showSaveDialog(Window parent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить модель");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );
        return fileChooser.showSaveDialog(parent);
    }
}