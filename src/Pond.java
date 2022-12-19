import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Random;

class Pond extends GameObject {
  private Circle pond;
  private Random rand;
  private GameText pondText;
  private int pondCapacity;
  private final int heightOffset = 200;
  private double expansionIncrement = 0.1;

  public Pond() {
    createPond();
    makePondBound();
  }

  private void createPond() {
    rand = new Random();

    pondCapacity = rand.nextInt(17) + 13;
    pond = new Circle();
    pond.setFill(Color.BLUE);
    pond.setRadius(pondCapacity * .5 + pondCapacity);

    pondText = new GameText(pondCapacity + "%");
    pondText.setTranslateX(pond.getCenterX() - 15);
    pondText.setTranslateY(pond.getCenterY() + 10);

    translation(rand.nextInt((int) (Game.GAME_WIDTH + pond.getRadius())),
        rand.nextInt((int) (Game.GAME_HEIGHT - heightOffset)) + heightOffset);

    pondCollidingWall();

    add(pond);
    add(pondText);
  }

  private void makePondBound() {
    createBoundingBox(pond.getBoundsInParent().getMinX(),
        pond.getBoundsInParent().getMinY(),
        pond.getBoundsInParent().getWidth(),
        pond.getBoundsInParent().getHeight());
  }

  public Rectangle getPondBound() {
    return bbox;
  }

  public void pondCollidingWall() {
    if (myTranslation.getX() + (pond.getRadius() * 2) >= Game.GAME_WIDTH ||
        myTranslation.getX() - (pond.getRadius() * 2) <= 0 ||
        myTranslation.getY() + (pond.getRadius() * 2) >= Game.GAME_HEIGHT ||
        myTranslation.getY() + (pond.getRadius() * 2) <= Game.HELI_SPAWN_AREA) {
      resetPond();
    }
  }

  public void fillingPond(Point2D cloudPos) {
    double maxDistance = 4 * (pond.getRadius() * 2);

    double a = Math.abs(myTranslation.getX() - cloudPos.getX());
    double b = Math.abs(myTranslation.getY() - cloudPos.getY());

    double currDistance = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

    if (currDistance < maxDistance) {
      if (pondCapacity < 100) {
        pondCapacity++;
        expandPond();
        expansionIncrement += 0.01;
      }
    }
  }

  public int pondCap() {
    return pondCapacity;
  }

  public void expandPond() {
    pond.setRadius(pond.getRadius() + expansionIncrement);
  }

  public void resetPond() {
    pondText.setTranslateX(pond.getCenterX() - 15);
    pondText.setTranslateY(pond.getCenterY() + 10);

    translation(rand.nextInt((int) (Game.GAME_WIDTH + pond.getRadius())),
        rand.nextInt((int) (Game.GAME_HEIGHT - (pond.getRadius() * 2))) +
            heightOffset);

    pondCollidingWall();
  }

  @Override
  public void update() {
    pondText.setText(pondCapacity + "%");
  }
}
