import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Helicopter extends GameObject implements HeliState {
  private final GameText fuelText;
  private HeliState state;
  private final HeliBlade heliBlade;
  private int delayRot = 50, bladeRot;
  private final static int offset_Text_X = -25, offset_Text_Y = -55;
  private double heliSpeed, heliHeading, fuel;
  private final static double maxHeliSpeed = 10, minHeliSpeed = -2;

  public Helicopter(double centerX, double centerY) {
    fuel = 25000;
    heliSpeed = 0;
    heliHeading = 0;
    bladeRot = 0;

    HeliBody heliBody = new HeliBody();
    heliBlade = new HeliBlade();

    fuelText = new GameText("F:" + (int) fuel);
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

  private void makeHeliBound() {
    createBoundingBox(heliBlade.getBoundsInParent().getMinX(),
        heliBlade.getBoundsInParent().getMinY(),
        heliBlade.getBoundsInParent().getWidth(),
        heliBlade.getBoundsInParent().getHeight());
  }

  public Rectangle getHeliBound() {
    return bbox;
  }

  public double getVx() {
    return -Math.sin(Math.toRadians(getMyRotation())) * heliSpeed;
  }

  public double getVy() {
    return Math.cos(Math.toRadians(getMyRotation())) * heliSpeed;
  }

  public void rotateLeft() {
    if (state instanceof HeliStateReady) {
      heliHeading -= 15;
      if (getMyRotation() >= 345) {
        heliHeading = 0;
      }
    }
  }

  public void rotateRight() {
    if (state instanceof HeliStateReady) {
      heliHeading += 15;
      if (getMyRotation() <= -345) {
        heliHeading = 0;
      }
    }
  }

  public void increaseSpeed() {
    if (state instanceof HeliStateReady)
      heliSpeed += 0.1;
  }

  public void decreaseSpeed() {
    if (state instanceof HeliStateReady)
      heliSpeed -= 0.1;
  }

  public boolean isFuelEmpty() {
    return Math.floor(fuel) == 0;
  }

  private void moveHelicopter() {
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

  public void increaseFuel() {
    fuel += 10;
  }

  private void decreaseFuel() {
    fuelText.setText("F:" + (int) fuel);
    if (fuel >= 0) {
      fuel = fuel - (1 + (heliSpeed / maxHeliSpeed) +
          Math.abs(heliSpeed / minHeliSpeed));
    } else {
      fuel = 0;
    }
  }

  public double getFuel() {
    return fuel;
  }

  public double getHeliSpeed() {
    return heliSpeed;
  }

  public void changeState(HeliState state) {
    this.state = state;
  }

  @Override
  public void toggleIgnition() {
    if (Math.floor(heliSpeed) >= -0.3 && Math.floor(heliSpeed) <= 0.3) {
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
