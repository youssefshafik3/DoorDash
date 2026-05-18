package game.controller;

import java.util.HashMap;
import java.util.Map;

import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardController {
	
	private Game game;
	
	@FXML
	private GridPane boardGrid;
	// Player Dashboard Labels
	@FXML
	private Label pNameLabel, pTypeLabel, pOriginalRoleLabel, pCurrentRoleLabel, pEnergyLabel, pPositionLabel, pStatusLabel;

	// Opponent Dashboard Labels
	@FXML
	private Label oNameLabel, oTypeLabel, oOriginalRoleLabel, oCurrentRoleLabel, oEnergyLabel, oPositionLabel, oStatusLabel;
	
	@FXML private Button btnRollDice;
    @FXML private Button btnPowerup;
    @FXML private Label gameLogLabel;

    @FXML
    private StackPane errorPopupOverlay;
    @FXML
    private Label errorPopupMessage;
    
	private Image sockImg;
	private Image cardImg;
	private Image conveyorImg;
	private Image[] doorImages;
	private final Map<String, Image> monsterImages = new HashMap<>();
	
	private StackPane[] cellViews = new StackPane[100]; // Caches the UI cells for instant access
	private ImageView playerToken;
	private ImageView opponentToken;
	
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
			
		}
		catch (NullPointerException e) {
	        System.err.println("CRITICAL: An image file is missing! Check your spelling and extensions (.png vs .jpg)");
	        e.printStackTrace();
	    }
		buildGrid();
		String pName = game.getPlayer().getName();
		String oName = game.getOpponent().getName();

		// 2. Create the ImageViews for the tokens
		playerToken = new ImageView(monsterImages.get(pName));
		opponentToken = new ImageView(monsterImages.get(oName));

		// 3. Make them smaller than the 60x60 cell so they fit nicely
		playerToken.setFitWidth(30);
		playerToken.setFitHeight(30);
		opponentToken.setFitWidth(30);
		opponentToken.setFitHeight(30);

		// Optional: Add a drop shadow so they look like physical pieces sitting on the board
		String dropShadowCSS = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);";
		playerToken.setStyle(dropShadowCSS);
		opponentToken.setStyle(dropShadowCSS);

		// 4. Place them on the board for the first time
		updateTokenPositions();
		updateDashboards();
    }
	
	private void buildGrid() {
	    // Assuming your engine instance is already initialized
	    Cell[][] boardCells = game.getBoard().getBoardCells();
	    int cols = Constants.BOARD_COLS;
	    int rows = Constants.BOARD_ROWS;
	    int size = Constants.BOARD_SIZE;

	    for (int i = 0; i < size; i++) {
	        int logicalRow = i / cols;
	        int logicalCol = i % cols;
	        if (logicalRow % 2 == 1) {
	            logicalCol = cols - 1 - logicalCol;
	        }

	        // Fetch the backend cell
	        Cell cellData = boardCells[logicalRow][logicalCol];

	        // 2. Calculate GUI Coordinates (Invert the Row for JavaFX)
	        int guiRow = (rows - 1) - logicalRow;
	        int guiCol = logicalCol;

	        // 3. Create the Visual Node
	        StackPane cellView = createCellView(cellData, i);
	        cellViews[i] = cellView;

	        // 4. Add to GridPane
	        boardGrid.add(cellView, guiCol, guiRow);
	    }
	}
	
	private StackPane createCellView(Cell cell, int index) {
	    StackPane stack = new StackPane();
	    stack.setMinSize(60, 60);
	    stack.setMaxSize(60, 60);
	    stack.setPrefSize(60, 60);
	    stack.getStyleClass().add("grid-cell");

	    ImageView backgroundView = new ImageView();
	    backgroundView.setFitWidth(58);
	    backgroundView.setFitHeight(58);
	    
	    boolean addStandardImage = true; // Flag to handle standard image additions

	    if (cell instanceof MonsterCell) {
	        MonsterCell mCell = (MonsterCell) cell;
	        String monsterName = mCell.getName().trim();
	        Image specificMonsterImg = monsterImages.get(monsterName);
	        
	        if (specificMonsterImg != null) {
	            backgroundView.setImage(specificMonsterImg);
	        } else {
	            System.err.println("Warning: No image found for monster " + monsterName);
	        }
	    } 
	    else if (cell instanceof ConveyorBelt) {
	        backgroundView.setImage(conveyorImg);
	    } 
	    else if (cell instanceof ContaminationSock) {
	        backgroundView.setImage(sockImg);
	    } 
	    else if (cell instanceof CardCell) {
	        backgroundView.setImage(cardImg);
	    } 
	    else if (cell instanceof DoorCell) {
	        if (index == 99) {
	            backgroundView.setImage(doorImages[4]);
	        } else {
	            backgroundView.setImage(doorImages[(index / 2) % 4]);
	        }
	        
	        DoorCell dCell = (DoorCell) cell;
	        String roleText = (dCell.getRole() == Role.SCARER) ? "S" : "L";
	        String energyText = dCell.getEnergy() + "";
	        
	        Label roleLabel = new Label(roleText);
	        roleLabel.getStyleClass().add("door-role-label");
	        StackPane.setAlignment(roleLabel, Pos.TOP_RIGHT); // Pinned to top-right
	        
	        Label energyLabel = new Label(energyText);
	        energyLabel.getStyleClass().add("door-energy-label");
	        StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER); // Corrected alignment target
	        
	        stack.getChildren().addAll(backgroundView, roleLabel, energyLabel);
	        addStandardImage = false; // Already added to stack, don't duplicate
	    } 
	    else {
	        // Normal Cell: Solid fill
	        Rectangle bg = new Rectangle(60, 60);
	        bg.getStyleClass().add("normal-cell-bg");
	        stack.getChildren().add(bg);
	        addStandardImage = false; // No image for normal cells
	    }

	    // Add the correctly sized background view for Monsters, Conveyors, Socks, and Cards
	    if (addStandardImage) {
	        stack.getChildren().add(backgroundView);
	    }

	    // Layer: Index Number
	    Label indexLabel = new Label(String.valueOf(index));
	    indexLabel.getStyleClass().add("cell-index-label");
	    StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);
	    stack.getChildren().add(indexLabel);

	    return stack;
	}
	private void updateDashboards() {
	    Monster player = game.getPlayer();
	    Monster opponent = game.getOpponent();
	    String pType;
	    String oType;

	    // --- Update Player Dashboard ---
	    pNameLabel.setText("Name: " + player.getName());
	    if (player instanceof Dasher)
	    	pType = "Dasher";
	    else if (player instanceof Dynamo)
	    	pType = "Dynamo";
	    else if (player instanceof MultiTasker)
	    	pType = "Multitasker";
	    else
	    	pType = "Schemer";
	    pTypeLabel.setText("Type: " + pType); 
	    pOriginalRoleLabel.setText("Original Role: " + player.getOriginalRole());
	    pCurrentRoleLabel.setText("Current Role: " + player.getRole());
	    pEnergyLabel.setText("Energy: " + player.getEnergy());
	    pPositionLabel.setText("Position: Cell " + player.getPosition());
	    pStatusLabel.setText(buildStatusString(player));

	    // --- Update Opponent Dashboard ---
	    oNameLabel.setText("Name: " + opponent.getName());
	    if (opponent instanceof Dasher)
	    	oType = "Dasher";
	    else if (opponent instanceof Dynamo)
	    	oType = "Dynamo";
	    else if (opponent instanceof MultiTasker)
	    	oType = "Multitasker";
	    else
	    	oType = "Schemer";
	    oTypeLabel.setText("Type: " + oType); 
	    oOriginalRoleLabel.setText("Original Role: " + opponent.getRole());
	    oCurrentRoleLabel.setText("Current Role: " + opponent.getRole());
	    oEnergyLabel.setText("Energy: " + opponent.getEnergy());
	    oPositionLabel.setText("Position: Cell " + opponent.getPosition());
	    oStatusLabel.setText(buildStatusString(opponent));
	}

	private String buildStatusString(Monster m) {
	    StringBuilder status = new StringBuilder();
	    
	    if (m.isFrozen()) status.append("❄️ Frozen (Skips next turn)\n");
	    if (m.isConfused()) status.append("🌀 Confused!\n"); 
	    if (m.isShielded()) status.append("🛡️ Shield Active\n");
	    
	    return status.length() == 0 ? "None" : status.toString();
	}
	@FXML
	private void handleRollDice(ActionEvent event) {
	    try {
	        // 1. Tell the backend to play the turn
	        Monster activeMonster = game.getCurrent();
	        game.playTurn(); 
	        
	        // 2. Update the UI upon success
	        gameLogLabel.setText(activeMonster.getName() + " rolled the dice and moved!");
	        updateDashboards();
	        
	        // 3. Move the physical tokens to their new cells
	        // ---> DELETED boardGrid.getChildren().clear() and buildGrid() <---
	        updateTokenPositions(); 
	        
	        // 4. Check if someone won
	        checkWinCondition();               

	    } catch (InvalidMoveException e) {
	        // Catch the specific exception from your backend and show it
	        showErrorPopup("Invalid Move: " + e.getMessage());
	    } catch (Exception e) {
	        // Catch any unexpected errors without crashing
	        showErrorPopup("An error occurred: " + e.getMessage());
	    }
	}
	@FXML
    private void handleUsePowerup(ActionEvent event) {
        try {
            Monster activeMonster = game.getCurrent();
            game.usePowerup();
            
            gameLogLabel.setText(activeMonster.getName() + " activated their powerup!");
            updateDashboards(); // Update energy levels on the UI
            
        } catch (OutOfEnergyException e) {
            showErrorPopup("Cannot use powerup: " + e.getMessage());
        } catch (Exception e) {
            showErrorPopup("Error: " + e.getMessage());
        }
    }
	private void showErrorPopup(String message) {
        errorPopupMessage.setText(message);
        errorPopupOverlay.setVisible(true);
        // Optional: Disable the main board while popup is showing
        boardGrid.setDisable(true); 
        btnRollDice.setDisable(true);
        btnPowerup.setDisable(true);
    }

    @FXML
    private void hideErrorPopup(ActionEvent event) {
        errorPopupOverlay.setVisible(false);
        // Re-enable the main board
        boardGrid.setDisable(false);
        btnRollDice.setDisable(false);
        btnPowerup.setDisable(false);
    }
    private void checkWinCondition() {
        Monster winner = game.getWinner();
        if (winner != null) {
            gameLogLabel.setText("GAME OVER! " + winner.getName() + " wins!");
            // Disable buttons so players can't keep playing
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);
            
            // TODO: Trigger the final Game Over Screen (Milestone Requirement)
        }
    }
    private void updateTokenPositions() {
	    // 1. Get current positions from the backend
	    int pPos = game.getPlayer().getPosition();
	    int oPos = game.getOpponent().getPosition();

	    // 2. Remove tokens from their previous cells (if they were already on the board)
	    if (playerToken.getParent() != null) {
	        ((StackPane) playerToken.getParent()).getChildren().remove(playerToken);
	    }
	    if (opponentToken.getParent() != null) {
	        ((StackPane) opponentToken.getParent()).getChildren().remove(opponentToken);
	    }

	    // 3. Align them so they don't overlap each other if they land on the same cell
	    if (pPos == oPos) {
	        // For Cell 0 (or any shared cell): Place side-by-side
	        StackPane.setAlignment(playerToken, Pos.CENTER_LEFT);
	        StackPane.setAlignment(opponentToken, Pos.CENTER_RIGHT);
	    } else {
	        // For the rest of the game: Dead center
	        StackPane.setAlignment(playerToken, Pos.CENTER);
	        StackPane.setAlignment(opponentToken, Pos.CENTER);
	    }

	    // 4. Add them to their new specific cells using the cellViews cache array
	    cellViews[pPos].getChildren().add(playerToken);
	    cellViews[oPos].getChildren().add(opponentToken);
	    
	    // 5. Bring them to the absolute front so index numbers don't cover them
	    playerToken.toFront();
	    opponentToken.toFront();
	}

}
