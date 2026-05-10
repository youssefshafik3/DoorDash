package game.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PopupAboutController {

    @FXML
    private Button btnClose;
    
    @FXML
    private StackPane popupOverlay;

    @FXML
    public void initialize() {
        // This grabs the window the button is sitting in, and closes it
    	btnClose.setOnAction(event -> {
    	    // 1. Create the Fade-Out
    	    FadeTransition fadeOut = new FadeTransition(Duration.millis(250), popupOverlay);
    	    fadeOut.setToValue(0);
    	    
    	    // 2. Create the Scale-Down
    	    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), popupOverlay);
    	    scaleDown.setToX(0.8);
    	    scaleDown.setToY(0.8);
    	    
    	    // 3. Play them together
    	    ParallelTransition disappearAnimation = new ParallelTransition(fadeOut, scaleDown);
    	    
    	    // 4. CRUCIAL: Remove the popup ONLY after the animation finishes
    	    disappearAnimation.setOnFinished(e -> {
    	        StackPane parent = (StackPane) popupOverlay.getParent();
    	        if (parent != null) {
    	            parent.getChildren().remove(popupOverlay);
    	        }
    	    });
    	    
    	    disappearAnimation.play();
    	});
    }
}