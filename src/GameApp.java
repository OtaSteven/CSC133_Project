import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Random;

class Game extends Pane
{
  public Game()
  {
    getChildren().add(new HeliPad());
    getChildren().add(new Helicopter());
  }
}
abstract class GameObject extends Group
{
}
class Pond extends GameObject
{
  private Circle pond;
  private Random rand;

  public Pond()
  {
    rand = new Random();
    pond = new Circle(25, Color.BLUE);
    pond.setCenterX(rand.nextInt((int)(GameApp.WIDTH)));
  }
}
class Cloud extends GameObject
{
  private Circle cloud;
  private Random rand;

  public Cloud()
  {
    rand = new Random();
    cloud = new Circle(25, Color.BLUE);
    cloud.setCenterX(rand.nextInt((int)(GameApp.WIDTH)));
  }
}
class HeliPad extends GameObject
{
  private Rectangle heliPad;
  private Circle padCircle;

  public HeliPad()
  {
    heliPad = new Rectangle(75,75);
    heliPad.setStroke(Color.GRAY);
    heliPad.setX((GameApp.WIDTH/2)-(heliPad.getWidth()/2));
    heliPad.setY(GameApp.HEIGHT-100);
    padCircle = new Circle();
    padCircle.setRadius(30);
    padCircle.setStroke(Color.YELLOW);
    padCircle.setCenterX(heliPad.getX()+heliPad.getWidth()/2);
    padCircle.setCenterY(heliPad.getY()+heliPad.getHeight()/2);
    getChildren().addAll(heliPad, padCircle);
  }
}
class Helicopter extends GameObject
{
  Circle heli;
  Line heliHead;
  public Helicopter()
  {
    heli = new Circle(10);
    heli.setFill(Color.YELLOW);
    heli.setCenterX((GameApp.WIDTH/2));
    heli.setCenterY((GameApp.HEIGHT)-60);
    heliHead = new Line(heli.getCenterX(), heli.getCenterY(), heli.getCenterX(),
        heli.getCenterY()-25);
    heliHead.setStroke(Color.YELLOW);
    getChildren().addAll(heli, heliHead);
  }
}
class PondAndCloud extends GameObject
{

}
interface Updatable
{

}


public class GameApp extends Application {

  final static double WIDTH = 600;
  final static double HEIGHT = Screen.getPrimary().getBounds().
      getHeight()-100;


  @Override
  public void start(Stage primaryStage) {
    Game game = new Game();
    Scene scene = new Scene(game, WIDTH, HEIGHT);
    scene.setFill(Color.BLACK);

    primaryStage.setScene(scene);
    primaryStage.setTitle("RainMaker");
    primaryStage.setResizable(false);

    primaryStage.show();
  }
  public static void main(String[] args) {
        launch(args);
    }
}

