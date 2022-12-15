import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

interface Updatable
{
  void update();
}
class Game extends Pane
{
  final static int MAX_ROTATE_BLADE_SPEED = 7;
  final static double WIND_SPEED = 1;
  final static double GAME_WIDTH = 800;
  final static double GAME_HEIGHT = 800;
  final static double HELI_SPAWN_AREA = 250;
  private final Random rand = new Random();
  private HeliPad heliPad;
  private Helicopter helicopter;
  private Text msg;
  private Alert alert;
  private CloudMaker cloudMaker;
  private PondMaker pondMaker;
  public Game()
  {
    setScaleY(-1);
    init();
  }
  public void init()
  {
    getChildren().clear();
    msg = new Text();
    BackgroundImg bgImg = new BackgroundImg();
    pondMaker = new PondMaker();
    cloudMaker = new CloudMaker(rand.nextInt(5) + 1);
    heliPad = new HeliPad();
    helicopter = new Helicopter(heliPad.myTranslation.getX(),
        heliPad.myTranslation.getY());

    getChildren().add(bgImg);
    getChildren().add(pondMaker);
    getChildren().addAll(cloudMaker);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
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
          run();
        } else {
          Platform.exit();
        }
      });
      alert.show();
    }
    if (helicopter.isHelicopterTurnOn()) {
      if (averagePondCap() >= 80) {
        GameTimer.stop();
        msg.setText("You have won! Play again?");
        alert = new Alert(Alert.AlertType.CONFIRMATION, msg.getText(),
            ButtonType.YES, ButtonType.NO);
        alert.setOnHidden(event -> {
          if (alert.getResult() == ButtonType.YES) {
            init();
            run();
          } else {
            Platform.exit();
          }
        });
        alert.show();
      }
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
    for (int i = 0; i < cloudMaker.getListOfCloud().size(); i++) {
      if (!Shape.intersect(helicopter.getHeliBound(),
          ((Cloud)cloudMaker.getListOfCloud().get(i)).getCloudBound()).
          getBoundsInParent().isEmpty())
      {
        cloudMaker.increaseOnCloud(i);
      }
    }
  }
  public double averagePondCap()
  {
    System.out.println(pondMaker.getTotalPondCap()/pondMaker.getListOfPond().size());
      return pondMaker.getTotalPondCap()/pondMaker.getListOfPond().size();
  }
  public void startHelicopter()
  {
    if (!Shape.intersect(helicopter.getHeliBound(),
        heliPad.getHeliPadBound()).getBoundsInParent().isEmpty())
    {
      helicopter.toggleIgnition();
    }
  }
  public void turnOnBoundary()
  {
    for (int i = 0; i < cloudMaker.getListOfCloud().size(); i++)
      ((Cloud)cloudMaker.getListOfCloud().get(i)).showBoundingBox();

    for (int i = 0; i < pondMaker.getListOfPond().size(); i++)
      ((Pond)pondMaker.getListOfPond().get(i)).showBoundingBox();

    heliPad.showBoundingBox();
    helicopter.showBoundingBox();
  }
  public void loseCloudSaturation()
  {
    for (int i = 0; i < cloudMaker.getListOfCloud().size(); i++)
      cloudMaker.decreaseOnCloud(i);
  }
  public void run()
  {
    AnimationTimer loop = new AnimationTimer() {
      private int iteration = 0;
      @Override
      public void handle(long now) {
        winLossCondition(this);
        helicopter.update();

        if (iteration++ % 30 == 0) {
          loseCloudSaturation();
          for (int i = 0; i < cloudMaker.getListOfCloud().size(); i++)
          {
            if (((Cloud)cloudMaker.getListOfCloud().get(i)).
                isCloudCapcityOver())
            {
              for (int j = 0; j < pondMaker.getListOfPond().size(); j++) {
                pondMaker.fillPonds(j, cloudMaker.getPositionOfCloud(i));
              }
            }
          }
        }
        cloudMaker.update();
        pondMaker.update();
      }
    };
    loop.start();
  }
}
class BackgroundImg extends Pane
{
  public BackgroundImg()
  {
    Image img = new Image("desertBackground.png");
    ImageView imgView = new ImageView(img);

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
  public void createBoundingBox(double minX, double minY, double width,
                                double height)
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
  private final Circle pond;
  private final Random rand;
  private final GameText pondText;
  private int pondCapacity;
  private double expansionIncrement = 0.1;
  public Pond()
  {
    rand = new Random();
    pondCapacity = rand.nextInt(17)+13;
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(pondCapacity*.5 + pondCapacity);
    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)Game.GAME_HEIGHT));

    pondText = new GameText(pondCapacity + "%");
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    pondCollidingWall();
    makePondBound();
    add(pond);
    add(pondText);
  }
  private void makePondBound()
  {
    createBoundingBox(pond.getBoundsInParent().getMinX(),
        pond.getBoundsInParent().getMinY(),
        pond.getBoundsInParent().getWidth(),
        pond.getBoundsInParent().getHeight());
  }
  public Rectangle getPondBound()
  {
    return bbox;
  }
  public void pondCollidingWall()
  {
    if (myTranslation.getX()+(pond.getRadius()*2) >= Game.GAME_WIDTH ||
        myTranslation.getX()-(pond.getRadius()*2)  <= 0 ||
        myTranslation.getY()+(pond.getRadius()*2) >= Game.GAME_HEIGHT ||
        myTranslation.getY()+(pond.getRadius()*2) <= Game.HELI_SPAWN_AREA)
    {
      resetPond();
      pondCollidingWall();
    }
  }
  public void fillingPond(Point2D cloudPos)
  {
    double maxDistance = 4 * (pond.getRadius()*2);

    double a = Math.abs(myTranslation.getX() - cloudPos.getX());
    double b = Math.abs(myTranslation.getY() - cloudPos.getY());

    double currDistance = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

    if (currDistance < maxDistance)
    {
      if (pondCapacity < 100) {
        pondCapacity++;
        System.out.println("Pond CAP: " + pondCapacity);
        expandPond();
        expansionIncrement += 0.01;
        System.out.println("EXAPANSION INCREMENT: " + expansionIncrement);
      }
    }
  }
  public boolean isPondFull()
  {
    return pondCapacity >= 100;
  }
  public int pondCap()
  {
    return pondCapacity;
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
  }
  @Override
  public void update() {
    pondText.setText(pondCapacity + "%");
  }
}
class PondMaker extends GameObject
{
  private final LinkedList<Pond> pondList = new LinkedList<>();
  public PondMaker()
  {
    for (int i = 0; i < 3; i++) {
      pondList.add(new Pond());
    }
    checkPondIntersect();
    getChildren().addAll(pondList);
  }
  public boolean isListOfPondFull(int n)
  {
    return pondList.get(n).isPondFull();
  }
  public LinkedList getListOfPond()
  {
    return pondList;
  }
  private void checkPondIntersect()
  {
    for (int i = 0; i < pondList.size(); i++)
    {
      for (int j = 0; j < pondList.size(); j++)
      {
        if (i != j && !Shape.intersect(pondList.get(i).getPondBound(),
            pondList.get(j).getPondBound()).getBoundsInParent().isEmpty())
        {
          pondList.get(j).resetPond();
          System.out.println("REPOSITION POND: " + j);
        }
      }
    }
  }
  public int getTotalPondCap()
  {
    int totalPondCap = 0;
    for (int i = 0; i < pondList.size(); i++)
    {
      totalPondCap += pondList.get(i).pondCap();
    }
    return totalPondCap;
  }
  public void fillPonds(int n, Point2D cloudPos)
  {
    pondList.get(n).fillingPond(cloudPos);
  }
  public void update()
  {
    for (Pond p : pondList)
    {
      p.update();
    }
  }
}
interface CloudManager
{
  void moveCloud();
}
class CloudAlive implements CloudManager
{
  private final Cloud currCloud;
  public CloudAlive(Cloud cloud)
  {
    this.currCloud = cloud;
  }
  @Override
  public void moveCloud() {
    currCloud.moveCloud();
    if (currCloud.myTranslation.getX() >= 0)
    {
      currCloud.changeState(new CloudInView(currCloud));
    }
  }
}
class CloudInView implements CloudManager
{
  private final Cloud currCloud;
  public CloudInView(Cloud cloud)
  {
    this.currCloud = cloud;
  }
  @Override
  public void moveCloud() {
    currCloud.moveCloud();
    if (currCloud.myTranslation.getX() >= Game.GAME_WIDTH +
        currCloud.getBoundsInParent().getWidth())
    {
      currCloud.changeState(new CloudDead(currCloud));
    }
  }
}
class CloudDead implements CloudManager
{
  private Cloud currCloud;
  public CloudDead(Cloud cloud)
  {
    // Cloud will be removed from the cloudList in this state
    currCloud = cloud;
  }
  @Override
  public void moveCloud() {
    currCloud = null;
  }
}
class Cloud extends GameObject
{
  private Circle cloud;
  private final Random rand = new Random();
  private int cloudCapacity;
  private GameText cloudText;
  private int saturationColor = 255;
  private CloudManager cloudState;
  private int heightOffset = 200;

  public Cloud()
  {
    cloudCapacity = 0;

    createCloud();

    translation(-rand.nextInt((int)cloud.getRadius()*2)-10,
        rand.nextInt((int)Game.GAME_HEIGHT) + heightOffset);

    makeCloudBound();

    cloudState = new CloudAlive(this);
  }
  private void createCloud()
  {
    cloud = new Circle(50, Color.rgb(saturationColor, saturationColor,
        saturationColor));
    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    cloudText.setColor(Color.BLUE);
    add(cloud);
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
  public void resetCloud()
  {
    translation(-rand.nextInt((int)cloud.getRadius()*2)-10,
        rand.nextInt((int)(Game.GAME_HEIGHT/2)) +
            Game.GAME_HEIGHT/2);
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
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
    return cloudCapacity >= 30;
  }
  protected void moveCloud()
  {
    myTranslation.setX(myTranslation.getX() + Game.WIND_SPEED);
  }
  public void changeState(CloudManager state)
  {
    this.cloudState = state;
  }
  public boolean isCloudDead()
  {
    return cloudState instanceof CloudDead;
  }
  @Override
  public void update() {
    cloudText.setText(cloudCapacity + "%");
    cloudState.moveCloud();
  }
}
class CloudMaker extends GameObject
{
  private final LinkedList<Cloud> cloudList = new LinkedList<>();
  Random rand = new Random();
  public CloudMaker(int n)
  {
    for (int i = 0; i < n; i++) {
      cloudList.add(new Cloud());
    }
    getChildren().addAll(cloudList);
  }
  public void increaseOnCloud(int i)
  {
    cloudList.get(i).increaseCloud();
  }
  public void decreaseOnCloud(int i)
  {
    cloudList.get(i).decreaseCloud();
  }
  public LinkedList getListOfCloud()
  {
    return cloudList;
  }
  public Point2D getPositionOfCloud(int i)
  {
    return new Point2D(cloudList.get(i).myTranslation.getX(),
        cloudList.get(i).myTranslation.getY());
  }
  public Rectangle getBoundingBox(int n)
  {
    return cloudList.get(n).getCloudBound();
  }
  public void update()
  {
    for (int i = 0; i < cloudList.size(); i++)
    {
      cloudList.get(i).update();
      if (cloudList.get(i).isCloudDead())
      {
        cloudList.remove(i);
        if (cloudList.size() <= 2) {
          for (int j = 0; j < rand.nextInt(5) + 1; j++)
          {
            cloudList.add(new Cloud());
            getChildren().add(cloudList.get(cloudList.size()-1));
          }
        }
        else {
          if (rand.nextInt(2) == 1)
          {
            cloudList.add(new Cloud());
            getChildren().add(cloudList.get(cloudList.size()-1));
          }
        }
      }
    }
  }
}
class HeliPad extends GameObject
{
  private final Rectangle heliPad;
  private final Circle padCircle;
  private final int heliPositionY = 100;
  private final int outlineWidth = 3;
  public HeliPad()
  {
    heliPad = new Rectangle(100,100);
    heliPad.setStroke(Color.GRAY);
    heliPad.setStrokeWidth(outlineWidth);
    heliPad.setFill(Color.TRANSPARENT);

    padCircle = new Circle();
    padCircle.setRadius(40);
    padCircle.setStroke(Color.YELLOW);
    heliPad.setStrokeWidth(outlineWidth);
    padCircle.setFill(Color.TRANSPARENT);

    heliPad.setX(padCircle.getCenterX()-heliPad.getWidth()/2);
    heliPad.setY(padCircle.getCenterY()-heliPad.getHeight()/2);

    translation((Game.GAME_WIDTH/2), heliPositionY);

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
interface HeliState
{
  void toggleIgnition();
  int spinBlade(int spinSpeed);
}
class HeliStateOff implements HeliState
{
  Helicopter heli;
  public HeliStateOff(Helicopter heli)
  {
    this.heli = heli;
  }
  @Override
  public void toggleIgnition() {
    System.out.println("HELI CHANGE OFF TO STARTING");
    heli.changeState(new HeliStateStarting(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    spinSpeed = 0;
    return spinSpeed;
  }
}
class HeliStateStarting implements HeliState
{
  Helicopter heli;
  public HeliStateStarting(Helicopter heli)
  {
    this.heli = heli;
  }
  @Override
  public void toggleIgnition() {
    System.out.println("HELI Starting TO STOPPING");
    heli.changeState(new HeliStateStopping(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    spinSpeed += 1;
    if (spinSpeed >= 5) {
      System.out.println("HELI Starting TO READY");
      heli.changeState(new HeliStateReady(heli));
    }
    return spinSpeed;
  }
}
class HeliStateReady implements HeliState
{
  Helicopter heli;
  public HeliStateReady(Helicopter heli)
  {
    this.heli = heli;
  }
  @Override
  public void toggleIgnition() {
    System.out.println("HELI Ready TO STOPPING");
    heli.changeState(new HeliStateStopping(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    if (spinSpeed < Game.MAX_ROTATE_BLADE_SPEED)
      spinSpeed++;
    return spinSpeed;
  }
}
class HeliStateStopping implements HeliState
{
  Helicopter heli;
  public HeliStateStopping(Helicopter heli)
  {
    this.heli = heli;
  }
  @Override
  public void toggleIgnition() {
    System.out.println("HELI Stopping TO STARTING");
    heli.changeState(new HeliStateStarting(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    spinSpeed -= 1;
    if (spinSpeed == 0) {
      System.out.println("HELI STOPPING TO OFF");
      heli.changeState(new HeliStateOff(heli));
    }
    return spinSpeed;
  }
}
class Helicopter extends GameObject implements HeliState
{
  private final GameText fuelText;
  private HeliState state;
  private final HeloBody heliBody;
  private final HeloBlade heliBlade;
  private int delayRot = 50, bladeRot;
  private final static int offset_Text_Y = -55;
  private final static int offset_Text_X = -25;
  private double heliSpeed, heliHeading, fuel;
  private final static double maxHeliSpeed = 10, minHeliSpeed = -2;
  public Helicopter(double centerX, double centerY)
  {
    fuel = 25000;
    heliSpeed = 0;
    heliHeading = 0;
    bladeRot = 0;

    heliBody = new HeloBody();
    heliBlade = new HeloBlade();

    fuelText = new GameText("F:" + (int)fuel);
    fuelText.setColor(Color.YELLOW);

    fuelText.setTranslateX(offset_Text_X);
    fuelText.setTranslateY(offset_Text_Y);

    translation(centerX, centerY);

    makeHeliBound();

    add(heliBody);
    add(heliBlade);
    add(fuelText);
    this.state = new HeliStateOff(this);
  }
  private void makeHeliBound()
  {
    createBoundingBox(heliBlade.getBoundsInParent().getMinX(),
        heliBlade.getBoundsInParent().getMinY(),
        heliBlade.getBoundsInParent().getWidth(),
        heliBlade.getBoundsInParent().getHeight());
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
    if (state instanceof HeliStateReady) {
      heliHeading -= 15;
      if (getMyRotation() >= 345) {
        heliHeading = 0;
      }
    }
  }
  public void rotateRight()
  {
    if (state instanceof HeliStateReady) {
      heliHeading += 15;
      if (getMyRotation() <= -345) {
        heliHeading = 0;
      }
    }
  }
  public void increaseSpeed()
  {
    if (state instanceof HeliStateReady)
      heliSpeed += 0.1;
  }
  public void decreaseSpeed()
  {
    if (state instanceof HeliStateReady)
      heliSpeed -= 0.1;
  }
  public boolean isFuelEmpty()
  {
    return Math.floor(fuel) == 0;
  }
  private void moveHelicopter()
  {
    if (state instanceof HeliStateReady) {
      if (heliSpeed >= maxHeliSpeed)
        heliSpeed = 10;
      if (heliSpeed <= minHeliSpeed)
        heliSpeed = -2;

      translation(myTranslation.getX() + getVx(),
          myTranslation.getY() + getVy());

      rotation(-heliHeading);
    }
  }
  private void decreaseFuel()
  {
    fuelText.setText("F:" + (int) fuel);
    if (fuel >= 0) {
      fuel = fuel - (1 + (heliSpeed / maxHeliSpeed) +
          Math.abs(heliSpeed / minHeliSpeed));
    } else {
      fuel = 0;
    }
  }
  public void changeState(HeliState state)
  {
    this.state = state;
  }
  @Override
  public void toggleIgnition() {
    if (Math.floor(heliSpeed) <= 0.3 && Math.floor(heliSpeed) >= -0.3)
    {
      state.toggleIgnition();
    }
  }
  @Override
  public int spinBlade(int spinSpeed) {
    return state.spinBlade(spinSpeed);
  }
  public boolean isHelicopterTurnOn() {
    return state instanceof HeliStateOff;
  }
  @Override
  public void update() {
    moveHelicopter();
    if (state instanceof HeliStateOff) {
      heliSpeed = 0;
    }
    if (!(state instanceof HeliStateOff))
      decreaseFuel();
    if (delayRot == 0) {
      bladeRot = spinBlade(bladeRot);
      delayRot = 50;
    } else if (delayRot > 0) {
      delayRot--;
    }
    heliBlade.bladeSpin(bladeRot);
  }
}
class HeloBody extends GameObject
{
  public HeloBody()
  {
    ImageView imgView = new ImageView( new Image("heliBody.png"));
    translation(0-imgView.getBoundsInParent().getWidth()/2,
        0-imgView.getBoundsInParent().getHeight()/2);
    add(imgView);
  }
}

class HeloBlade extends GameObject
{
  private int bladeRot;
  public HeloBlade()
  {
    bladeRot = 0;
    Line blade = new Line(-50, -50,50,50);
    blade.setStrokeWidth(5);
    blade.setStroke(Color.GRAY);

    add(blade);
    loop.start();
  }
  public void bladeSpin(int bladeRot)
  {
    this.bladeRot = bladeRot;
  }
  AnimationTimer loop = new AnimationTimer() {
    @Override
    public void handle(long now) {
      rotation(getMyRotation()-bladeRot);
      getTransforms().clear();
      getTransforms().addAll(myRotation, myTranslation);
    }
  };
}
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

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
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
      }
    });
    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        keysDown.remove(event.getCode());
      }
    });
    game.run();
    primaryStage.show();
  }
  public static void main(String[] args) {
        launch(args);
    }
}