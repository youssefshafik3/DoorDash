package game.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
    	Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Poppin-Regular.ttf"), 14);
    	Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Gang Bangers.ttf"), 14);
    	Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Monster AG.ttf"), 14);
        // 1. Load the Welcome Screen FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/welcome.fxml"));
        Parent root = loader.load();

        // 2. Set the Scene (Window size: 1024x768 is usually good for a 100-cell board game)
        Scene scene = new Scene(root, 1024, 768);

        // 3. Configure and show the main window
        primaryStage.setTitle("DoorDash: Scare vs Laugh Touchdown");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Keeps your board from getting stretched later
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}