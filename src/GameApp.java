import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
  final static double GAME_WIDTH = 600;
  final static double GAME_HEIGHT = Screen.getPrimary().getBounds().
      getHeight()-100;
  private PondAndCloud pondCloud;
  private HeliPad heliPad;
  private Helicopter helicopter;
  public Game()
  {

    pondCloud = new PondAndCloud();
    heliPad = new HeliPad();
    helicopter = new Helicopter(heliPad.myTranslation.getX() +
        heliPad.getBoundsInParent().getWidth()/2,
        heliPad.myTranslation.getY() +
            heliPad.getBoundsInParent().getHeight()/2);
    setScaleY(-1);
    setBackground(new Background(new BackgroundFill(Color.BLACK,
        new CornerRadii(0), Insets.EMPTY)));
    getChildren().add(pondCloud);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
  }
  public void heliLeft()
  {
    helicopter.rotateLeft();
  }
  public void heliRight()
  {
    helicopter.rotateRight();
  }
  public void heliAccelerate()
  {
    helicopter.increaseSpeed();
  }
  public void heliDecelerate()
  {
    helicopter.decreaseSpeed();
  }
  public void fillCloud()
  {

  }
  public void turnOnBoundary()
  {
    pondCloud.turnOnPondCloudBoundary();
    helicopter.showHeliBound();
  }
  public void run()
  {
    AnimationTimer loop = new AnimationTimer() {
      @Override
      public void handle(long now) {
        helicopter.update();
        if (Shape.intersect(helicopter.getHeliBound(), pondCloud.cloud.getCloudBound()).getBoundsInParent().getWidth() > 0)
          System.out.println("THEY INTERSECTTTT");

        //System.out.println("HELICOPTER: " + helicopter.getBoundsInParent());
        //System.out.println("cloud: " +pondCloud.cloud.getBoundsInParent());
      }
    };
    loop.start();
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
  private Rectangle pondBound;
  private int pondCapacity;
  public Pond()
  {
    rand = new Random();
    pondCapacity = 0;
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(25);
    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT)));

    pondBound = new Rectangle(pond.getBoundsInParent().getMinX(),
        pond.getBoundsInParent().getMinY(), pond.getBoundsInParent().getWidth(),
        pond.getBoundsInParent().getHeight());
    pondBound.setStroke(Color.YELLOW);
    add(pondBound);
    pondBound.setVisible(!pondBound.isVisible());

    add(pond);

    pondCapacity = rand.nextInt(50)+1;

    pondText = new GameText(String.valueOf(
        pondCapacity) + "%");
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    add(pondText);
  }
  public void showPondBound()
  {
    pondBound.setVisible(!pondBound.isVisible());
  }
  public void fillingPond()
  {
    pondText.setText(String.valueOf(pondCapacity+1));
  }
  @Override
  public void update() {
    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT)));
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);
    System.out.println("POND INTERSECT");
  }
}
class Cloud extends GameObject
{
  private Circle cloud;
  private Random rand;
  private int cloudCapacity;
  private GameText cloudText;
  private Rectangle cloudBound;

  public Cloud()
  {
    rand = new Random();
    cloudCapacity = 0;
    cloud = new Circle(50, Color.WHITE);
    translation(rand.nextInt((int)(Game.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT/2)) +
            (int)Game.GAME_HEIGHT/2);

    cloudBound = new Rectangle(cloud.getBoundsInParent().getMinX(),
        cloud.getBoundsInParent().getMinY(), cloud.getBoundsInParent().getWidth(),
        cloud.getBoundsInParent().getHeight());
    cloudBound.setStroke(Color.YELLOW);
    add(cloudBound);
    cloudBound.setVisible(!cloudBound.isVisible());

    add(cloud);

    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    cloudText.setColor(Color.BLACK);

    add(cloudText);
  }
  public void showCloudBound()
  {
    cloudBound.setVisible(!cloudBound.isVisible());
  }
  public Rectangle getCloudBound()
  {
    return cloudBound;
  }
  public void fillCloud()
  {
    cloudText.setText(String.valueOf(cloudCapacity+1) + "%");
  }
  @Override
  public void update() {
    translation(rand.nextInt((int)(Game.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT/2)) +
            (int)Game.GAME_HEIGHT/2);
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
  public void turnOnPondCloudBoundary()
  {
    pond.showPondBound();
    cloud.showCloudBound();
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
      this.update();
    }
    if (pond.myTranslation.getX()+pond.getBoundsInParent().getWidth() >=
        Game.GAME_WIDTH ||
        pond.myTranslation.getX()-pond.getBoundsInParent().getWidth()  <= 0 ||
        pond.myTranslation.getY()+pond.getBoundsInParent().getHeight() >=
            Game.GAME_HEIGHT ||
        pond.myTranslation.getY()+pond.getBoundsInParent().getHeight() <= 250)
    {
      System.out.println("POND COLLIDE WALL");
      pond.update();
      this.update();
    }
    if (cloud.myTranslation.getX()+cloud.getBoundsInParent().getWidth() >=
        Game.GAME_WIDTH ||
        cloud.myTranslation.getX()-cloud.getBoundsInParent().getWidth() <= 0 ||
        cloud.myTranslation.getY()+cloud.getBoundsInParent().getHeight() >=
            Game.GAME_HEIGHT ||
        cloud.myTranslation.getY()+cloud.getBoundsInParent().getHeight() <= 250)
    {
      System.out.println("CLOUD COLLIDE WALL");
      cloud.update();
      this.update();
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

    translation((Game.GAME_WIDTH/2)-(heliPad.getWidth()/2), 100);
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
  GameText fuelText;
  private double heliSpeed, heliRot;
  private int fuel;
  private Rectangle heliBound;
  private static double maxSpeed = 10, minSpeed = -2;
  public Helicopter()
  {
    //NOTHING IS CREATED
  }
  public Helicopter(double centerX, double centerY)
  {
    fuel = 25000;
    heliSpeed = 0;
    heli = new Circle(10);
    heli.setFill(Color.YELLOW);
    translation(centerX, centerY);
    makeHeliHead();


    fuelText = new GameText("F:" + fuel);
    fuelText.setTranslateX(heli.getCenterX()-30);
    fuelText.setTranslateY(heli.getCenterY()-15);
    fuelText.setColor(Color.YELLOW);

    makeHeliBound();

    heliBound.setVisible(!heliBound.isVisible());

    add(heli);
    add(heliHead);
    add(fuelText);
  }
  private void makeHeliHead()
  {
    heliHead = new Line(heli.getCenterX(), heli.getCenterY(), heli.getCenterX(),
        heli.getCenterY()+25);
    heliHead.setStroke(Color.YELLOW);
  }
  private void makeHeliBound()
  {
    heliBound = new Rectangle(fuelText.getBoundsInParent().getMinX(),
        fuelText.getBoundsInParent().getMinY(), fuelText.getBoundsInParent().getWidth(),
        (heliHead.getBoundsInParent().getHeight() +
            heli.getRadius() + fuelText.getBoundsInParent().getHeight()));
    System.out.println(heliHead.getBoundsInParent());
    heliBound.setStroke(Color.YELLOW);
    add(heliBound);
  }
  @Override
  public void update() {
    rotation(-heliRot);
    if (heliSpeed >= maxSpeed)
      heliSpeed = 10;
    if (heliSpeed <= minSpeed)
      heliSpeed = -2;
    myTranslation.setX(myTranslation.getX() + getVx());
    myTranslation.setY(myTranslation.getY() + getVy());
  }
  public double getVx()
  {
    return -Math.sin(Math.toRadians(getMyRotation()))*heliSpeed;
  }
  public double getVy()
  {
    return Math.cos(Math.toRadians(getMyRotation()))*heliSpeed;
  }
  public void rotateLeft()
  {
    heliRot -= 15;
    if (getMyRotation() >= 345)
    {
      heliRot = 0;
    }
    //System.out.println("ROTATION: " + getMyRotation());
    //System.out.println("HELIROT: " + heliRot);
  }
  public void rotateRight()
  {
    heliRot += 15;
    if (getMyRotation() <= -345)
    {
      heliRot = 0;
    }
    //System.out.println("ROTATION: " + getMyRotation());
    //System.out.println("HELIROT: " + heliRot);
  }
  public void increaseSpeed()
  {
    heliSpeed += 0.1;
  }
  public void decreaseSpeed()
  {
    heliSpeed -= 0.1;
  }
  public void showHeliBound()
  {
    heliBound.setVisible(!heliBound.isVisible());
  }
  public Rectangle getHeliBound()
  {
    return heliBound;
  }
}
public class GameApp extends Application {
  Game game;
  @Override
  public void start(Stage primaryStage) {
    game = new Game();
    Scene scene = new Scene(game, Game.GAME_WIDTH, Game.GAME_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("RainMaker");
    primaryStage.setResizable(false);

    scene.setOnKeyPressed(e -> {
      switch (e.getCode())
      {
        case  LEFT: game.heliLeft(); break;
        case RIGHT: game.heliRight(); break;
        case    UP: game.heliAccelerate(); break;
        case  DOWN: game.heliDecelerate(); break;
        case SPACE:
        case     B: game.turnOnBoundary(); break;
        case     R: reset(); break;

        default: ;
      }
    });
    game.run();
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

