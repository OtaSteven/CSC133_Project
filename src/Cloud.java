import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Random;

class Cloud extends TransientGameObject implements Observer {
  private Circle cloud;
  private int cloudCapacity;
  private GameText cloudText;
  private int saturationColor = 255;
  private double windSpeed = 0;

  public Cloud() {
    createCloud();
    makeCloudBound();
    objectState = new Created(this);
  }

  private void createCloud() {
    cloudCapacity = 0;
    Random rand = new Random();
    cloud = new Circle(50, Color.rgb(saturationColor, saturationColor,
        saturationColor));
    cloudText = new GameText("0%");
    cloudText.setTranslateX(cloud.getCenterX() - 15);
    cloudText.setTranslateY(cloud.getCenterY() + 10);
    cloudText.setColor(Color.BLUE);

    translation(-rand.nextInt((int) cloud.getRadius()) - 10,
        rand.nextInt((int) (Game.GAME_HEIGHT - cloud.getRadius())));

    add(cloud);
    add(cloudText);
  }

  private void makeCloudBound() {
    createBoundingBox(cloud.getBoundsInParent().getMinX(),
        cloud.getBoundsInParent().getMinY(),
        cloud.getBoundsInParent().getWidth(),
        cloud.getBoundsInParent().getHeight());
  }

  public Rectangle getCloudBound() {
    return bbox;
  }

  public void increaseCloud() {
    if (cloudCapacity < 100) {
      {
        cloudCapacity++;
        if (saturationColor > 155)
          cloud.setFill(Color.rgb(--saturationColor, --saturationColor,
              --saturationColor));
      }
    }
  }

  public void decreaseCloud() {
    if (cloudCapacity > 0) {
      cloudCapacity -= 1;
      if (saturationColor < 255)
        cloud.setFill(Color.rgb(++saturationColor, ++saturationColor,
            ++saturationColor));
    }
  }

  public boolean isCloudCapacityOver() {
    return cloudCapacity >= 30;
  }

  protected void moveCloud() {
    myTranslation.setX(myTranslation.getX() + windSpeed);
    objectState.movingObject();
  }

  @Override
  public void update() {
    cloudText.setText(cloudCapacity + "%");
    moveCloud();
  }

  @Override
  public void updateWind(Wind w) {
    windSpeed = w.windSpeed();
  }
}
