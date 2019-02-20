package utils.manager;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileChooserManager {

    private static FileChooser fileChooser;

    public static File openFile(Stage stage) {
        initFileChooser("Открытие");
        return fileChooser.showOpenDialog(stage);
    }

    public static File saveFile(Stage stage) {
        initFileChooser("Сохранение");
        return fileChooser.showSaveDialog(stage);
    }

    private static void initFileChooser(String title) {
        fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
    }
}
