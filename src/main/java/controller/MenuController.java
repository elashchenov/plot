package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import utils.Point;
import utils.manager.FileChooserManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MenuController {
    @FXML
    public MenuItem openFileMenuItem;
    @FXML
    public MenuItem saveFileMenuItem;

    @FXML
    public void openFileMenu() {
        File file = FileChooserManager.openFile(Main.getStage());
        if (file == null) {
            return;
        }

        ObservableList<Point> ellipsePoints = MainController.getEllipsePoints();
        ObservableList<Point> points = MainController.getPoints();
        try (Scanner scan = new Scanner(file)) {
            //Читаем две точки эллипса
            ellipsePoints.setAll(
                    new Point(readDouble(scan), readDouble(scan)),
                    new Point(readDouble(scan), readDouble(scan))
            );

            //Читаем множество точек
            while (scan.hasNextDouble()) {
                points.add(new Point(readDouble(scan), readDouble(scan)));
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка при чтении", "Файл не был открыт или неверный формат файла");
            e.printStackTrace();
        }
    }

    @FXML
    public void saveFileMenu() {
        File file = FileChooserManager.saveFile(Main.getStage());

        if (file == null) {
            return;
        }

        ObservableList<Point> ellipsePoints = MainController.getEllipsePoints();
        ObservableList<Point> points = MainController.getPoints();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(ellipsePoints.get(0).getX() + " " + ellipsePoints.get(0).getY());
            writer.println(ellipsePoints.get(1).getX() + " " + ellipsePoints.get(1).getY());
            for (Point point : points) {
                writer.println(point.getX() + " " + point.getY());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Считываем Double из файла
     * @param scan сканнер, к которому привязан файл
     * @throws IOException - при неудачном считывании
     */
    private Double readDouble(Scanner scan) throws IOException {
        if (scan.hasNextDouble()) {
            return scan.nextDouble();
        } else {
            throw new IOException("Wrong file format");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void showErrorDialog(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeight(200);
        alert.setResizable(true);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(text);
        alert.showAndWait();
    }

    /**
     * Загрузка окна О программе
     */
    @FXML
    private void showHelp() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Help.fxml"));
        AnchorPane root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("О программе");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
