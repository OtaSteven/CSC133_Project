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
  PondAndCloud pondCloud;
  HeliPad heliPad;
  Helicopter helicopter;
  public Game()
  {
    pondCloud = new PondAndCloud();
    heliPad = new HeliPad();
    helicopter = new Helicopter();
    setScaleY(-1);
    setBackground(new Background(new BackgroundFill(Color.BLACK,
        new CornerRadii(0), Insets.EMPTY)));
    getChildren().add(pondCloud);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
  }
}
abstract class GameObject extends Group implements Updatable
{
  protected Translate myTranslation;
  protected Rotate myRotation;
  protected Scale myScale;
  public GameObject()
  {
    myTranslation = new Translate();
    myRotation = new Rotate();
    myScale = new Scale();
    this.getTransforms().addAll(myTranslation, myRotation, myScale);
  }
  public void translation(double tx, double ty)
  {
    myTranslation.setX(tx);
    myTranslation.setY(ty);
  }
  public void rotation(double degree)
  {
    myRotation.setAngle(degree);
    myRotation.setPivotX(0);
    myRotation.setPivotY(0);
  }
  public void scale(double sx, double sy)
  {
    myScale.setX(sx);
    myScale.setY(sy);
  }
  public double getMyRotation()
  {
    return myRotation.getAngle();
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
  public void setColor(Color col)
  {
    text.setFill(col);
  }
}
class Pond extends GameObject
{
  private Circle pond;
  private Random rand;
  private GameText pondText;
  public Pond()
  {
    super();
    rand = new Random();
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(25);
    translation(rand.nextInt((int)(GameApp.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(GameApp.GAME_HEIGHT)));
    add(pond);

    pondText = new GameText(String.valueOf(
        rand.nextInt(100)+1) + "%");
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    add(pondText);
  }
  @Override
  public void update() {
    translation(rand.nextInt((int)(GameApp.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(GameApp.GAME_HEIGHT)));
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);
    System.out.println("POND INTERSECT");
  }
}
class Cloud extends GameObject
{
  private Circle cloud;
  private Random rand;
  private GameText cloudText;

  public Cloud()
  {
    rand = new Random();
    cloud = new Circle(50, Color.WHITE);
    translation(rand.nextInt((int)(GameApp.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)(GameApp.GAME_HEIGHT/2)) +
            (int)GameApp.GAME_HEIGHT/2);
    add(cloud);

    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    cloudText.setColor(Color.BLACK);

    add(cloudText);
  }

  @Override
  public void update() {
    translation(rand.nextInt((int)(GameApp.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)(GameApp.GAME_HEIGHT/2)) +
            (int)GameApp.GAME_HEIGHT/2);
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    System.out.println("CLOUD INTERSECT");
  }
}
class PondAndCloud extends GameObject {

  Pond pond;
  Cloud cloud;
  public PondAndCloud()
  {
    pond = new Pond();
    cloud = new Cloud();
    update();
    add(pond);
    add(cloud);
  }
  @Override
  public void update() {
    //System.out.println("POND: " + pond.getBoundsInParent());
    //System.out.println("CLOUD: " + cloud.getBoundsInParent());
    if (pond.getBoundsInParent().intersects(cloud.getBoundsInParent()) ||
        cloud.getBoundsInParent().intersects(pond.getBoundsInParent()))
    {
      pond.update();
      cloud.update();
      update();
    }
    if (pond.myTranslation.getX()+pond.getBoundsInParent().getWidth() >=
        GameApp.GAME_WIDTH ||
        pond.myTranslation.getX()-pond.getBoundsInParent().getWidth()  <= 0 ||
        pond.myTranslation.getY()+pond.getBoundsInParent().getHeight() >=
            GameApp.GAME_HEIGHT ||
        pond.myTranslation.getY()+pond.getBoundsInParent().getHeight() <= 250)
    {
      System.out.println("POND COLLIDE WALL");
      pond.update();
      update();
    }
    if (cloud.myTranslation.getX()+cloud.getBoundsInParent().getWidth() >=
        GameApp.GAME_WIDTH ||
        cloud.myTranslation.getX()-cloud.getBoundsInParent().getWidth() <= 0 ||
        cloud.myTranslation.getY()+cloud.getBoundsInParent().getHeight() >=
            GameApp.GAME_HEIGHT ||
        cloud.myTranslation.getY()+cloud.getBoundsInParent().getHeight() <= 250)
    {
      System.out.println("CLOUD COLLIDE WALL");
      cloud.update();
      update();
    }
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


    padCircle = new Circle();
    padCircle.setRadius(30);
    padCircle.setStroke(Color.YELLOW);
    padCircle.setCenterX(heliPad.getX()+heliPad.getWidth()/2);
    padCircle.setCenterY(heliPad.getY()+heliPad.getHeight()/2);

    translation((GameApp.GAME_WIDTH/2)-(heliPad.getWidth()/2), 100);
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
    heli.setCenterX((GameApp.GAME_WIDTH/2));
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
public class GameApp extends Application {

  final static double GAME_WIDTH = 600;
  final static double GAME_HEIGHT = Screen.getPrimary().getBounds().
      getHeight()-100;
  Game game;
  @Override
  public void start(Stage primaryStage) {
    game = new Game();
    Scene scene = new Scene(game, GAME_WIDTH, GAME_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("RainMaker");
    primaryStage.setResizable(false);

    scene.setOnKeyPressed(e -> {
      switch (e.getCode())
      {
        case R: reset(); break;

        default: ;
      }
    });

    primaryStage.show();
  }
  public void reset()
  {
    game.getChildren().clear();
    game.getChildren().add(new PondAndCloud());
    game.getChildren().add(new HeliPad());
    game.getChildren().add(new Helicopter());
  }
  public static void main(String[] args) {
        launch(args);
    }
}

