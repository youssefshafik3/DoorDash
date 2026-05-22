package game.controller;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.HashMap;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import java.util.Map;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.*;
import game.engine.cells.CardCell;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
public class BoardController {
    
    // --- FXML INJECTIONS ---
	
    @FXML private GridPane boardGrid;
    @FXML private Pane overlayPane;
    @FXML private Label pTitleLabel,pNameLabel, pTypeLabel, pOriginalRoleLabel, pCurrentRoleLabel, pEnergyLabel, pPositionLabel, pStatusLabel, pAbilityLabel;
    @FXML private Label oTitleLabel,oNameLabel, oTypeLabel, oOriginalRoleLabel, oCurrentRoleLabel, oEnergyLabel, oPositionLabel, oStatusLabel, oAbilityLabel;
    @FXML private Button btnRollDice, btnPowerup;
    @FXML private Label gameLogLabel;
    @FXML private StackPane errorPopupOverlay;
    @FXML private Label errorPopupMessage;
    @FXML private StackPane cardPopupOverlay;
    @FXML private Label cardTitleLabel;
    @FXML private Label cardEffectLabel;
    @FXML private StackPane mainRootPane;
    @FXML private VBox playerDashboard;
    @FXML private StackPane cardPileContainer;
    
    // --- STATE VARIABLES ---
 // Keeps the music alive so the garbage collector doesn't mute it!
    private javafx.scene.media.MediaPlayer backgroundMusicPlayer;
    private Game game;
    private Image sockImg, cardImg, conveyorImg;
    private Image[] doorImages;
    private final Map<String, Image> monsterImages = new HashMap<>();
    private StackPane[] cellViews = new StackPane[100]; 
    private ImageView playerToken, opponentToken;
 // Track the number of cards left in the active drawing pile
    private int cardsRemaining = 25;
    private Label cardPileLabel;
    // --- INITIALIZATION ---
    public void initData(Game initializedGame) {
        this.game = initializedGame;
        
        try {
            sockImg = new Image(getClass().getResourceAsStream("/resources/images/sock.jpg"));
            cardImg = new Image(getClass().getResourceAsStream("/resources/images/card.png"));
            conveyorImg = new Image(getClass().getResourceAsStream("/resources/images/conveyor.png"));
            doorImages = new Image[] {
                new Image(getClass().getResourceAsStream("/resources/images/doors/door_red.jpg")),
                new Image(getClass().getResourceAsStream("/resources/images/doors/door_blue.jpg")),
                new Image(getClass().getResourceAsStream("/resources/images/doors/door_green.jpg")),
                new Image(getClass().getResourceAsStream("/resources/images/doors/door_yellow.jpg")),
                new Image(getClass().getResourceAsStream("/resources/images/doors/boosDoor.jpg"))
            };
            monsterImages.put("James P. Sullivan", new Image(getClass().getResourceAsStream("/resources/images/monsters/sully.png")));
            monsterImages.put("Mike Wazowski", new Image(getClass().getResourceAsStream("/resources/images/monsters/mike.png")));
            monsterImages.put("Randall Boggs", new Image(getClass().getResourceAsStream("/resources/images/monsters/randall.png")));
            monsterImages.put("Celia Mae", new Image(getClass().getResourceAsStream("/resources/images/monsters/celia.png")));
            monsterImages.put("Roz", new Image(getClass().getResourceAsStream("/resources/images/monsters/roz.png")));
            monsterImages.put("Fungus", new Image(getClass().getResourceAsStream("/resources/images/monsters/fungus.png")));
            monsterImages.put("Henry J. Waternoose", new Image(getClass().getResourceAsStream("/resources/images/monsters/waternoose.png")));
            monsterImages.put("Yeti", new Image(getClass().getResourceAsStream("/resources/images/monsters/yeti.png")));
        } catch (NullPointerException e) {
            System.err.println("CRITICAL: Missing image file.");
            e.printStackTrace();
        }
        
        buildGrid();
        initCardPile();
        playerToken = new ImageView(monsterImages.get(game.getPlayer().getName()));
        opponentToken = new ImageView(monsterImages.get(game.getOpponent().getName()));

        playerToken.setFitWidth(30); playerToken.setFitHeight(30);
        opponentToken.setFitWidth(30); opponentToken.setFitHeight(30);
        
     // Remove the old "token-shadow" class and add the new specific ones
        playerToken.getStyleClass().clear();
        playerToken.getStyleClass().add("player-glow");

        opponentToken.getStyleClass().clear();
        opponentToken.getStyleClass().add("opponent-glow");

        updateTokenPositions();
        updateDashboards();
        drawTransportArrows();
        cardPileLabel = new Label("🃏 CARDS PILE: 25 / 25 LEFT");
        cardPileLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-padding: 10px;");
        cardPileLabel.setWrapText(true);
        startBackgroundMusic();
        // 👉 2. ADD IT TO THE DASHBOARD
        playerDashboard.getChildren().add(cardPileLabel);
        updateCardPileUI();
        // Wait for the scene to load, then attach the cheat keys
        Platform.runLater(this::setupDebugControls);
    }
    
    // --- CHEAT KEYS (Milestone Requirement) ---
    private void setupDebugControls() {
        if (boardGrid.getScene() != null) {
            boardGrid.getScene().setOnKeyPressed((KeyEvent event) -> {
                
                // [W]: INSTANT WIN
                if (event.getCode() == KeyCode.W) {
                    triggerGameOver(game.getPlayer().getName(), game.getPlayer().getRole().toString(), "#00ffcc");
                }
                
                // [E]: INCREASE ENERGY (Active Player)
                else if (event.getCode() == KeyCode.E) {
                    try {
                        Monster active = game.getCurrent();
                        active.setEnergy(active.getEnergy() + 20); // Adds 20 directly to engine
                        updateDashboards(); // Refreshes UI to show new energy
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // --- GAMEPLAY LOOP ---
    @FXML
    private void handleRollDice(ActionEvent event) {
        // 1. CAPTURE THE "BEFORE" SNAPSHOT
        Monster activeMonster = game.getCurrent();
        Monster opponent = game.getCurrent() == game.getPlayer() ? game.getOpponent() : game.getPlayer();
        
        int oldPos = activeMonster.getPosition();
        int oldEnergy = activeMonster.getEnergy();
        int oldOppEnergy = opponent.getEnergy();

        try {
            // 2. PLAY THE TURN
            int roll = game.playTurn();

            if (roll == 0) {
                gameLogLabel.setText("❄️ " + activeMonster.getName() + " is frozen and skipped their turn!");
                updateDashboards();
                return;
            }

// ... inside handleRollDice ...
            
            // 3. CAPTURE THE "AFTER" SNAPSHOT
         // 3. CAPTURE THE "AFTER" SNAPSHOT
            int newPos = activeMonster.getPosition();
            int newEnergy = activeMonster.getEnergy();
            int newOppEnergy = opponent.getEnergy();
            Card drawnCard = Board.getLastDrawnCard();

            // --- CALCULATE EXACT DESTINATION ---
            int expectedMove = roll; 
            
            if (activeMonster instanceof Dasher) { 
                Dasher m = (Dasher) activeMonster;
                expectedMove = m.getMomentumTurns() > 0 ? roll * 3 : roll * 2;
            } else if (activeMonster instanceof MultiTasker) { 
                MultiTasker m = (MultiTasker) activeMonster;
                if (m.getNormalSpeedTurns() == 0) {
                    expectedMove = roll / 2;
                }
            }
            
            // Apply Wrap-Around Logic!
            int expectedPos = (oldPos + expectedMove) % 100; // or % Constants.BOARD_SIZE
            
            // THE BRILLIANT FIX: Just look at the cell they landed on!
            // (Adjust the getter below to match your actual Board class architecture)
            int cols = Constants.BOARD_COLS;

    	    int row = expectedPos/ cols;
    	    int col = expectedPos % cols;

    	    if (row % 2 == 1)
    	        col = cols - 1 - col;

            Cell landedCell = game.getBoard().getBoardCells()[row][col]; 

            // 4. BUILD THE SMART LOG
            StringBuilder log = new StringBuilder("🎲 " + activeMonster.getName() + " rolled a " + roll + ". ");

            // Check exactly what they stepped on
            if (landedCell instanceof CardCell && drawnCard != null) {
                log.append("🃏 Landed on a Card Cell! Drew a ").append(drawnCard.getName()).append("! ");
                animateCardFromPile(getCardDescription(drawnCard));
                removeTopCardFromPile();

                Board.clearLastDrawnCard();
                decrementCardPile();
                
            } else if (landedCell instanceof ConveyorBelt) {
                log.append("🚀 Hit a Conveyor Belt! Taken to Cell ").append(newPos).append(". ");
                
            } else if (landedCell instanceof ContaminationSock) {
                log.append("🧦 Hit a Contamination Sock! Taken to Cell ").append(newPos).append(". ");
                
            } else if (landedCell instanceof MonsterCell) {
                log.append("👾 Landed on a Monster Cell! ");
                if (drawnCard == null && (newEnergy != oldEnergy || newOppEnergy != oldOppEnergy)) {
                    log.append("Energies Swapped! "); // Or write logic to check if they got a free powerup
                }
                
            } else if (landedCell instanceof DoorCell) {
                log.append("🚪 Landed on a Door! ");
                
            } else {
                log.append("Landed safely on Cell ").append(newPos).append(". ");
            }

            // --- REPORT UNIVERSAL ENERGY CHANGES ---
            // Because Socks, Doors, and Cards can all change energy, we just append the result at the end
            int energyDiff = newEnergy - oldEnergy;
            
            if (energyDiff > 0 && !(landedCell instanceof MonsterCell)) {
                log.append("🔋 Gained ").append(energyDiff).append(" Energy.");
            } else if (energyDiff < 0 && !(landedCell instanceof MonsterCell)) {
                log.append("💥 Lost ").append(Math.abs(energyDiff)).append(" Energy.");
            }

            // 5. FINALIZE UI
            gameLogLabel.setText(log.toString().trim());
            updateDashboards();
            updateTokenPositions();
            updateDoorVisuals();
            updateCardVisuals();
            checkWinCondition();                

        } catch (InvalidMoveException e) {
            // 6. CATCH OCCUPIED CELL COLLISIONS (AND GHOST PENALTIES)
            int failedRoll = game.getLastRoll(); 
            StringBuilder errorLog = new StringBuilder("🚫 " + activeMonster.getName() + " rolled a " + failedRoll + " but crashed into the opponent! ");
            
            // Did they lose energy from a Sock/Mismatch Door before crashing?
            int currentEnergy = activeMonster.getEnergy();
            if (currentEnergy < oldEnergy) {
                errorLog.append("💥 Still lost ").append(oldEnergy - currentEnergy).append(" Energy from the cell trap! ");
            }
            
            // Did they burn a card before crashing?
            Card burntCard = Board.getLastDrawnCard();
            if (burntCard != null) {
                errorLog.append("🃏 Burnt a ").append(burntCard.getName()).append(" in the process! ");
                Board.clearLastDrawnCard(); // CRITICAL: Clear it so it doesn't bleed into the next turn!
            }

            errorLog.append("Roll again.");
            gameLogLabel.setText(errorLog.toString());
            
            // Update EVERYTHING, not just dashboards, because doors/cards might have been exhausted
            updateDashboards(); 
            updateTokenPositions(); // Make sure they visually snap back to oldPosition
            updateDoorVisuals();
            updateCardVisuals();
            
            showErrorPopup("Invalid Move: " + e.getMessage());
            
        } catch (Exception e) {
            showErrorPopup("An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleUsePowerup(ActionEvent event) {
        try {
            Monster activeMonster = game.getCurrent();
            game.usePowerup();
            
            // Generate a specific, visual log message based on the monster's class
            if (activeMonster instanceof Dasher) {
                gameLogLabel.setText("⚡ " + activeMonster.getName() + " activated Momentum Rush! (3x Speed)");
            } 
            else if (activeMonster instanceof MultiTasker) {
                gameLogLabel.setText("🎯 " + activeMonster.getName() + " activated Focus Mode! (Normal Speed)");
            } 
            else if (activeMonster instanceof Dynamo) {
                gameLogLabel.setText("❄️ " + activeMonster.getName() + " used Screech Freeze! Opponent skips next turn!");
            } 
            else if (activeMonster instanceof Schemer) {
                gameLogLabel.setText("🦇 " + activeMonster.getName() + " used Chain Attack! Energy stolen from the oppnent and the board!");
            }
            
            updateDashboards(); 
            
        } catch (OutOfEnergyException e) {
            showErrorPopup("Cannot use powerup: " + e.getMessage());
        } catch (Exception e) {
            showErrorPopup("Error: " + e.getMessage());
        }
    }
    
    private void checkWinCondition() {
        Monster winner = game.getWinner();
        if (winner != null) {
            gameLogLabel.setText("GAME OVER! " + winner.getName() + " wins!");
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);
            triggerGameOver(winner.getName(), winner.getRole().toString(), "#00ffcc");
        }
    }

    // --- UI UPDATERS ---
    private void updateDashboards() {
        Monster p = game.getPlayer();
        Monster o = game.getOpponent();
        Monster c = game.getCurrent();

        // Dynamically update the top headers!
        if (c == p) {
            pTitleLabel.setText("▶ PLAYER\n (YOUR TURN)");
            oTitleLabel.setText("OPPONENT");
        } else {
            pTitleLabel.setText("PLAYER");
            oTitleLabel.setText("▶ OPPONENT\n (YOUR TURN)");
        }

        pNameLabel.setText("Name: " + p.getName());
        pTypeLabel.setText("Type: " + getMonsterTypeString(p)); 
        pOriginalRoleLabel.setText("Original Role: " + p.getOriginalRole());
        pCurrentRoleLabel.setText("Current Role: " + p.getRole());
        pEnergyLabel.setText("Energy: " + p.getEnergy());
        pPositionLabel.setText("Position: Cell " + p.getPosition());
        pStatusLabel.setText(buildStatusString(p));
        pAbilityLabel.setText(getMonsterAbilities(game.getPlayer()));
        

        oNameLabel.setText("Name: " + o.getName());
        oTypeLabel.setText("Type: " + getMonsterTypeString(o)); 
        oOriginalRoleLabel.setText("Original Role: " + o.getRole());
        oCurrentRoleLabel.setText("Current Role: " + o.getRole());
        oEnergyLabel.setText("Energy: " + o.getEnergy());
        oPositionLabel.setText("Position: Cell " + o.getPosition());
        oStatusLabel.setText(buildStatusString(o));
        oAbilityLabel.setText(getMonsterAbilities(game.getOpponent()));
    }
    
    private String getMonsterTypeString(Monster m) {
        if (m instanceof Dasher) return "Dasher";
        if (m instanceof Dynamo) return "Dynamo";
        if (m instanceof MultiTasker) return "Multitasker";
        return "Schemer";
    }

    private String buildStatusString(Monster m) {
        StringBuilder status = new StringBuilder();
        
        // Generic Statuses
        if (m.isFrozen()) status.append("❄️ Frozen (1 turn left)\n");
        if (m.isConfused()) {
        	int turns = m.getConfusionTurns();
        	if (turns > 0)
        		status.append("🌀 Confused (").append(turns).append(" left)\n");
        }
        if (m.isShielded()) status.append("🛡️ Shielded\n");
        
        // Specific Powerup Buffs
        if (m instanceof Dasher) {
            int turns = ((Dasher) m).getMomentumTurns();
            if (turns > 0) status.append("⚡ Momentum Rush (").append(turns).append(" left)\n");
        }
        
        if (m instanceof MultiTasker) {
            int turns = ((MultiTasker) m).getNormalSpeedTurns();
            if (turns > 0) status.append("🎯 Focus Mode (").append(turns).append(" left)\n");
        }
        
        return status.length() == 0 ? "Currently, no special effects are applied!" : status.toString();
    }
    private String getMonsterAbilities(Monster m) {
        if (m instanceof Dynamo) {
            return "⚡ Powerup: Screech Freeze (Freezes opponent for 1 turn)\n" +
                   "✨ Passive: Doubles all incoming energy changes";
        } 
        else if (m instanceof Dasher) {
            return "⚡ Powerup: Momentum Rush (3x speed for 3 turns)\n" +
                   "✨ Passive: Always moves at 2x the dice roll";
        } 
        else if (m instanceof MultiTasker) {
            return "⚡ Powerup: Focus Mode (Normal speed for 2 turns)\n" +
                   "✨ Passive: Moves at 1/2 dice roll, +200 to all energy changes";
        } 
        else if (m instanceof Schemer) {
            return "⚡ Powerup: Chain Attack (Steals energy from all stationed monsters)\n" +
                   "✨ Passive: Gains +10 bonus to all energy changes";
        }
        return "No special abilities.";
    }
    private String getCardDescription(Card card) {
        if (card instanceof SwapperCard) {
            return "Swapper Card!\nIf you are behind, you will swapp positions with the opponent!";
        } 
        else if (card instanceof EnergyStealCard) {
            return "Energy Steal!\n Opponent's Energy will be transferred to you (unless they have a shield!).";
        } 
        else if (card instanceof ShieldCard) {
            return "Shield Card!\nYou will gain a shield against the next negative energy effect, and the opponent's shield will be destroyed if they have one.";
        } 
        else if (card instanceof StartOverCard) {
            return "Start Over!\nSomeone will get sent all the way back to Cell 0!";
        } 
        else if (card instanceof ConfusionCard) {
            return "Confusion!\nBoth monsters will swap roles! Doors will now have reversed effects.";
        }
        return "A mysterious card was drawn!";
    }
    
    private void updateTokenPositions() {
        int pPos = game.getPlayer().getPosition();
        int oPos = game.getOpponent().getPosition();

        if (playerToken.getParent() != null) ((StackPane) playerToken.getParent()).getChildren().remove(playerToken);
        if (opponentToken.getParent() != null) ((StackPane) opponentToken.getParent()).getChildren().remove(opponentToken);

        if (pPos == oPos) {
            StackPane.setAlignment(playerToken, Pos.CENTER_LEFT);
            StackPane.setAlignment(opponentToken, Pos.CENTER_RIGHT);
        } else {
        	playerToken.setFitWidth(50); playerToken.setFitHeight(50);
            opponentToken.setFitWidth(50); opponentToken.setFitHeight(50);
            StackPane.setAlignment(playerToken, Pos.CENTER);
            StackPane.setAlignment(opponentToken, Pos.CENTER);
        }

        cellViews[pPos].getChildren().add(playerToken);
        cellViews[oPos].getChildren().add(opponentToken);
        playerToken.toFront(); opponentToken.toFront();
    }

    private void updateDoorVisuals() {
        ColorAdjust exhaustedEffect = new ColorAdjust();
        exhaustedEffect.setSaturation(-1.0); 
        exhaustedEffect.setBrightness(-0.3); 

        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            int row = i / Constants.BOARD_COLS;
            int col = i % Constants.BOARD_COLS;
            if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;

            if (cells[row][col] instanceof DoorCell && ((DoorCell) cells[row][col]).isActivated()) {
            	for (Node node : cellViews[i].getChildren()) {
                    
                    // Only gray it out if it is NOT one of the player tokens
                    if (node != playerToken && node != opponentToken) {
                        node.setEffect(exhaustedEffect);
                        node.setOpacity(0.5);
                    }
            	}
            }
        }
    }
    private void updateCardVisuals() {
        ColorAdjust exhaustedEffect = new ColorAdjust();
        exhaustedEffect.setSaturation(-1.0); // Completely removes color (grayscale)
        exhaustedEffect.setBrightness(-0.3); // Darkens the cell slightly

        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            int row = i / Constants.BOARD_COLS;
            int col = i % Constants.BOARD_COLS;
            if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;

            if (cells[row][col] instanceof CardCell) {
                // Check the static tracker in your Board class
                if (Board.isCardCellExhausted(i)) {
                	for (Node node : cellViews[i].getChildren()) {
                        
                        // Only gray it out if it is NOT one of the player tokens
                        if (node != playerToken && node != opponentToken) {
                            node.setEffect(exhaustedEffect);
                            node.setOpacity(0.5);
                        }
                    }
                } else {
                    // RESTORE COLOR: Safely clears effects when the deck gets reshuffled!
                	for (Node node : cellViews[i].getChildren()) {
                        if (node != playerToken && node != opponentToken) {
                            node.setEffect(null);
                            node.setOpacity(1.0);
                        }
                	}
                }
            }
        }
    }

    // --- VISUAL BUILDERS & MATH ---
    private void buildGrid() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            int row = i / Constants.BOARD_COLS;
            int col = i % Constants.BOARD_COLS;
            if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;

            int guiRow = (Constants.BOARD_ROWS - 1) - row;
            StackPane cellView = createCellView(cells[row][col], i);
            cellViews[i] = cellView;
            boardGrid.add(cellView, col, guiRow);
        }
    }
    
    private StackPane createCellView(Cell cell, int index) {
        StackPane stack = new StackPane();
        stack.setMinSize(77, 68); stack.setMaxSize(77, 68); stack.setPrefSize(77, 68);
        stack.getStyleClass().add("grid-cell");

        ImageView bgView = new ImageView();
        bgView.setFitWidth(60); bgView.setFitHeight(60);
        boolean addImage = true; 

        // 1. FIXED: Removed the duplicate duplicate 'if' statement line here
        if (cell instanceof MonsterCell) {
            MonsterCell mCell = (MonsterCell) cell;
            String monsterName = mCell.getName().trim();
            Image specificMonsterImg = monsterImages.get(monsterName);
            
            if (specificMonsterImg != null) {
                bgView.setImage(specificMonsterImg);
            } else {
                System.err.println("Warning: No image found for monster " + monsterName);
            }
            
            Label roleLbl = new Label(((MonsterCell) cell).getCellMonster().getRole() == Role.SCARER ? "S" : "L");
            roleLbl.getStyleClass().add("door-role-label");
            StackPane.setAlignment(roleLbl, Pos.TOP_RIGHT); 
            
            Label nrgLbl = new Label(((MonsterCell) cell).getCellMonster().getEnergy() + "");
            nrgLbl.getStyleClass().add("door-energy-label");
            StackPane.setAlignment(nrgLbl, Pos.BOTTOM_CENTER); 
            
            stack.getChildren().addAll(bgView, roleLbl, nrgLbl);
            addImage = false; 
        } else if (cell instanceof ConveyorBelt) {
            bgView.setImage(conveyorImg);
        } else if (cell instanceof ContaminationSock) {
            bgView.setImage(sockImg);
        } else if (cell instanceof CardCell) {
            bgView.setImage(cardImg);
        } else if (cell instanceof DoorCell) {
            bgView.setImage(index == 99 ? doorImages[4] : doorImages[(index / 2) % 4]);
            
            Label roleLbl = new Label(((DoorCell) cell).getRole() == Role.SCARER ? "S" : "L");
            roleLbl.getStyleClass().add("door-role-label");
            StackPane.setAlignment(roleLbl, Pos.TOP_RIGHT); 
            
            Label nrgLbl = new Label(((DoorCell) cell).getEnergy() + "");
            nrgLbl.getStyleClass().add("door-energy-label");
            StackPane.setAlignment(nrgLbl, Pos.BOTTOM_CENTER); 
            
            stack.getChildren().addAll(bgView, roleLbl, nrgLbl);
            addImage = false; 
        } else {
            Rectangle bg = new Rectangle(60, 60);
            bg.getStyleClass().add("normal-cell-bg");
            stack.getChildren().add(bg);
            addImage = false; 
        }

        // 2. If the cell block didn't manually handle adding the bgView, add it now
        if (addImage) {
            stack.getChildren().add(bgView);
        }

        // 3. Layer the cell index number on top of everything
        Label idxLbl = new Label(String.valueOf(index));
        idxLbl.getStyleClass().add("cell-index-label");
        StackPane.setAlignment(idxLbl, Pos.TOP_LEFT);
        stack.getChildren().add(idxLbl);

        return stack;
    }
        
    
    private void drawTransportArrows() {
        Platform.runLater(() -> {
            overlayPane.maxWidthProperty().bind(boardGrid.widthProperty());
            overlayPane.maxHeightProperty().bind(boardGrid.heightProperty());
            overlayPane.getChildren().clear(); 

            Cell[][] cells = game.getBoard().getBoardCells();
            for (int i = 0; i < Constants.BOARD_SIZE; i++) {
                int row = i / Constants.BOARD_COLS;
                int col = i % Constants.BOARD_COLS;
                if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;

                if (cells[row][col] instanceof ConveyorBelt) {
                    createArrowPath(i, Math.min(99, i + ((ConveyorBelt) cells[row][col]).getEffect()), Color.DARKORANGE);
                } else if (cells[row][col] instanceof ContaminationSock) {
                    createArrowPath(i, Math.max(0, i + ((ContaminationSock) cells[row][col]).getEffect()), Color.CRIMSON);
                }
            }
        });
    }

    private void createArrowPath(int startIdx, int endIdx, Color color) {
        Bounds start = overlayPane.sceneToLocal(cellViews[startIdx].localToScene(cellViews[startIdx].getBoundsInLocal()));
        Bounds end = overlayPane.sceneToLocal(cellViews[endIdx].localToScene(cellViews[endIdx].getBoundsInLocal()));

        double sX = start.getMinX() + (start.getWidth() / 2), sY = start.getMinY() + (start.getHeight() / 2);
        double eX = end.getMinX() + (end.getWidth() / 2), eY = end.getMinY() + (end.getHeight() / 2);
        double cX = (sX + eX) / 2 - ((eY - sY) * 0.25), cY = (sY + eY) / 2 + ((eX - sX) * 0.25);

        QuadCurve curve = new QuadCurve(sX, sY, cX, cY, eX, eY);
        curve.setStroke(color); curve.setStrokeWidth(4); curve.setFill(Color.TRANSPARENT);
        curve.getStrokeDashArray().addAll(8d, 6d); 

        double angle = Math.atan2(eY - cY, eX - cX);
        Polygon arrow = new Polygon(
            eX, eY,
            eX - 20 * Math.cos(angle) + 10 * Math.sin(angle), eY - 20 * Math.sin(angle) - 10 * Math.cos(angle),
            eX - 20 * Math.cos(angle) - 10 * Math.sin(angle), eY - 20 * Math.sin(angle) + 10 * Math.cos(angle)
        );
        arrow.setFill(color);
        overlayPane.getChildren().addAll(curve, arrow);
    }

    // --- SCENE TRANSITIONS & ERROR POPUPS ---
    private void showErrorPopup(String msg) {
        errorPopupMessage.setText(msg); errorPopupOverlay.setVisible(true);
        boardGrid.setDisable(true); btnRollDice.setDisable(true); btnPowerup.setDisable(true);
    }

    @FXML
    private void hideErrorPopup(ActionEvent event) {
        errorPopupOverlay.setVisible(false);
        boardGrid.setDisable(false); btnRollDice.setDisable(false); btnPowerup.setDisable(false);
    }
    
    private void triggerGameOver(String name, String role, String color) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/gameOver.fxml"));
            Parent root = loader.load();
            GameOverController pc = loader.getController();
            
            pc.setGameStats(name, role, game.getPlayer().getEnergy(), game.getOpponent().getEnergy(), color);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            
            // 1. CHANGE THIS: Make the Window itself completely transparent
            popup.initStyle(StageStyle.TRANSPARENT); 
            
            // 2. CHANGE THIS: Make the Scene canvas completely transparent
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); 
            
            popup.setScene(scene);
            popup.centerOnScreen();
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
                System.out.println("Stopping background music for Game Over.");
            }
            playWinSound(role);
            popup.showAndWait(); 

            Stage mainStage = (Stage) boardGrid.getScene().getWindow(); 
            mainStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/resources/fxml/welcome.fxml")).load(), 1024, 768));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Plays a specific sound effect based on the winning monster's role.
     */
    private void playWinSound(String role) {
        try {
            String soundFile = "";
            
            // Determine which file to play based on the role
            if ("Scarer".equalsIgnoreCase(role)) {
                soundFile = "/resources/music/ScarerWinning.mp3";
            } else if ("Laugher".equalsIgnoreCase(role)) {
                soundFile = "/resources/music/laugherWinning.mp3";
            } else {
                return; // Exit if the role doesn't match
            }

            // Locate the file inside the project structure
            java.net.URL audioUrl = getClass().getResource(soundFile);
            
            if (audioUrl != null) {
                javafx.scene.media.Media media = new javafx.scene.media.Media(audioUrl.toString());
                javafx.scene.media.MediaPlayer mediaPlayer = new javafx.scene.media.MediaPlayer(media);
                mediaPlayer.play();
                System.out.println("Playing victory sound: " + soundFile);
            } else {
                System.err.println("ERROR: Could not find sound file at " + soundFile + ". Check your folder structure!");
            }
        } catch (Exception e) {
            System.err.println("Failed to play audio. Ensure javafx.media module is loaded.");
            e.printStackTrace();
        }
    }
    private void showCardPopup(String title, String effect) {
        cardTitleLabel.setText(title);
        cardEffectLabel.setText(effect);
        
        cardPopupOverlay.setVisible(true);
        
        // Lock the game board so they have to acknowledge the card
        boardGrid.setDisable(true);
        btnRollDice.setDisable(true);
        btnPowerup.setDisable(true);
    }

    @FXML
    private void hideCardPopup(ActionEvent event) {
        cardPopupOverlay.setVisible(false);
        
        boardGrid.setDisable(false);
        btnRollDice.setDisable(false);
        btnPowerup.setDisable(false);
    }
    @FXML
    private void handleShowRules(javafx.event.ActionEvent event) {
        try {
            // 1. Load the rules FXML (Make sure the path matches where you saved it!)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/popup_rules.fxml"));
            StackPane rulesPopup = loader.load();
            
            // 2. Make sure it completely covers the board
            rulesPopup.prefWidthProperty().bind(mainRootPane.widthProperty());
            rulesPopup.prefHeightProperty().bind(mainRootPane.heightProperty());
            
            // 3. Add it to the very front of the screen
            mainRootPane.getChildren().add(rulesPopup);
            rulesPopup.toFront();
            
        } catch (Exception e) {
            System.err.println("Could not load rules popup!");
            e.printStackTrace();
        }
    }
    /**
     * Updates the graphical card pile text display on the screen.
     */
    private void updateCardPileUI() {
        if (cardPileLabel != null) {
            cardPileLabel.setText("🃏 CARD PILE: " + cardsRemaining + " / 25 LEFT");
            
            // Optional styling to make it look prominent:
            cardPileLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        }
    }

    /**
     * Decreases the card pile count and handles the automatic rule reshuffle.
     */
    private void decrementCardPile() {
        cardsRemaining--;
        System.out.println("A card was drawn. Cards remaining in pile: " + cardsRemaining);
        
        // Rule Implementation: When out of cards, reshuffle original cards back into the pile
        if (cardsRemaining <= 0) {
            cardsRemaining = 25; 
            System.out.println("🔄 Deck pile exhausted! Reshuffling all 25 cards back into the pile.");
        }
        
        updateCardPileUI();
    }
    /**
     * Starts the continuous background music for the game board.
     */
    private void startBackgroundMusic() {
        try {
            // Find the file in your sounds folder
            java.net.URL audioUrl = getClass().getResource("/resources/music/The Scare Floor.mp3");
            
            if (audioUrl != null) {
                javafx.scene.media.Media media = new javafx.scene.media.Media(audioUrl.toString());
                backgroundMusicPlayer = new javafx.scene.media.MediaPlayer(media);
                
                // Loop the music endlessly
                backgroundMusicPlayer.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE);
                
                // Optional: Lower the volume to 30% so sound effects can still be heard
                backgroundMusicPlayer.setVolume(0.3); 
                
                backgroundMusicPlayer.play();
                System.out.println("Background music started!");
            } else {
                System.err.println("Could not find background_music.mp3 in the sounds folder.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background music.");
            e.printStackTrace();
        }
    }
    public void showCardTransition( String cardDescription) {
        // 1. Dark overlay for background focus
        Region darkOverlay = new Region();
        darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // 2. The Playing Card container (Portrait shape)
        VBox cardBox = new VBox(20);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setMaxSize(320, 480); // Portrait aspect ratio
        cardBox.setStyle(
            "-fx-background-color: #0b61d9; " + 
            "-fx-background-radius: 15px; " +
            "-fx-border-radius: 15px; " +
            "-fx-border-color: white; " +       // White card stock border
            "-fx-border-width: 10px; " +        
            "-fx-padding: 30px 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 25, 0, 0, 10);"
        );

        // 3. Text Components
        Label headerLabel = new Label("CARD DRAWN");
        headerLabel.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Monster AG';");

       
        Label descLabel = new Label(cardDescription);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Monster AG'; -fx-line-spacing: 4px;");

        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: #ffcc00; -fx-text-fill: white; -fx-font-size: 18px; " +
            "-fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8px 30px; -fx-cursor: hand;"
        );

        // Spacers to push content into a card-like layout
        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        okButton.setOnAction(e -> mainRootPane.getChildren().removeAll(darkOverlay, cardBox));

        cardBox.getChildren().addAll(headerLabel, topSpacer, descLabel, bottomSpacer, okButton);
        mainRootPane.getChildren().addAll(darkOverlay, cardBox);

        // 4. Animations
        cardBox.setScaleX(0.1);
        cardBox.setScaleY(0.1);
        darkOverlay.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.seconds(0.4), cardBox);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.3), darkOverlay);
        ft.setToValue(1.0);

        st.play();
        ft.play();
    }
 // Add this to your BoardController
    
    public void initCardPile() {
        cardPileContainer.getChildren().clear();
        
        // Create 3 stacked card shapes
        for (int i = 0; i < 25; i++) {
            Region cardLayer = new Region();
            cardLayer.setPrefSize(80, 120); // Smaller than the full-sized card
            cardLayer.getStyleClass().add("card-pile");
            
            // Offset each layer slightly to show "stacking"
            cardLayer.setTranslateX(i * 0.15); 
            cardLayer.setTranslateY(i * -0.15);
            
            cardPileContainer.getChildren().add(cardLayer);
        }
    }

    public void removeTopCardFromPile() {
        // 1. Check if the pile has cards left
        if (cardPileContainer.getChildren().getLast()==cardPileContainer.getChildren().getFirst()) {
        	reshufflePile();     // Nothing to remove
        }

        // 2. Get the top card (the last one added to the list)
        int lastIndex = cardPileContainer.getChildren().size() - 1;
        Node topCard = cardPileContainer.getChildren().get(lastIndex);

        // 3. Create a smooth fade-out animation
        FadeTransition fade = new FadeTransition(Duration.millis(500), topCard);
        fade.setToValue(0); // Fade to invisible
        
        // 4. Once the animation finishes, remove it from the visual container
        fade.setOnFinished(e -> {
            cardPileContainer.getChildren().remove(topCard);
        });

        fade.play();
    }
    public void reshufflePile() {
        // Optional: Add a brief log message or visual effect
        System.out.println("Cards depleted! Reshuffling the deck...");
        
        // Call your existing initialization method to rebuild the 25 cards
        initCardPile();
        
        // Optional: Add a "fade-in" effect for the new deck so it doesn't just pop into existence
        cardPileContainer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), cardPileContainer);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    public void animateCardFromPile(String cardDescription) {
        // 1. Get the pile's bounds and convert to the mainRootPane's coordinate system
    	// 1. Get the pile's bounds and convert to the mainRootPane's coordinate system
    	// 1. Get the exact scene position of the card pile
        Bounds pileBounds = cardPileContainer.localToScene(cardPileContainer.getBoundsInLocal());
        double pileX = pileBounds.getMinX() + (pileBounds.getWidth() / 2);
        double pileY = pileBounds.getMinY() + (pileBounds.getHeight() / 2);

        // 2. Create the "Flying" Card
        Rectangle flyingCard = new Rectangle(80, 120);
        flyingCard.setStyle("-fx-fill: #0b61d9; -fx-stroke: white; -fx-stroke-width: 4; -fx-arc-width: 20; -fx-arc-height: 20;");

        // Add it to the pane FIRST
        mainRootPane.getChildren().add(flyingCard);

        // 3. Find the exact center of the screen
        Bounds rootBounds = mainRootPane.localToScene(mainRootPane.getBoundsInLocal());
        double centerX = rootBounds.getMinX() + (rootBounds.getWidth() / 2);
        double centerY = rootBounds.getMinY() + (rootBounds.getHeight() / 2);

        // 4. Set start position perfectly over the pile
        flyingCard.setTranslateX(pileX - centerX);
        flyingCard.setTranslateY(pileY - centerY);
        // 4. Create the Animations
        TranslateTransition move = new TranslateTransition(Duration.seconds(0.6), flyingCard);
        move.setToX(0); // Center of StackPane
        move.setToY(0);

        RotateTransition rotate = new RotateTransition(Duration.seconds(0.6), flyingCard);
        rotate.setByAngle(360);
        rotate.setAxis(Rotate.Y_AXIS);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.6), flyingCard);
        scale.setToX(3.5); // Adjusted scale to look good
        scale.setToY(3.5);

        ParallelTransition pt = new ParallelTransition(move, rotate, scale);
        
        // Cleanup and trigger the actual card info
        pt.setOnFinished(e -> {
            mainRootPane.getChildren().remove(flyingCard);
            showCardTransition(cardDescription);
        });

        pt.play();
    }
}