import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

class HeliBlade extends GameObject {
  private int bladeRot;

  public HeliBlade() {
    bladeRot = 0;
    Line blade = new Line(-50, -50, 50, 50);
    blade.setStrokeWidth(5);
    blade.setStroke(Color.GRAY);

    add(blade);
    loop.start();
  }

  public void bladeSpin(int bladeRot) {
    this.bladeRot = bladeRot;
  }

  AnimationTimer loop = new AnimationTimer() {
    @Override
    public void handle(long now) {
      rotation(getMyRotation() - bladeRot);
      getTransforms().clear();
      getTransforms().addAll(myRotation, myTranslation);
    }
  };
}
