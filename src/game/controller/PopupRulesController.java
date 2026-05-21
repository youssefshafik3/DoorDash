package game.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class PopupRulesController {

    @FXML
    private Button btnClose;
    
    @FXML
    private StackPane popupOverlay;

    @FXML
    private TextArea rulesTextArea; // INJECT THE TEXT AREA

    @FXML
    public void initialize() {
        
        // 1. SET THE GAME RULES TEXT
        String gameRules = 
            "DOOR DASH: SCARE VS LAUGH TOUCHDOWN\n\n" +
            "Welcome to the Floor! Monstropolis used to run exclusively on terrified shrieks. However, it has been discovered that laughter produces ten times more energy. Scarers and Laughers must now race across a 100-cell board to prove whose method is superior.\n\n" +
            
            "THE GOAL\n" +
            "Be the first monster to reach Boo's Door (Cell 99) with at least 1000 energy in your canister.\n\n" +
            
            "GAME SETUP\n" +
            "1. Choose Your Side: Select SCARER or LAUGHER.\n" +
            "2. Assign Monsters: The game randomly assigns you a monster matching your role, and an opponent from the opposing role.\n\n" +
            
            "TURN SEQUENCE\n" +
            "• Powerup Phase: Spend 500 energy to activate your unique ability.\n" +
            "• Roll & Move: Roll a 6-sided dice. You cannot land on the opponent.\n" +
            "• Cell Action: Apply the effect of the cell you land on.\n\n" +
            
            "BOARD CELLS\n" +
            "• Doors (50): Alternate between Scarer and Laugher. Matching your role gives your team energy. Mismatching loses energy. Doors are one-time use and are exhausted forever once activated.\n" +
            "• Cards (10): Draw a random card (Swap, Steal, Shield, Confusion, Start Over). Cards are discarded after use. The deck reshuffles if empty.\n" +
            "• Monster Cells (6): Land on a teammate for a free powerup. Land on an opponent to swap energy (only if you have less than them).\n" +
            "• Conveyor Belts (5): Instantly transport you forward.\n" +
            "• Contamination Socks (5): Transport you backward and drain 100 energy.\n\n" +
            
            "MONSTER CLASSES\n" +
            "• Dasher: Base movement is doubled. Powerup grants 3x speed for 3 turns.\n" +
            "• Dynamo: All energy gains/losses are doubled. Powerup forces opponent to skip a turn.\n" +
            "• Multitasker: Moves half the dice roll, but gets +200 to all energy changes. Powerup grants normal speed for 2 turns.\n" +
            "• Schemer: Gains +10 bonus to all energy changes. Powerup steals 10 energy from every stationed monster.";

        rulesTextArea.setText(gameRules);

        // 2. KEEP YOUR EXISTING ANIMATION LOGIC
        btnClose.setOnAction(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), popupOverlay);
            fadeOut.setToValue(0);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), popupOverlay);
            scaleDown.setToX(0.8);
            scaleDown.setToY(0.8);
            
            ParallelTransition disappearAnimation = new ParallelTransition(fadeOut, scaleDown);
            
            disappearAnimation.setOnFinished(e -> {
                // Safely use Pane (the parent of all layouts) instead of strict StackPane
                Pane parent = (Pane) popupOverlay.getParent();
                if (parent != null) {
                    parent.getChildren().remove(popupOverlay);
                }
            });
            
            disappearAnimation.play();
        });
    }
}