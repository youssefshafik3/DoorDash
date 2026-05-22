package game.controller;

// --- CLEANED IMPORTS ---
import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

import javafx.animation.*;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BoardController {

    // ==========================================
    // 1. FXML INJECTIONS & STATE
    // ==========================================
    @FXML private GridPane boardGrid;
    @FXML private Pane overlayPane;
    @FXML private Label pTitleLabel, pNameLabel, pTypeLabel, pOriginalRoleLabel, pCurrentRoleLabel, pEnergyLabel, pPositionLabel, pStatusLabel, pAbilityLabel;
    @FXML private Label oTitleLabel, oNameLabel, oTypeLabel, oOriginalRoleLabel, oCurrentRoleLabel, oEnergyLabel, oPositionLabel, oStatusLabel, oAbilityLabel;
    @FXML private Button btnRollDice, btnPowerup;
    @FXML private Label gameLogLabel;
    @FXML private StackPane errorPopupOverlay;
    @FXML private Label errorPopupMessage;
    @FXML private StackPane mainRootPane;
    @FXML private VBox playerDashboard,opponentDashboard;
    @FXML private StackPane cardPileContainer;

    private MediaPlayer backgroundMusicPlayer;
    private Game game;
    private Image sockImg, cardImg, conveyorImg;
    private Image[] doorImages;
    private final Map<String, Image> monsterImages = new HashMap<>();
    private StackPane[] cellViews = new StackPane[100]; 
    private ImageView playerToken, opponentToken;
    private int cardsRemaining = 25;
    private Label cardPileLabel;


    // ==========================================
    // 2. INITIALIZATION
    // ==========================================
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
        
        playerToken.getStyleClass().clear();
        playerToken.getStyleClass().add("player-glow");
        opponentToken.getStyleClass().clear();
        opponentToken.getStyleClass().add("opponent-glow");

        cardPileLabel = new Label("🃏 CARDS PILE: 25 / 25 LEFT");
        cardPileLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-padding: 10px;");
        cardPileLabel.setWrapText(true);
        playerDashboard.getChildren().add(cardPileLabel);
        
        refreshAllUI();
        updateCardPileUI();
        drawTransportArrows();
        startBackgroundMusic();
        
        Platform.runLater(this::setupDebugControls);
    }
    
    private void setupDebugControls() {
        if (boardGrid.getScene() != null) {
            boardGrid.getScene().setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.W) {
                    triggerGameOver(game.getPlayer().getName(), game.getPlayer().getRole().toString(), "#00ffcc");
                } else if (event.getCode() == KeyCode.E) {
                    try {
                        Monster active = game.getCurrent();
                        active.setEnergy(active.getEnergy() + 20); 
                        refreshAllUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    // ==========================================
    // 3. CORE GAME LOOP
    // ==========================================
    @FXML
    private void handleRollDice(ActionEvent event) {
        Monster activeMonster = game.getCurrent();
        Monster opponent = game.getCurrent() == game.getPlayer() ? game.getOpponent() : game.getPlayer();
        
        int oldPos = activeMonster.getPosition();
        int oldEnergy = activeMonster.getEnergy();
        int oldOppEnergy = opponent.getEnergy();

        try {
            // 1. The backend engine instantly calculates the turn in memory
            int roll = game.playTurn();

            if (roll == 0) {
                gameLogLabel.setText("❄️ " + activeMonster.getName() + " is frozen and skipped their turn!");
                return;
            }

            // 2. Trigger the Cinematic Dice Roll!
            // Everything inside the { } will wait until the 0.5s animation finishes.
            animateDiceRoll(roll, () -> {
                
                int newPos = activeMonster.getPosition();
                int newEnergy = activeMonster.getEnergy();
                int newOppEnergy = opponent.getEnergy();
                Card drawnCard = Board.getLastDrawnCard();

                int expectedMove = roll; 
                if (activeMonster instanceof Dasher) { 
                    Dasher m = (Dasher) activeMonster;
                    expectedMove = m.getMomentumTurns() > 0 ? roll * 3 : roll * 2;
                } else if (activeMonster instanceof MultiTasker) { 
                    MultiTasker m = (MultiTasker) activeMonster;
                    if (m.getNormalSpeedTurns() == 0) expectedMove = roll / 2;
                }
                
                int expectedPos = (oldPos + expectedMove) % Constants.BOARD_SIZE;
                int cols = Constants.BOARD_COLS;
                int row = expectedPos / cols;
                int col = expectedPos % cols;
                if (row % 2 == 1) col = cols - 1 - col;

                Cell landedCell = game.getBoard().getBoardCells()[row][col]; 
                StringBuilder log = new StringBuilder("🎲 " + activeMonster.getName() + " rolled a " + roll + ". ");

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
                        log.append("Energies Swapped! ");
                    }
                } else if (landedCell instanceof DoorCell) {
                    log.append("🚪 Landed on a Door! ");
                } else {
                    log.append("Landed safely on Cell ").append(newPos).append(". ");
                }

                int energyDiff = newEnergy - oldEnergy;
                if (energyDiff > 0 && !(landedCell instanceof MonsterCell)) {
                    log.append("🔋 Gained ").append(energyDiff).append(" Energy.");
                } else if (energyDiff < 0 && !(landedCell instanceof MonsterCell)) {
                    log.append("💥 Lost ").append(Math.abs(energyDiff)).append(" Energy.");
                }

                gameLogLabel.setText(log.toString().trim());
                
                // Re-enable the dice button for the next turn, refresh UI, and check for a winner!
                btnRollDice.setDisable(false); 
                refreshAllUI();
                checkWinCondition();
            });

        } catch (InvalidMoveException e) {
            // 3. If they crash, we STILL want to animate the dice roll so they see what they rolled!
            int failedRoll = game.getLastRoll(); 
            
            animateDiceRoll(failedRoll, () -> {
                StringBuilder errorLog = new StringBuilder("🚫 " + activeMonster.getName() + " rolled a " + failedRoll + " but crashed! ");
                
                int currentEnergy = activeMonster.getEnergy();
                if (currentEnergy < oldEnergy) {
                    errorLog.append("💥 Lost ").append(oldEnergy - currentEnergy).append(" Energy from trap! ");
                }
                
                Card burntCard = Board.getLastDrawnCard();
                if (burntCard != null) {
                    errorLog.append("🃏 Burnt a ").append(burntCard.getName()).append("! ");
                    Board.clearLastDrawnCard(); 
                }

                errorLog.append("Roll again.");
                gameLogLabel.setText(errorLog.toString());
                showErrorPopup("Invalid Move: " + e.getMessage());
                refreshAllUI();
            });
            
        } catch (Exception e) {
            showErrorPopup("An error occurred: " + e.getMessage());
            btnRollDice.setDisable(false);
        }
    }

    @FXML
    private void handleUsePowerup(ActionEvent event) {
        try {
            Monster activeMonster = game.getCurrent();
            game.usePowerup();
            
            if (activeMonster instanceof Dasher) {
                gameLogLabel.setText("⚡ " + activeMonster.getName() + " activated Momentum Rush! (3x Speed)");
            } else if (activeMonster instanceof MultiTasker) {
                gameLogLabel.setText("🎯 " + activeMonster.getName() + " activated Focus Mode! (Normal Speed)");
            } else if (activeMonster instanceof Dynamo) {
                gameLogLabel.setText("❄️ " + activeMonster.getName() + " used Screech Freeze!");
            } else if (activeMonster instanceof Schemer) {
                gameLogLabel.setText("🦇 " + activeMonster.getName() + " used Chain Attack!");
            }
            
        } catch (OutOfEnergyException e) {
            showErrorPopup("Cannot use powerup: " + e.getMessage());
        } catch (Exception e) {
            showErrorPopup("Error: " + e.getMessage());
        } finally {
        	refreshAllUI();
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


    // ==========================================
    // 4. UI UPDATERS (THE MACRO SYSTEM)
    // ==========================================
    private void refreshAllUI() {
    	updateCardPileUI();
        updateDashboards();
        updateTokenPositions();
        updateDoorVisuals();
        updateMonsterCellVisuals();
    }

    private void updateDashboards() {
        Monster p = game.getPlayer();
        Monster o = game.getOpponent();
        Monster c = game.getCurrent();
        
        playerDashboard.getStyleClass().remove("dashboard-active");
        opponentDashboard.getStyleClass().remove("dashboard-active");
        
        if (c == p) {
            pTitleLabel.setText("▶ PLAYER\n (YOUR TURN)");
            oTitleLabel.setText("OPPONENT");
            playerDashboard.getStyleClass().add("dashboard-active");
            
        } else {
            pTitleLabel.setText("PLAYER");
            oTitleLabel.setText("▶ OPPONENT\n (YOUR TURN)");
            opponentDashboard.getStyleClass().add("dashboard-active");
        }

        pNameLabel.setText("Name: " + p.getName());
        pTypeLabel.setText("Type: " + getMonsterTypeString(p)); 
        pOriginalRoleLabel.setText("Original Role: " + p.getOriginalRole());
        pCurrentRoleLabel.setText("Current Role: " + p.getRole());
        pEnergyLabel.setText("Energy: " + p.getEnergy());
        pPositionLabel.setText("Position: Cell " + p.getPosition());
        pStatusLabel.setText(buildStatusString(p));
        pAbilityLabel.setText(getMonsterAbilities(p));
        
        oNameLabel.setText("Name: " + o.getName());
        oTypeLabel.setText("Type: " + getMonsterTypeString(o)); 
        oOriginalRoleLabel.setText("Original Role: " + o.getOriginalRole());
        oCurrentRoleLabel.setText("Current Role: " + o.getRole());
        oEnergyLabel.setText("Energy: " + o.getEnergy());
        oPositionLabel.setText("Position: Cell " + o.getPosition());
        oStatusLabel.setText(buildStatusString(o));
        oAbilityLabel.setText(getMonsterAbilities(o));
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
        animateTokenLanding(playerToken);
        animateTokenLanding(opponentToken);
        playerToken.toFront(); opponentToken.toFront();
    }

    private void updateDoorVisuals() {
        ColorAdjust exhaustedEffect = new ColorAdjust();
        exhaustedEffect.setSaturation(-1.0); 
        exhaustedEffect.setBrightness(-0.3); 

        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            int[] pos = indexToRowCol(i);
            int row = pos[0];
            int col = pos[1];

            if (cells[row][col] instanceof DoorCell && ((DoorCell) cells[row][col]).isActivated()) {
            	for (Node node : cellViews[i].getChildren()) {
                    if (node != playerToken && node != opponentToken) {
                        node.setEffect(exhaustedEffect);
                        node.setOpacity(0.5);
                    }
            	}
            }
        }
    }
    
    private void updateMonsterCellVisuals() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
        	int[] pos = indexToRowCol(i);
            int row = pos[0];
            int col = pos[1];

            if (cells[row][col] instanceof MonsterCell) {
                MonsterCell mCell = (MonsterCell) cells[row][col];
                String currentEnergy = mCell.getCellMonster().getEnergy() + ""; 

                for (Node node : cellViews[i].getChildren()) {
                    if (node instanceof Label && node.getStyleClass().contains("monster-energy-label")) {
                        ((Label) node).setText(currentEnergy);
                        break; 
                    }
                }
            }
        }
    }


    // ==========================================
    // 5. ANIMATIONS & DYNAMIC BUILDERS
    // ==========================================
    private void buildGrid() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
        	int[] pos = indexToRowCol(i);
            int row = pos[0];
            int col = pos[1];

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

        if (cell instanceof MonsterCell) {
            MonsterCell mCell = (MonsterCell) cell;
            String monsterName = mCell.getName().trim();
            Image specificMonsterImg = monsterImages.get(monsterName);
            
            if (specificMonsterImg != null) bgView.setImage(specificMonsterImg);
            
            Label roleLbl = new Label(mCell.getCellMonster().getRole() == Role.SCARER ? "S" : "L");
            roleLbl.getStyleClass().add("monster-role-label");
            StackPane.setAlignment(roleLbl, Pos.TOP_RIGHT); 
            
            Label nrgLbl = new Label(mCell.getCellMonster().getEnergy() + "");
            nrgLbl.getStyleClass().add("monster-energy-label");
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

        if (addImage) stack.getChildren().add(bgView);

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
            	int[] pos = indexToRowCol(i);
                int row = pos[0];
                int col = pos[1];

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
    
    public void initCardPile() {
        cardPileContainer.getChildren().clear();
        for (int i = 0; i < 25; i++) {
            Region cardLayer = new Region();
            cardLayer.setPrefSize(80, 120); 
            cardLayer.getStyleClass().add("card-pile");
            cardLayer.setTranslateX(i * 0.15); 
            cardLayer.setTranslateY(i * -0.15);
            cardPileContainer.getChildren().add(cardLayer);
        }
    }

    private void updateCardPileUI() {
        if (cardPileLabel != null) {
            cardPileLabel.setText("🃏 CARD PILE: " + cardsRemaining + " / 25 LEFT");
        }
    }

    private void decrementCardPile() {
        cardsRemaining--;
        if (cardsRemaining <= 0) {
            cardsRemaining = 25; 
            System.out.println("🔄 Deck pile exhausted! Reshuffling all 25 cards back into the pile.");
        }
        updateCardPileUI();
    }

    public void removeTopCardFromPile() {
        if (cardPileContainer.getChildren().getLast() == cardPileContainer.getChildren().getFirst()) {
        	reshufflePile(); 
        }

        int lastIndex = cardPileContainer.getChildren().size() - 1;
        Node topCard = cardPileContainer.getChildren().get(lastIndex);

        FadeTransition fade = new FadeTransition(Duration.millis(500), topCard);
        fade.setToValue(0); 
        fade.setOnFinished(e -> cardPileContainer.getChildren().remove(topCard));
        fade.play();
    }
    
    public void reshufflePile() {
        initCardPile();
        cardPileContainer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), cardPileContainer);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    public void animateCardFromPile(String cardDescription) {
        boardGrid.setDisable(true);
        btnRollDice.setDisable(true);
        btnPowerup.setDisable(true);

        Bounds pileBounds = cardPileContainer.localToScene(cardPileContainer.getBoundsInLocal());
        double pileX = pileBounds.getMinX() + (pileBounds.getWidth() / 2);
        double pileY = pileBounds.getMinY() + (pileBounds.getHeight() / 2);

        Rectangle flyingCard = new Rectangle(80, 120);
        flyingCard.setStyle("-fx-fill: #0b61d9; -fx-stroke: white; -fx-stroke-width: 4; -fx-arc-width: 20; -fx-arc-height: 20;");
        mainRootPane.getChildren().add(flyingCard);

        Bounds rootBounds = mainRootPane.localToScene(mainRootPane.getBoundsInLocal());
        double centerX = rootBounds.getMinX() + (rootBounds.getWidth() / 2);
        double centerY = rootBounds.getMinY() + (rootBounds.getHeight() / 2);

        flyingCard.setTranslateX(pileX - centerX);
        flyingCard.setTranslateY(pileY - centerY);
        
        TranslateTransition move = new TranslateTransition(Duration.seconds(0.6), flyingCard);
        move.setToX(0); 
        move.setToY(0);

        RotateTransition rotate = new RotateTransition(Duration.seconds(0.6), flyingCard);
        rotate.setByAngle(360);
        rotate.setAxis(Rotate.Y_AXIS);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.6), flyingCard);
        scale.setToX(3.5); 
        scale.setToY(3.5);

        ParallelTransition pt = new ParallelTransition(move, rotate, scale);
        pt.setOnFinished(e -> {
            mainRootPane.getChildren().remove(flyingCard);
            showCardTransition(cardDescription);
        });

        pt.play();
    }
    
    public void showCardTransition(String cardDescription) {
        Region darkOverlay = new Region();
        darkOverlay.getStyleClass().add("card-transition-overlay");

        VBox cardBox = new VBox(20);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setMaxSize(320, 480); 
        cardBox.getStyleClass().add("card-transition-box");

        Label headerLabel = new Label("CARD DRAWN");
        headerLabel.getStyleClass().add("card-transition-title");

        Label descLabel = new Label(cardDescription);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.getStyleClass().add("card-transition-desc");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("card-transition-button");

        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        okButton.setOnAction(e -> {
            mainRootPane.getChildren().removeAll(darkOverlay, cardBox);
            boardGrid.setDisable(false);
            btnRollDice.setDisable(false);
            btnPowerup.setDisable(false);
        });

        cardBox.getChildren().addAll(headerLabel, topSpacer, descLabel, bottomSpacer, okButton);
        mainRootPane.getChildren().addAll(darkOverlay, cardBox);

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


    // ==========================================
    // 6. POPUPS & UTILITIES
    // ==========================================
    private void showErrorPopup(String msg) {
        errorPopupMessage.setText(msg); 
        errorPopupOverlay.setVisible(true);
        boardGrid.setDisable(true); 
        btnRollDice.setDisable(true); 
        btnPowerup.setDisable(true);
    }

    @FXML
    private void closeActivePopup(ActionEvent event) {
        if (errorPopupOverlay != null) errorPopupOverlay.setVisible(false);
        
        boardGrid.setDisable(false); 
        btnRollDice.setDisable(false); 
        btnPowerup.setDisable(false);
    }
    
    private void triggerGameOver(String name, String role, String color) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/gameOver.fxml"));
            Parent root = loader.load();
            GameOverController pc = loader.getController();
            
            pc.setGameStats(name, role, game.getPlayer().getEnergy(), game.getOpponent().getEnergy(), color);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.TRANSPARENT); 
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT); 
            
            popup.setScene(scene);
            popup.centerOnScreen();
            
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
            }
            playWinSound(role);
            popup.showAndWait(); 

            Stage mainStage = (Stage) boardGrid.getScene().getWindow(); 
            mainStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/resources/fxml/welcome.fxml")).load(), 1024, 768));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowRules(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/popup_rules.fxml"));
            StackPane rulesPopup = loader.load();
            rulesPopup.prefWidthProperty().bind(mainRootPane.widthProperty());
            rulesPopup.prefHeightProperty().bind(mainRootPane.heightProperty());
            mainRootPane.getChildren().add(rulesPopup);
            rulesPopup.toFront();
        } catch (Exception e) {
            System.err.println("Could not load rules popup!");
            e.printStackTrace();
        }
    }

    private void startBackgroundMusic() {
        try {
            URL audioUrl = getClass().getResource("/resources/music/The Scare Floor.mp3");
            if (audioUrl != null) {
                Media media = new Media(audioUrl.toString());
                backgroundMusicPlayer = new MediaPlayer(media);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(0.3); 
                backgroundMusicPlayer.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playWinSound(String role) {
        try {
            String soundFile = "";
            if ("Scarer".equalsIgnoreCase(role)) {
                soundFile = "/resources/music/ScarerWinning.mp3";
            } else if ("Laugher".equalsIgnoreCase(role)) {
                soundFile = "/resources/music/laugherWinning.mp3";
            } else {
                return; 
            }

            URL audioUrl = getClass().getResource(soundFile);
            if (audioUrl != null) {
                Media media = new Media(audioUrl.toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMonsterTypeString(Monster m) {
        if (m instanceof Dasher) return "Dasher";
        if (m instanceof Dynamo) return "Dynamo";
        if (m instanceof MultiTasker) return "Multitasker";
        return "Schemer";
    }

    private String buildStatusString(Monster m) {
        StringBuilder status = new StringBuilder();
        if (m.isFrozen()) status.append("❄️ Frozen (1 turn left)\n");
        
        if (m.isConfused()) {
        	int turns = m.getConfusionTurns();
        	if (turns > 0) status.append("🌀 Confused (").append(turns).append(" left)\n");
        }
        
        if (m.isShielded()) status.append("🛡️ Shielded\n");
        
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
            return "⚡ Powerup: Screech Freeze (Freezes opponent for 1 turn)\n✨ Passive: Doubles all incoming energy changes";
        } else if (m instanceof Dasher) {
            return "⚡ Powerup: Momentum Rush (3x speed for 3 turns)\n✨ Passive: Always moves at 2x the dice roll";
        } else if (m instanceof MultiTasker) {
            return "⚡ Powerup: Focus Mode (Normal speed for 2 turns)\n✨ Passive: Moves at 1/2 dice roll, +200 to all energy changes";
        } else if (m instanceof Schemer) {
            return "⚡ Powerup: Chain Attack (Steals energy from all stationed monsters)\n✨ Passive: Gains +10 bonus to all energy changes";
        }
        return "No special abilities.";
    }

    private String getCardDescription(Card card) {
        if (card instanceof SwapperCard) {
            return "Swapper Card!\nIf you are behind, you will swap positions with the opponent!";
        } else if (card instanceof EnergyStealCard) {
            return "Energy Steal!\nOpponent's Energy will be transferred to you (unless they have a shield!).";
        } else if (card instanceof ShieldCard) {
            return "Shield Card!\nYou will gain a shield against the next negative energy effect, and the opponent's shield will be destroyed if they have one.";
        } else if (card instanceof StartOverCard) {
            return "Start Over!\nSomeone will get sent all the way back to Cell 0!";
        } else if (card instanceof ConfusionCard) {
            return "Confusion!\nBoth monsters will swap roles! Doors will now have reversed effects.";
        }
        return "A mysterious card was drawn!";
    }
    private int[] indexToRowCol(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        return new int[]{row, col};
    }
    private void animateTokenLanding(ImageView token) {
        // Step 1: Shrink slightly and squash down (the impact)
        ScaleTransition squash = new ScaleTransition(Duration.millis(150), token);
        squash.setToX(1.3); // Get wider
        squash.setToY(0.7); // Get shorter
        squash.setInterpolator(Interpolator.EASE_OUT);

        // Step 2: Spring back up (the rebound)
        ScaleTransition stretch = new ScaleTransition(Duration.millis(150), token);
        stretch.setToX(0.9);
        stretch.setToY(1.1);
        stretch.setInterpolator(Interpolator.EASE_IN);

        // Step 3: Settle back to normal size
        ScaleTransition settle = new ScaleTransition(Duration.millis(100), token);
        settle.setToX(1.0);
        settle.setToY(1.0);

        // Play them in sequence!
        SequentialTransition bounce = new SequentialTransition(squash, stretch, settle);
        bounce.play();
    }
    private void spawnFloatingText(String text, Color color, int cellIndex) {
        // 1. Find the physical location of the cell on the screen
        Bounds cellBounds = cellViews[cellIndex].localToScene(cellViews[cellIndex].getBoundsInLocal());
        
        // 2. Create the floating label
        Label floatingText = new Label(text);
        floatingText.setStyle("-fx-font-family: 'Monster AG'; -fx-font-size: 24px; -fx-font-weight: bold;");
        floatingText.setTextFill(color);
        floatingText.setEffect(new DropShadow(10, Color.BLACK)); // Make it readable anywhere

        // 3. Position it dead center over the cell
        floatingText.setLayoutX(cellBounds.getMinX() + 10);
        floatingText.setLayoutY(cellBounds.getMinY() - 10);
        
        // Add to the highest layer of your UI (mainRootPane)
        mainRootPane.getChildren().add(floatingText);

        // 4. Animate it floating UP and fading OUT over 1.5 seconds
        TranslateTransition floatUp = new TranslateTransition(Duration.seconds(1.5), floatingText);
        floatUp.setByY(-50); // Move up 50 pixels

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), floatingText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(floatUp, fadeOut);
        
        // CRITICAL: Delete the label after the animation finishes so it doesn't lag the game
        pt.setOnFinished(e -> mainRootPane.getChildren().remove(floatingText));
        pt.play();
    }
    private void animateDiceRoll(int finalRoll, Runnable onRollFinished) {
        btnRollDice.setDisable(true); // Stop double clicking
        
        Timeline timeline = new Timeline();
        // Rapidly change the text every 50 milliseconds
        for (int i = 0; i < 10; i++) {
            KeyFrame kf = new KeyFrame(Duration.millis(i * 50), e -> {
                int randomFace = (int)(Math.random() * 6) + 1;
                gameLogLabel.setText("🎲 Rolling... " + randomFace);
            });
            timeline.getKeyFrames().add(kf);
        }
        
        // The grand finale: show the real roll and run the rest of your turn logic
        KeyFrame finale = new KeyFrame(Duration.millis(500), e -> {
            gameLogLabel.setText("🎲 Rolled a " + finalRoll + "!");
            onRollFinished.run(); // This executes your movement code!
        });
        timeline.getKeyFrames().add(finale);
        
        timeline.play();
    }
}