import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.Random;

class Blimp extends TransientGameObject implements Observer {
  private ImageView imgView;
  private final Random rand = new Random();
  private GameText refuelText;
  private int refuelCapacity = 0;
  private double windSpeed = 0;

  public Blimp() {
    init();
  }

  private void init() {
    createBlimp();
    makeBlimpBound();
    objectState = new Created(this);
  }

  private void createBlimp() {
    int blimpOffset = 200;
    refuelCapacity = rand.nextInt(5000) + 5000;

    imgView = new ImageView(new Image("blimp.png"));

    refuelText = new GameText("F: " + refuelCapacity);
    refuelText.setColor(Color.YELLOW);
    refuelText.translation(imgView.getBoundsInParent().getWidth() / 2 -
            refuelText.getBoundsInParent().getWidth() / 2,
        imgView.getBoundsInParent().getHeight() / 2);

    translation(-rand.nextInt((int) imgView.getBoundsInLocal().getWidth()) - 20,
        rand.nextInt((int) (Game.GAME_HEIGHT - (blimpOffset +
            imgView.getBoundsInLocal().getHeight()))) + blimpOffset);

    add(imgView);
    add(refuelText);
  }

  private void makeBlimpBound() {
    createBoundingBox(imgView.getBoundsInParent().getMinX(),
        imgView.getBoundsInParent().getMinY(),
        imgView.getBoundsInParent().getWidth(),
        imgView.getBoundsInParent().getHeight());
  }

  protected void moveBlimp() {
    myTranslation.setX(myTranslation.getX() + windSpeed);
    objectState.movingObject();
  }

  public void decreaseRefuel() {
    if (refuelCapacity > 0)
      refuelCapacity -= 10;
  }

  public double getBlimpSpeed() {
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
