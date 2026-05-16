package game.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.ResourceBundle;

public class RoleSelectionController implements Initializable {
	@FXML private Pane rootPane;
	
    @FXML private Button btnScarer;
    @FXML private Button btnLaugher;
    @FXML private Label lblScarer;
    @FXML private Label lblLaugher;

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
}