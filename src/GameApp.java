import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.Random;

interface Updatable
{
  void update();
}
class Game extends Pane
{
  Pond pond;
  HeliPad heliPad;
  Helicopter helicopter;
  public Game()
  {
    pond = new Pond();
    heliPad = new HeliPad();
    helicopter = new Helicopter();
    setScaleY(-1);
    setBackground(new Background(new BackgroundFill(Color.BLACK,
        new CornerRadii(0), Insets.EMPTY)));
    getChildren().add(pond);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
  }
}
abstract class GameObject extends Group implements Updatable
{
  protected Translate myTranslate;
  protected Rotate myRotate;
  protected Scale myScale;
  public GameObject()
  {
    myTranslate = new Translate();
    myRotate = new Rotate();
    myScale = new Scale();
    this.getTransforms().addAll(myTranslate, myRotate, myScale);
  }
  public void translate(double tx, double ty)
  {
    myTranslate.setX(tx);
    myTranslate.setY(ty);
  }
  public void rotate(double degree)
  {
    myRotate.setAngle(degree);
    myRotate.setPivotX(0);
    myRotate.setPivotY(0);
  }
  public void scale(double sx, double sy)
  {
    myScale.setX(sx);
    myScale.setY(sy);
  }
  public double getMyRotate()
  {
    return myRotate.getAngle();
  }
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
class GameText extends GameObject
{
  Text text;
  public GameText()
  {
    this("");
  }
  public GameText(String textString)
  {
    text = new Text(textString);
    text.setScaleY(-1);
    text.setFill(Color.WHITE);
    text.setFont(Font.font(18));
    add(text);
  }
  public void setText(String textString)
  {
    text.setText(textString);
  }
}
class Pond extends GameObject
{
  private Circle pond;
  private Random rand;
  private int pondX, pondY;
  public Pond()
  {
    rand = new Random();
    pond = new Circle();
    pondX = rand.nextInt((int)(GameApp.WIDTH-50));
    pondY = rand.nextInt((int)(GameApp.HEIGHT/2)) + (int)GameApp.HEIGHT/2;
    pond.setFill(Color.BLUE);
    pond.setRadius(25);
    pond.setCenterX(pondX);
    pond.setCenterY(pondY);
    add(pond);

    GameText pondText = new GameText(String.valueOf(
        rand.nextInt(100)+1) + '%');
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);
    add(pondText);
  }
  @Override
  public void update() {
    AnimationTimer loop = new AnimationTimer() {
      @Override
      public void handle(long now) {

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
  Game game;
  @Override
  public void start(Stage primaryStage) {
    game = new Game();
    Scene scene = new Scene(game, WIDTH, HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("RainMaker");
    primaryStage.setResizable(false);

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.R)
          reset();
      }
    });

    primaryStage.show();
  }
  public void reset()
  {
    game.getChildren().clear();
    game.getChildren().add(new Pond());
    //game.getChildren().add(new Cloud());
    game.getChildren().add(new HeliPad());
    game.getChildren().add(new Helicopter());
  }
  public static void main(String[] args) {
        launch(args);
    }
}

