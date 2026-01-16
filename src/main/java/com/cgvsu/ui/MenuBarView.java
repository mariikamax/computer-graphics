package com.cgvsu.ui;

import javafx.scene.control.*;

public class MenuBarView extends MenuBar {
    private final MenuItem openMenuItem;
    private final MenuItem saveMenuItem;
    private final MenuItem exitMenuItem;
    private final RadioMenuItem lightThemeItem;
    private final RadioMenuItem darkThemeItem;

    public MenuBarView() {
        Menu fileMenu = new Menu("Файл");
        openMenuItem = new MenuItem("Открыть модель");
        saveMenuItem = new MenuItem("Сохранить модель");
        exitMenuItem = new MenuItem("Выход");
        fileMenu.getItems().addAll(openMenuItem, saveMenuItem, new SeparatorMenuItem(), exitMenuItem);

        Menu viewMenu = new Menu("Вид");

        Menu themeMenu = new Menu("Тема интерфейса");

        ToggleGroup themeGroup = new ToggleGroup();

        lightThemeItem = new RadioMenuItem("Светлая тема");
        lightThemeItem.setToggleGroup(themeGroup);
        lightThemeItem.setSelected(true); // По умолчанию светлая тема

        darkThemeItem = new RadioMenuItem("Темная тема");
        darkThemeItem.setToggleGroup(themeGroup);

        themeMenu.getItems().addAll(lightThemeItem, darkThemeItem);
        viewMenu.getItems().add(themeMenu);

        Menu helpMenu = new Menu("Справка");
        MenuItem aboutItem = new MenuItem("О программе");
        helpMenu.getItems().add(aboutItem);

        this.getMenus().addAll(fileMenu, viewMenu, helpMenu);
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getExitMenuItem() {
        return exitMenuItem;
    }

    public RadioMenuItem getLightThemeItem() {
        return lightThemeItem;
    }

    public RadioMenuItem getDarkThemeItem() {
        return darkThemeItem;
    }
}