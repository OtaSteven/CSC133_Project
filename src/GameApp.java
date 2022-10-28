import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.Random;

interface Updatable
{
  void update();
}
class Game extends Pane
{
  public Game()
  {
    setScaleY(-1);
    getChildren().add(new Pond());
    getChildren().add(new HeliPad());
    getChildren().add(new Helicopter());
  }
}
abstract class GameObject extends Group implements Updatable
{
  public void update()
  {
    for (Node n : getChildren())
    {
      if (n instanceof Updatable)
        ((Updatable)n).update();
    }
  }
  public void add(Node node)
  {
    getChildren().add(node);
  }
}
class Pond extends GameObject
{
  private Circle pond;
  private Random rand;

  public Pond()
  {
    rand = new Random();
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(25);
    pond.setCenterY(150);
    pond.setCenterX(150);
    update();
    add(pond);
  }

  @Override
  public void update() {
    AnimationTimer loop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        pond.setTranslateX(pond.getTranslateX()+1);
      }
    };
    loop.start();
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
    add(cloud);
  }

  @Override
  public void update() {

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
    heliPad.setY(100);

    padCircle = new Circle();
    padCircle.setRadius(30);
    padCircle.setStroke(Color.YELLOW);
    padCircle.setCenterX(heliPad.getX()+heliPad.getWidth()/2);
    padCircle.setCenterY(heliPad.getY()+heliPad.getHeight()/2);
    add(heliPad);
    add(padCircle);
  }

  @Override
  public void update() {

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
    heli.setCenterY(130);
    heliHead = new Line(heli.getCenterX(), heli.getCenterY(), heli.getCenterX(),
        heli.getCenterY()+25);
    heliHead.setStroke(Color.YELLOW);
    add(heli);
    add(heliHead);
  }

  @Override
  public void update() {

  }
}
class PondAndCloud extends GameObject {

  @Override
  public void update() {

  }
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

