package game.controller;

import java.util.HashMap;
import java.util.Map;

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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class BoardController {
    
    // --- FXML INJECTIONS ---
    @FXML private GridPane boardGrid;
    @FXML private Pane overlayPane;
    @FXML private Label pNameLabel, pTypeLabel, pOriginalRoleLabel, pCurrentRoleLabel, pEnergyLabel, pPositionLabel, pStatusLabel, pAbilityLabel;
    @FXML private Label oNameLabel, oTypeLabel, oOriginalRoleLabel, oCurrentRoleLabel, oEnergyLabel, oPositionLabel, oStatusLabel, oAbilityLabel;
    @FXML private Button btnRollDice, btnPowerup;
    @FXML private Label gameLogLabel;
    @FXML private StackPane errorPopupOverlay;
    @FXML private Label errorPopupMessage;
    @FXML private StackPane cardPopupOverlay;
    @FXML private Label cardTitleLabel;
    @FXML private Label cardEffectLabel;
    
    // --- STATE VARIABLES ---
    private Game game;
    private Image sockImg, cardImg, conveyorImg;
    private Image[] doorImages;
    private final Map<String, Image> monsterImages = new HashMap<>();
    private StackPane[] cellViews = new StackPane[100]; 
    private ImageView playerToken, opponentToken;
    
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
        
        playerToken = new ImageView(monsterImages.get(game.getPlayer().getName()));
        opponentToken = new ImageView(monsterImages.get(game.getOpponent().getName()));

        playerToken.setFitWidth(30); playerToken.setFitHeight(30);
        opponentToken.setFitWidth(30); opponentToken.setFitHeight(30);

        // Uses the new CSS class instead of inline styles
        playerToken.getStyleClass().add("token-shadow");
        opponentToken.getStyleClass().add("token-shadow");

        updateTokenPositions();
        updateDashboards();
        drawTransportArrows();
        
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
        try {
            Monster activeMonster = game.getCurrent();
            game.playTurn(); 
            gameLogLabel.setText(activeMonster.getName() + " rolled the dice and moved!");
            
            Card drawnCard = Board.getLastDrawnCard(); 
            
            if (drawnCard != null) {
                showCardPopup("Card Drawn!", getCardDescription(drawnCard));
                
                // Erase it from the engine so it doesn't trigger again next turn
                Board.clearLastDrawnCard(); 
            }
            
            updateDashboards();
            updateTokenPositions(); 
            updateDoorVisuals();
            updateCardVisuals();
            checkWinCondition();               

        } catch (InvalidMoveException e) {
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
        if (m.isFrozen()) status.append("❄️ Frozen\n");
        if (m.isConfused()) status.append("🌀 Confused\n"); 
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
        
        return status.length() == 0 ? "None" : status.toString();
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
                cellViews[i].setEffect(exhaustedEffect);
                cellViews[i].setOpacity(0.5);
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
                    cellViews[i].setEffect(exhaustedEffect);
                    cellViews[i].setOpacity(0.5);
                } else {
                    // RESTORE COLOR: Safely clears effects when the deck gets reshuffled!
                    cellViews[i].setEffect(null);
                    cellViews[i].setOpacity(1.0);
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
        stack.setMinSize(60, 60); stack.setMaxSize(60, 60); stack.setPrefSize(60, 60);
        stack.getStyleClass().add("grid-cell");

        ImageView bgView = new ImageView();
        bgView.setFitWidth(58); bgView.setFitHeight(58);
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
            popup.showAndWait(); 

            Stage mainStage = (Stage) boardGrid.getScene().getWindow(); 
            mainStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/resources/fxml/welcome.fxml")).load(), 1024, 768));
        } catch (Exception e) {
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
}