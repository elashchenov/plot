package main;

import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

public class Main extends Application {
    @Getter
    private static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;
        //load Main.fxml and MainController
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        stage.setTitle("Построение графика");
        Scene scene = new Scene(root);
        scene.setUserData(controller);
        scene.getStylesheets().add("mycss.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
