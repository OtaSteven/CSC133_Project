import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.util.*;

public class GameApp extends Application {
  Game game;
  Set<KeyCode> keysDown = new HashSet<>();
  @Override
  public void start(Stage primaryStage) {
    game = new Game();
    Scene scene = new Scene(game, Game.GAME_WIDTH, Game.GAME_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("RainMaker");
    primaryStage.setResizable(false);

    scene.setOnKeyPressed(event -> {
      keysDown.add(event.getCode());
      if (keysDown.contains(KeyCode.LEFT))
        game.heliLeft();
      if (keysDown.contains(KeyCode.RIGHT))
        game.heliRight();
      if (keysDown.contains(KeyCode.UP))
        game.heliAccelerate();
      if (keysDown.contains(KeyCode.DOWN))
        game.heliDecelerate();
      if (keysDown.contains(KeyCode.SPACE))
        game.fillCloud();
      if (keysDown.contains(KeyCode.I))
        game.startHelicopter();
      if (keysDown.contains(KeyCode.B))
        game.turnOnBoundary();
      if (keysDown.contains(KeyCode.R))
        game.init();
    });

    scene.setOnKeyReleased(event -> keysDown.remove(event.getCode()));

    game.run();
    primaryStage.show();
  }
  public static void main(String[] args) {
        launch(args);
    }
}