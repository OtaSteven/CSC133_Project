import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import java.util.*;

interface Updatable
{
  void update();
}
interface Observer
{
  void updateWind(Wind w);
}
interface Subject
{
  void attach(Observer o);
  void detach(Observer o);
  double windSpeed();
}
interface HeliState
{
  void toggleIgnition();
  int spinBlade(int spinSpeed);
}
class Game extends Pane
{
  final static int MAX_ROTATE_BLADE_SPEED = 15;
  final static double GAME_WIDTH = 800;
  final static double GAME_HEIGHT = 800;
  final static double HELI_SPAWN_AREA = 250;
  private Text msg;
  private Alert alert;
  private PondMaker pondMaker;
  private CloudMaker cloudMaker;
  private BlimpFactory blimp;
  private HeliPad heliPad;
  private Helicopter helicopter;
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
    cloudMaker = new CloudMaker();
    blimp = new BlimpFactory();
    heliPad = new HeliPad();
    helicopter = new Helicopter(heliPad.myTranslation.getX(),
        heliPad.myTranslation.getY());

    getChildren().add(bgImg);
    getChildren().add(pondMaker);
    getChildren().add(cloudMaker);
    getChildren().add(blimp);
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
    if (!helicopter.isHelicopterTurnOn()) {
      if (averagePondCap() >= 80) {
        GameTimer.stop();
        msg.setText("You have won! Your score is: " + (int)(helicopter.getFuel()
            * ((double)pondMaker.getTotalPondCap()/100)) + "\nPlay again?");
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
    if (helicopter.isHelicopterTurnOn()) {
      for (int i = 0; i < cloudMaker.getCloudListSize(); i++) {
        if (!Shape.intersect(helicopter.getHeliBound(),
                cloudMaker.getCloudBoundingBox(i)).getBoundsInParent().
            isEmpty()) {
          cloudMaker.increaseOnCloud(i);
        }
      }
    }
  }
  private int averagePondCap()
  {
      return pondMaker.getTotalPondCap()/pondMaker.getPondListSize();
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
    cloudMaker.showCloudsBoundingBox();
    pondMaker.showPondsBoundingBox();
    blimp.showBlimpBoundingBox();
    heliPad.showBoundingBox();
    helicopter.showBoundingBox();
  }
  private void loseCloudSaturation()
  {
    for (int i = 0; i < cloudMaker.getCloudListSize(); i++)
      cloudMaker.decreaseOnCloud(i);
  }
  public void run()
  {
    AnimationTimer loop = new AnimationTimer() {
      private int iteration = 0;
      private int fuelIteration = 30;
      @Override
      public void handle(long now) {
        winLossCondition(this);
        helicopter.update();
        blimp.update();

        if (iteration++ % 30 == 0)
        {
          loseCloudSaturation();
          for (int i = 0; i < cloudMaker.getCloudListSize(); i++)
          {
            if (cloudMaker.isCloudCapacityOver(i))
            {
              for (int j = 0; j < pondMaker.getPondListSize(); j++) {
                pondMaker.fillPonds(j, cloudMaker.getPositionOfCloud(i));
              }
            }
          }
        }
        if (!Shape.intersect(helicopter.getHeliBound(), blimp.getBlimpBound())
            .getBoundsInParent().isEmpty() && (helicopter.getHeliSpeed() >
            blimp.getBlimpSpeed() - 0.7 && helicopter.getHeliSpeed() <
            blimp.getBlimpSpeed() + 0.7))
        {
          if (fuelIteration == 0) {
            blimp.decreaseBlimpRefuel();
            helicopter.increaseFuel();
          }
          else
          {
            fuelIteration--;
          }
        }
        else
        {
          fuelIteration = 100;
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
  public void add(Node node)
  {
    this.getChildren().add(node);
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
  private Circle pond;
  private Random rand;
  private GameText pondText;
  private int pondCapacity;
  private final int heightOffset = 200;
  private double expansionIncrement = 0.1;
  public Pond()
  {
    createPond();
    makePondBound();
  }
  private void createPond()
  {
    rand = new Random();

    pondCapacity = rand.nextInt(17)+13;
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(pondCapacity*.5 + pondCapacity);

    pondText = new GameText(pondCapacity + "%");
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT - heightOffset)) + heightOffset);

    pondCollidingWall();

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
        expandPond();
        expansionIncrement += 0.01;
      }
    }
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
    pondText.setTranslateX(pond.getCenterX()-15);
    pondText.setTranslateY(pond.getCenterY()+10);

    translation(rand.nextInt((int)(Game.GAME_WIDTH+pond.getRadius())),
        rand.nextInt((int)(Game.GAME_HEIGHT - (pond.getRadius()*2))) +
            heightOffset);

    pondCollidingWall();
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
      Pond p = new Pond();
      pondList.add(p);
      add(p);
    }
    checkPondIntersect();
  }
  private void checkPondIntersect()
  {
    for (int i = 0; i < pondList.size(); i++)
    {
      for (int j = 0; j < pondList.size(); j++)
      {
        if (i != j && (!Shape.intersect(pondList.get(i).getPondBound(),
            pondList.get(j).getPondBound()).getBoundsInParent().isEmpty()))
        {
          pondList.get(j).resetPond();
          checkPondIntersect();
        }
      }
    }
  }
  public int getPondListSize()
  {
    return pondList.size();
  }
  public void showPondsBoundingBox()
  {
    for (Pond pond : pondList) {
      pond.showBoundingBox();
    }
  }
  public int getTotalPondCap()
  {
    int totalPondCap = 0;
    for (Pond pond : pondList) {
      totalPondCap += pond.pondCap();
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
class TransientGameObject extends GameObject
{
  protected TransientState objectState;
  public void changeState(TransientState state)
  {
    this.objectState = state;
  }
  public boolean isObjectDead()
  {
    return objectState instanceof Dead;
  }
  interface TransientState
  {
    void movingObject();
  }
  class Created implements TransientState
  {
    private final GameObject object;
    public Created(GameObject object)
    {
      this.object = object;
    }
    @Override
    public void movingObject() {
      if (object.myTranslation.getX() > 0) {
        changeState(new InView(object));
      }
    }
  }
  class InView implements TransientState
  {
    private final GameObject object;
    public InView(GameObject object)
    {
      this.object = object;
    }
    @Override
    public void movingObject() {
      if (object.myTranslation.getX() >= Game.GAME_WIDTH +
          object.getBoundsInLocal().getWidth()/2) {
        changeState(new Dead());
      }
    }
  }
  static class Dead implements TransientState
  {
    public Dead()
    {
      // Object is consider dead and will no longer be in use
    }
    @Override
    public void movingObject() {
      // Object is dead, will be recreated in their respective class after being
      // removed
    }
  }
}
class Wind implements Subject
{
  private final Random rand = new Random();
  private final LinkedList<Observer> observers = new LinkedList<>();
  @Override
  public void attach(Observer o) {
    observers.add(o);
  }
  @Override
  public void detach(Observer o) {
    observers.remove(o);
  }
  @Override
  public double windSpeed() {
    return rand.nextDouble(2)+1;
  }
}
class Cloud extends TransientGameObject implements Observer
{
  private Circle cloud;
  private int cloudCapacity;
  private GameText cloudText;
  private int saturationColor = 255;
  private double windSpeed = 0;
  public Cloud()
  {
    createCloud();
    makeCloudBound();
    objectState = new Created(this);
  }
  private void createCloud()
  {
    cloudCapacity = 0;
    Random rand = new Random();
    cloud = new Circle(50, Color.rgb(saturationColor, saturationColor,
        saturationColor));
    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX()-15);
    cloudText.setTranslateY(cloud.getCenterY()+10);
    cloudText.setColor(Color.BLUE);

    translation(-rand.nextInt((int)cloud.getRadius())-10,
        rand.nextInt((int)(Game.GAME_HEIGHT-cloud.getRadius())));

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
  public boolean isCloudCapacityOver()
  {
    return cloudCapacity >= 30;
  }
  protected void moveCloud()
  {
    myTranslation.setX(myTranslation.getX() + windSpeed);
    objectState.movingObject();
  }
  @Override
  public void update() {
    cloudText.setText(cloudCapacity + "%");
    moveCloud();
  }
  @Override
  public void updateWind(Wind w)
  {
    windSpeed = w.windSpeed();
  }
}
class CloudMaker extends GameObject
{
  private final LinkedList<Cloud> cloudList = new LinkedList<>();
  private final Random rand = new Random();
  private final Wind wind = new Wind();
  public CloudMaker()
  {
    for (int i = 0; i < rand.nextInt(3)+2; i++) {
      Cloud newCloud = new Cloud();
      cloudList.add(newCloud);
      wind.attach(newCloud);
      newCloud.updateWind(wind);
      add(newCloud);
    }
  }
  public int getCloudListSize()
  {
    return cloudList.size();
  }
  public void showCloudsBoundingBox()
  {
    for (Cloud c : cloudList)
    {
      c.showBoundingBox();
    }
  }
  public boolean isCloudCapacityOver(int n)
  {
    return cloudList.get(n).isCloudCapacityOver();
  }
  public Rectangle getCloudBoundingBox(int n)
  {
    return cloudList.get(n).getCloudBound();
  }
  public void increaseOnCloud(int i)
  {
    cloudList.get(i).increaseCloud();
  }
  public void decreaseOnCloud(int i)
  {
    cloudList.get(i).decreaseCloud();
  }
  public Point2D getPositionOfCloud(int i)
  {
    return new Point2D(cloudList.get(i).myTranslation.getX(),
        cloudList.get(i).myTranslation.getY());
  }
  public void update()
  {
    for (int i = 0; i < cloudList.size(); i++)
    {
      cloudList.get(i).update();
      if (cloudList.get(i).isObjectDead())
      {
        if (cloudList.size() <= 2) {
          for (int j = 0; j < rand.nextInt(3) + 1; j++)
          {
            Cloud newCloud = new Cloud();
            cloudList.add(newCloud);
            wind.attach(newCloud);
            newCloud.updateWind(wind);
            add(newCloud);
          }
        }
        if (rand.nextInt(2) == 1)
        {
          Cloud newCloud = new Cloud();
          cloudList.add(newCloud);
          wind.attach(newCloud);
          newCloud.updateWind(wind);
          add(newCloud);
        }
        wind.detach(cloudList.get(i));
        cloudList.remove(i);
        getChildren().remove(i);
      }
    }
  }
}
class Blimp extends TransientGameObject implements Observer
{
  private ImageView imgView;
  private final Random rand = new Random();
  private GameText refuelText;
  private int refuelCapacity = 0;
  private double windSpeed = 0;
  public Blimp()
  {
    init();
  }
  private void init()
  {
    createBlimp();
    makeBlimpBound();
    objectState = new Created(this);
  }
  private void createBlimp()
  {
    int blimpOffset = 200;
    refuelCapacity = rand.nextInt(5000) + 5000;

    imgView = new ImageView(new Image("blimp.png"));

    refuelText = new GameText("F: " + refuelCapacity);
    refuelText.setColor(Color.YELLOW);
    refuelText.translation(imgView.getBoundsInParent().getWidth()/2 -
            refuelText.getBoundsInParent().getWidth()/2,
        imgView.getBoundsInParent().getHeight()/2);

    translation(-rand.nextInt((int)imgView.getBoundsInLocal().getWidth())-20,
        rand.nextInt((int)(Game.GAME_HEIGHT - (blimpOffset +
            imgView.getBoundsInLocal().getHeight()))) + blimpOffset);

    add(imgView);
    add(refuelText);
  }
  private void makeBlimpBound()
  {
    createBoundingBox(imgView.getBoundsInParent().getMinX(),
        imgView.getBoundsInParent().getMinY(),
        imgView.getBoundsInParent().getWidth(),
        imgView.getBoundsInParent().getHeight());
  }
  protected void moveBlimp()
  {
    myTranslation.setX(myTranslation.getX() + windSpeed);
    objectState.movingObject();
  }
  public void decreaseRefuel()
  {
    if (refuelCapacity > 0)
      refuelCapacity -= 10;
  }
  public double getBlimpSpeed()
  {
    return windSpeed;
  }
  @Override
  public void update() {
    refuelText.setText("F: " + refuelCapacity);
    moveBlimp();
  }

  @Override
  public void updateWind(Wind w) {
    windSpeed = w.windSpeed();
  }
}
class BlimpFactory extends GameObject
{
  private final LinkedList<Blimp> blimpList = new LinkedList<>();
  private final Wind wind = new Wind();
  public BlimpFactory()
  {
    blimpList.add(new Blimp());
    add(blimpList.getFirst());
    wind.attach(blimpList.getFirst());
    blimpList.getFirst().updateWind(wind);
  }
  public Rectangle getBlimpBound()
  {
    return blimpList.getFirst().bbox;
  }
  public void showBlimpBoundingBox()
  {
    blimpList.getFirst().showBoundingBox();
  }
  public void decreaseBlimpRefuel()
  {
    blimpList.getFirst().decreaseRefuel();
  }
  public double getBlimpSpeed()
  {
    return blimpList.getFirst().getBlimpSpeed();
  }
  @Override
  public void update()
  {
    blimpList.getFirst().update();
    if (blimpList.getFirst().isObjectDead())
    {
      wind.detach(blimpList.getFirst());
      getChildren().remove(blimpList.getFirst());
      blimpList.remove();

      Blimp newBlimp = new Blimp();
      blimpList.add(newBlimp);
      add(newBlimp);
      wind.attach(blimpList.getFirst());
      blimpList.getFirst().updateWind(wind);
    }
  }
}
class HeliPad extends GameObject
{
  private final Rectangle heliPad;
  public HeliPad()
  {
    int heliPositionY = 100;
    int outlineWidth = 3;

    heliPad = new Rectangle(100,100);
    heliPad.setStroke(Color.GRAY);
    heliPad.setStrokeWidth(outlineWidth);
    heliPad.setFill(Color.TRANSPARENT);

    Circle padCircle = new Circle();
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
    heli.changeState(new HeliStateStopping(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    spinSpeed += 1;
    if (spinSpeed >= 7) {
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
    heli.changeState(new HeliStateStarting(heli));
  }
  @Override
  public int spinBlade(int spinSpeed){
    spinSpeed -= 1;
    if (spinSpeed == 0) {
      heli.changeState(new HeliStateOff(heli));
    }
    return spinSpeed;
  }
}
class Helicopter extends GameObject implements HeliState
{
  private final GameText fuelText;
  private HeliState state;
  private final HeliBlade heliBlade;
  private int delayRot = 50, bladeRot;
  private final static int offset_Text_X = -25, offset_Text_Y = -55;
  private double heliSpeed, heliHeading, fuel;
  private final static double maxHeliSpeed = 10, minHeliSpeed = -2;
  public Helicopter(double centerX, double centerY)
  {
    fuel = 25000;
    heliSpeed = 0;
    heliHeading = 0;
    bladeRot = 0;

    HeliBody heliBody = new HeliBody();
    heliBlade = new HeliBlade();

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
  public void increaseFuel()
  {
    fuel+=10;
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
  public double getFuel()
  {
    return fuel;
  }
  public double getHeliSpeed()
  {
    return heliSpeed;
  }
  public void changeState(HeliState state)
  {
    this.state = state;
  }
  @Override
  public void toggleIgnition() {
    if (Math.floor(heliSpeed) >= -0.3 && Math.floor(heliSpeed) <= 0.3)
    {
      state.toggleIgnition();
    }
  }
  @Override
  public int spinBlade(int spinSpeed) {
    return state.spinBlade(spinSpeed);
  }
  public boolean isHelicopterTurnOn() {
    return !(state instanceof HeliStateOff);
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
class HeliBody extends GameObject
{
  public HeliBody()
  {
    ImageView imgView = new ImageView( new Image("heliBody.png"));
    translation(0-imgView.getBoundsInParent().getWidth()/2,
        0-imgView.getBoundsInParent().getHeight()/2);
    add(imgView);
  }
}

class HeliBlade extends GameObject
{
  private int bladeRot;
  public HeliBlade()
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