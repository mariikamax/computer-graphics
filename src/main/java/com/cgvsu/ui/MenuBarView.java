package com.cgvsu.ui;

import javafx.scene.control.*;

public class MenuBarView extends MenuBar {
    private MenuItem openMenuItem;
    private MenuItem saveMenuItem;
    private MenuItem exitMenuItem;

    public MenuBarView() {
        Menu fileMenu = new Menu("Файл");
        openMenuItem = new MenuItem("Открыть модель");
        saveMenuItem = new MenuItem("Сохранить модель");
        exitMenuItem = new MenuItem("Выход");
        fileMenu.getItems().addAll(openMenuItem, saveMenuItem, new SeparatorMenuItem(), exitMenuItem);

        Menu viewMenu = new Menu("Вид");

        Menu helpMenu = new Menu("Справка");
        MenuItem aboutItem = new MenuItem("О программе");
        helpMenu.getItems().add(aboutItem);

        this.getMenus().addAll(fileMenu, helpMenu);
    }

    public MenuItem getOpenMenuItem() { return openMenuItem; }
    public MenuItem getSaveMenuItem() { return saveMenuItem; }
    public MenuItem getExitMenuItem() { return exitMenuItem; }
}