import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.stage.Stage;

import java.util.Random;

interface Updatable
{
  void update();
}
class Game extends Pane
{
  final static double GAME_WIDTH = 400;
  final static double GAME_HEIGHT = 800;
  final static double HELI_SPAWN_AREA = 250;
  //Screen.getPrimary().getBounds().getHeight()-100;
  private BackgroundImg bgImg;
  private Pond pond;
  private Cloud cloud;
  private HeliPad heliPad;
  private Helicopter helicopter;
  private Text msg;
  private Alert alert;
  private boolean isHeliMoving = false;
  public Game()
  {
    setScaleY(-1);
    setBackground(new Background(new BackgroundFill(Color.BLACK,
        new CornerRadii(0), Insets.EMPTY)));

    init();
  }
  public void init()
  {
    getChildren().clear();
    msg = new Text();
    isHeliMoving = false;
    bgImg = new BackgroundImg();
    pond = new Pond();
    cloud = new Cloud();
    heliPad = new HeliPad();
    helicopter = new Helicopter(heliPad.myTranslation.getX() +
        heliPad.getBoundsInParent().getWidth()/2,
        heliPad.myTranslation.getY() +
            heliPad.getBoundsInParent().getHeight()/2);

    repositionPondCloud();
    getChildren().add(bgImg);
    getChildren().add(pond);
    getChildren().add(cloud);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
  }
  private void repositionPondCloud()
  {
    //System.out.println("POND: " + pond.getBoundsInParent());
    //System.out.println("CLOUD: " + cloud.getBoundsInParent());
    if (pond.getBoundsInParent().intersects(cloud.getBoundsInParent()) ||
        cloud.getBoundsInParent().intersects(pond.getBoundsInParent()))
    {
      pond.resetPond();
      cloud.resetCloud();
      this.repositionPondCloud();
    }
    if (pond.isPondCollidingWall())
    {
      System.out.println("POND COLLIDE WALL");
      pond.resetPond();
      this.repositionPondCloud();
    }
    if (cloud.isCloudCollidingWall())
    {
      System.out.println("CLOUD COLLIDE WALL");
      cloud.resetCloud();
      this.repositionPondCloud();
    }
  }
  private void winLossCondition(AnimationTimer GameTimer)
  {
    if (helicopter.isFuelEmpty()) {
      GameTimer.stop();
      msg.setText("You have lost! Play again?");
      alert = new Alert(Alert.AlertType.CONFIRMATION, msg.getText(),
          ButtonType.YES, ButtonType.NO);
      alert.setOnHidden(event -> {
        if (alert.getResult() == ButtonType.YES) {
          init();
        } else {
          //SOMEHOW EXIT STAGE
          Platform.exit();
        }
      });
      alert.show();
    }
    else if (pond.isPondFull() && isHeliMoving == false)
    {
      GameTimer.stop();
      msg.setText("You have won! Play again?");
      alert = new Alert(Alert.AlertType.CONFIRMATION, msg.getText(),
          ButtonType.YES, ButtonType.NO);
      alert.setOnHidden(event -> {
        if (alert.getResult() == ButtonType.YES) {
          init();
        } else {
          //SOMEHOW EXIT STAGE
          Platform.exit();
        }
      });
      alert.show();
    }
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
    if (!Shape.intersect(helicopter.getHeliBound(),
        cloud.getCloudBound()).getBoundsInParent().isEmpty())
    {
      cloud.increaseCloud();
    }
  }
  public void startHelicopter()
  {
    if (!Shape.intersect(helicopter.getHeliBound(),
        heliPad.getHeliPadBound()).getBoundsInParent().isEmpty())
    {
      if (isHeliMoving == false) {
        helicopter.startIgnitition();
        isHeliMoving = true;
      }
      else {
        helicopter.stopIgnition();
        isHeliMoving = false;
      }
    }
  }
  public void turnOnBoundary()
  {
    pond.showBoundingBox();
    cloud.showBoundingBox();
    heliPad.showBoundingBox();
    helicopter.showBoundingBox();
  }
  public void run()
  {
    AnimationTimer loop = new AnimationTimer() {
      private int iteration = 0;
      @Override
      public void handle(long now) {
        winLossCondition(this);
        helicopter.update();

        if (iteration++ % 10 == 0) {
          cloud.decreaseCloud();
          if (cloud.isCloudCapcityOver())
          {
            pond.fillingPond();
          }
        }
        cloud.update();
        pond.update();
        //System.out.println("HELICOPTER: " + helicopter.getBoundsInParent());
        //System.out.println("cloud: " +pondCloud.cloud.getBoundsInParent());
      }
    };
    loop.start();
  }
}
class BackgroundImg extends Pane
{
  private BackgroundImage bg;
  private Image img;
  private ImageView imgView;
  public BackgroundImg()
  {
    img = new Image("desertBackground.png");
    imgView = new ImageView(img);

    getChildren().addAll(imgView);
  }
}
abstract class GameObject extends Group implements Updatable
{
  protected Translate myTranslation;
  protected Rotate myRotation;
  protected Scale myScale;
  protected Rectangle bbox;
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
  public void createBoundingBox(double minX, double minY, double height,
                                double width)
  {
    bbox = new Rectangle(minX, minY, width, height);
    bbox.setStroke(Color.YELLOW);
    bbox.setFill(Color.TRANSPARENT);
    bbox.setVisible(!bbox.isVisible());
    add(bbox);
  }
  public void showBoundingBox()
  {
    bbox.setVisible(!bbox.isVisible());
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
  private double expansionIncrement = 0.5;
  public Pond()
  {
    rand = new Random();
    pondCapacity = rand.nextInt(50)+1;
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(25);
    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)Game.GAME_HEIGHT));

    makePondBound();

    add(pond);

    pondText = new GameText(pondCapacity + "%");
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    add(pondText);
  }
  private void makePondBound()
  {
    createBoundingBox(pond.getBoundsInParent().getMinX(),
        pond.getBoundsInParent().getMinY(), pond.getBoundsInParent().getWidth(),
        pond.getBoundsInParent().getHeight());
  }
  /*
  public void showPondBound()
  {
    bbox.setVisible(!bbox.isVisible());
  }

   */
  public boolean isPondCollidingWall()
  {
    if (myTranslation.getX()+(pond.getRadius()*2) >= Game.GAME_WIDTH ||
        myTranslation.getX()-(pond.getRadius()*2)  <= 0 ||
        myTranslation.getY()+(pond.getRadius()*2) >= Game.GAME_HEIGHT ||
        myTranslation.getY()+(pond.getRadius()*2) <= Game.HELI_SPAWN_AREA)
    {
      return true;
    }
    else
      return false;
  }
  public void fillingPond()
  {
    if (pondCapacity < 100) {
      pondCapacity++;
      expandPond();
    }
  }
  public boolean isPondFull()
  {
    if (pondCapacity >= 100)
      return true;
    else
      return false;
  }
  public void expandPond()
  {
    pond.setRadius(pond.getRadius() + expansionIncrement);
  }
  public void resetPond()
  {
    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT)));
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);
    System.out.println("POND INTERSECT");
  }
  @Override
  public void update() {
    pondText.setText(pondCapacity + "%");
  }
}
class Cloud extends GameObject
{
  private Circle cloud;
  private Random rand;
  private int cloudCapacity;
  private GameText cloudText;
  private int saturationColor = 255;

  public Cloud()
  {
    rand = new Random();
    cloudCapacity = 0;
    cloud = new Circle(50, Color.rgb(saturationColor, saturationColor,
        saturationColor));
    translation(rand.nextInt((int)(Game.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)Game.GAME_HEIGHT));

    makeCloudBound();

    add(cloud);

    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    cloudText.setColor(Color.BLUE);

    add(cloudText);
  }
  private void makeCloudBound()
  {
    createBoundingBox(cloud.getBoundsInParent().getMinX(),
        cloud.getBoundsInParent().getMinY(),
        cloud.getBoundsInParent().getWidth(),
        cloud.getBoundsInParent().getHeight());
  }
  public Rectangle getCloudBound()
  {
    return bbox;
  }
  public boolean isCloudCollidingWall()
  {
    if (myTranslation.getX()+(cloud.getRadius()*2) >= Game.GAME_WIDTH ||
        myTranslation.getX()-(cloud.getRadius()*2) <= 0 ||
        myTranslation.getY()+(cloud.getRadius()*2) >= Game.GAME_HEIGHT ||
        myTranslation.getY()+(cloud.getRadius()*2) <= Game.HELI_SPAWN_AREA)
    {
      return true;
    }
    else
      return false;
  }
  public void resetCloud()
  {
    translation(rand.nextInt((int)(Game.GAME_WIDTH+cloud.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT/2)) +
            (int)Game.GAME_HEIGHT/2);
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    System.out.println("CLOUD INTERSECT");
  }
  public void increaseCloud()
  {
    if (cloudCapacity < 100) {
      {
        cloudCapacity++;
        if (saturationColor > 155)
          cloud.setFill(Color.rgb(--saturationColor, --saturationColor,
              --saturationColor));
      }
    }
    System.out.println(saturationColor);
  }
  public void decreaseCloud()
  {
    if (cloudCapacity > 0) {
      cloudCapacity -= 1;
      if (saturationColor < 255)
        cloud.setFill(Color.rgb(++saturationColor,++saturationColor,
            ++saturationColor));
    }
  }
  public boolean isCloudCapcityOver()
  {
    if (cloudCapacity >= 30)
      return true;
    else
      return false;
  }
  @Override
  public void update() {
    cloudText.setText(cloudCapacity + "%");
    //System.out.println(cloudCapacity);
  }
}
class HeliPad extends GameObject
{
  private Rectangle heliPad;
  private Circle padCircle;
  private int heliPositionY = 25;
  public HeliPad()
  {
    heliPad = new Rectangle(75,75);
    heliPad.setStroke(Color.GRAY);

    padCircle = new Circle();
    padCircle.setRadius(30);
    padCircle.setStroke(Color.YELLOW);
    padCircle.setCenterX(heliPad.getX()+heliPad.getWidth()/2);
    padCircle.setCenterY(heliPad.getY()+heliPad.getHeight()/2);
    translation((Game.GAME_WIDTH/2)-(heliPad.getWidth()/2), heliPositionY);

    makeHeliPadBound();

    add(heliPad);
    add(padCircle);
  }
  private void makeHeliPadBound()
  {
    createBoundingBox(heliPad.getX(), heliPad.getY(), heliPad.getWidth(),
        heliPad.getHeight());
    bbox.setStrokeWidth(3);
  }
  public Rectangle getHeliPadBound()
  {
    return bbox;
  }
  @Override
  public void update() {
    //DO NOTHING
  }
}
class Helicopter extends GameObject
{
  private Circle heli;
  private Line heliHead;
  private GameText fuelText;
  private double heliSpeed, heliHeading;
  private double fuel;
  private static double maxHeliSpeed = 10, minHeliSpeed = -2;
  private boolean ignition = false;
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


    fuelText = new GameText("F:" + (int)fuel);
    fuelText.setTranslateX(heli.getCenterX()-30);
    fuelText.setTranslateY(heli.getCenterY()-15);
    fuelText.setColor(Color.YELLOW);

    makeHeliBound();

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
    createBoundingBox(fuelText.getBoundsInParent().getMinX(),
        fuelText.getBoundsInParent().getMinY(),
        fuelText.getBoundsInParent().getWidth(),
        (heliHead.getBoundsInParent().getHeight() +
        heli.getRadius() + fuelText.getBoundsInParent().getHeight()));
  }
  public Rectangle getHeliBound()
  {
    return bbox;
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
    heliHeading -= 15;
    if (getMyRotation() >= 345)
    {
      heliHeading = 0;
    }
    //System.out.println("ROTATION: " + getMyRotation());
    //System.out.println("HELIROT: " + heliRot);
  }
  public void rotateRight()
  {
    heliHeading += 15;
    if (getMyRotation() <= -345)
    {
      heliHeading = 0;
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
  public void startIgnitition()
  {
    if (ignition == false) {
      /*int timer = 3;
      while (timer != 0) {
        try {
          System.out.println("Helicopter starting in " + timer + "...");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          System.err.format("IOException: %s%n", e);
        }
        timer -= 1;
      }
      System.out.println("Helicopter starting in " + timer + "...");*/
      System.out.println("Ready to fly!");
      ignition = true;
    }
  }
  public void stopIgnition()
  {
    if (ignition == true && (Math.floor(heliSpeed) <= 0.1 &&
        Math.floor(heliSpeed) >= -0.1))
    {
      ignition = false;
      heliSpeed = 0;
    }
  }
  public boolean isFuelEmpty()
  {
    if (Math.floor(fuel) == 0)
      return true;
    else
      return false;
  }
  private void moveHelicopter()
  {
    if (ignition) {
      fuelText.setText("F:" + (int)fuel);
      rotation(-heliHeading);
      if (heliSpeed >= maxHeliSpeed)
        heliSpeed = 10;
      if (heliSpeed <= minHeliSpeed)
        heliSpeed = -2;
      myTranslation.setX(myTranslation.getX() + getVx());
      myTranslation.setY(myTranslation.getY() + getVy());
      if (fuel >= 0) {
        fuel = fuel - ( 1 + (heliSpeed/maxHeliSpeed) +
            Math.abs(heliSpeed/minHeliSpeed));
      }
      else
      {
        fuel = 0;
      }
    }
    else
    {
      heliSpeed = 0;
      heliHeading = 0;
    }
  }
  @Override
  public void update() {
    moveHelicopter();
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
        case SPACE: game.fillCloud(); break;
        case     I: game.startHelicopter(); break;
        case     B: game.turnOnBoundary(); break;
        case     R: game.init(); break;

        default: ;
      }
    });
    game.run();
    primaryStage.show();
  }

  public static void main(String[] args) {
        launch(args);
    }
}

