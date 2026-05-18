package game.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import game.engine.Game;
import game.engine.Role;

public class RoleSelectionController implements Initializable {
	@FXML
	private Pane rootPane;
	
    @FXML
    private Button btnScarer;
    @FXML
    private Button btnLaugher;
    @FXML
    private Label lblScarer;
    @FXML
    private Label lblLaugher;

    // SFX (Short sounds)
    private AudioClip scareSFX;
    private AudioClip laughSFX;

    // Background Themes (Looping music)
    private MediaPlayer evilMusic;
    private MediaPlayer funnyMusic;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	// 1. Load your music and sound effects
        loadResources();
        
        // 2. Set up the hover listeners for your buttons
        setupRoleEffects(btnScarer, scareSFX, evilMusic);
        setupRoleEffects(btnLaugher, laughSFX, funnyMusic);
        
        // 3. Load the CSS with the .toExternalForm() fix
        try {
            URL cssURL = getClass().getResource("/resources/css/roleSelection.css");
            if (cssURL != null) {
                rootPane.getStylesheets().add(cssURL.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("CSS not found, skipping styles.");
        }
    }

    private void loadResources() {
        try {
            // Load SFX
            URL sfxS = getClass().getResource("/resources/music/scarer.mp3");
            URL sfxL = getClass().getResource("/resources/music/laugher.mp3");
            if (sfxS != null) scareSFX = new AudioClip(sfxS.toExternalForm());
            if (sfxL != null) laughSFX = new AudioClip(sfxL.toExternalForm());

            // Load Background Themes
            URL musicE = getClass().getResource("/resources/music/scarerBackground.mp3");
            URL musicF = getClass().getResource("/resources/music/laugherBackground.mp3");
            
            if (musicE != null) evilMusic = createLoopingPlayer(musicE);
            if (musicF != null) funnyMusic = createLoopingPlayer(musicF);
            
        } catch (Exception e) {
            System.err.println("Note: Some audio files are missing. Game continuing..."); 
        }
    }

    private MediaPlayer createLoopingPlayer(URL resource) {
        MediaPlayer player = new MediaPlayer(new Media(resource.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(0.4); // Background music should be quieter than SFX
        return player;
    }

    private void setupRoleEffects(Button button, AudioClip sfx, MediaPlayer music) {
        button.setOnMouseEntered(event -> {
            if (sfx != null) sfx.play();
            if (music != null) {
                music.stop(); // Restart from beginning
                music.play();
                
            }
            button.setScaleX(1.1); 
            button.setScaleY(1.1);
        });

        button.setOnMouseExited(event -> {
            if(sfx!=null)
            	sfx.stop();
            
        	if (music != null) music.pause();
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }
    @FXML
    private void handleScarerSelection(ActionEvent event) {
        transitionToBoard(Role.SCARER, event);
    }

    @FXML
    private void handleLaugherSelection(ActionEvent event) {
        transitionToBoard(Role.LAUGHER, event);
    }

    private void transitionToBoard(Role chosenRole, ActionEvent event) {
        // 1. Stop all audio before leaving the screen
        if (evilMusic != null) evilMusic.stop();
        if (funnyMusic != null) funnyMusic.stop();
        if (scareSFX != null) scareSFX.stop();
        if (laughSFX != null) laughSFX.stop();

        try {
            // 2. Initialize the backend Game model using the selected role
            Game gameModel = new Game(chosenRole);
            System.out.println(gameModel.getOpponent().getName());
            System.out.println(gameModel.getCurrent().getName());

            // 3. Load the Board FXML
            // (Make sure the path matches where your Board.fxml actually lives)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/board.fxml")); 
            Parent boardRoot = loader.load();

            // 4. Pass the initialized Game to the BoardController
            BoardController boardController = loader.getController();
            boardController.initData(gameModel);

            // 5. Swap the Scene on the current Stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(boardRoot,1200,800));
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load the game board or initialize the engine.");
            e.printStackTrace();
        }
    }
    
}