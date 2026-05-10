package game.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    // These variables link directly to your SceneBuilder buttons
    @FXML
    private Button btnStart;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnRules;
    @FXML
    private Button btnAboutUs;
    @FXML
    private StackPane mainStackPane; // Linked to your welcome.fxml root
    
    public static MediaPlayer backgroundPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	try {
            // 1. Grab the exact path to your MP3 file
            // Make sure the filename matches yours perfectly!
            String musicFile = getClass().getResource("/resources/music/Intro.mp3").toExternalForm();
            
            // 2. Load it into the JavaFX Media engine
            Media sound = new Media(musicFile);
            backgroundPlayer = new MediaPlayer(sound);
            
            // 3. Set it to loop forever and press play!
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); 
            backgroundPlayer.play();
            
        } catch (Exception e) {
            System.out.println("Could not find the music file!");
            e.printStackTrace();
        }
        
        btnStart.setOnAction(event -> {
            System.out.println("Start button clicked! Time to pick a monster.");
        });

        btnSettings.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/popup_settings.fxml"));
                Parent popup = loader.load();
                
                // Set initial state (invisible and shrunk down)
                popup.setOpacity(0);
                popup.setScaleX(0.8);
                popup.setScaleY(0.8);
                
                // Add to the screen
                mainStackPane.getChildren().add(popup);
                
                // 1. Create the Fade-In
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popup);
                fadeIn.setToValue(1.0);
                
                // 2. Create the Scale-Up (Zoom)
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), popup);
                scaleUp.setToX(1.0);
                scaleUp.setToY(1.0);
                
                // 3. Play them together
                ParallelTransition appearAnimation = new ParallelTransition(fadeIn, scaleUp);
                appearAnimation.play();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        btnRules.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/popup_rules.fxml"));
                Parent popup = loader.load();
                
                // Set initial state (invisible and shrunk down)
                popup.setOpacity(0);
                popup.setScaleX(0.8);
                popup.setScaleY(0.8);
                
                // Add to the screen
                mainStackPane.getChildren().add(popup);
                
                // 1. Create the Fade-In
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popup);
                fadeIn.setToValue(1.0);
                
                // 2. Create the Scale-Up (Zoom)
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), popup);
                scaleUp.setToX(1.0);
                scaleUp.setToY(1.0);
                
                // 3. Play them together
                ParallelTransition appearAnimation = new ParallelTransition(fadeIn, scaleUp);
                appearAnimation.play();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        btnAboutUs.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/popup_about.fxml"));
                Parent popup = loader.load();
                
                // Set initial state (invisible and shrunk down)
                popup.setOpacity(0);
                popup.setScaleX(0.8);
                popup.setScaleY(0.8);
                
                // Add to the screen
                mainStackPane.getChildren().add(popup);
                
                // 1. Create the Fade-In
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popup);
                fadeIn.setToValue(1.0);
                
                // 2. Create the Scale-Up (Zoom)
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), popup);
                scaleUp.setToX(1.0);
                scaleUp.setToY(1.0);
                
                // 3. Play them together
                ParallelTransition appearAnimation = new ParallelTransition(fadeIn, scaleUp);
                appearAnimation.play();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
    }
}