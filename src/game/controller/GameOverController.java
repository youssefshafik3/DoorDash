package game.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class GameOverController {

    @FXML private Label lblWinner;
    @FXML private Label lblScarerEnergy;
    @FXML private Label lblLaugherEnergy;
    @FXML private Button btnMenu;

    /**
     * Injects the live game match scores into the popup window display labels.
     */
    public void setGameStats(String name, String role, int scarerEnergy, int laugherEnergy, String accentColor) {
        lblWinner.setText("🏆 Winner: " + name + " (" + role + ")");
        lblWinner.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        
        lblScarerEnergy.setText("⚡ Scarer Energy: " + scarerEnergy + " AP");
        lblLaugherEnergy.setText("🔋 Laugher Energy: " + laugherEnergy + " AP");
        
        // --- FIXED: Removed the inline structural style overrides ---
        // Instead of overriding the entire background, fonts, and borders, 
        // we just optionally pass the text color tint if you want it, or delete this line completely 
        // to let the .close-button CSS class have 100% control!
        btnMenu.setStyle("-fx-text-fill: #111625;"); 
    }

    @FXML
    void handleReturnToMenu(ActionEvent event) {
        // Close the modal popup overlay stage window context completely
        Stage popupStage = (Stage) btnMenu.getScene().getWindow();
        popupStage.close();
    }
}