import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

class HeliPad extends GameObject {
  private final Rectangle heliPad;

  public HeliPad() {
    int heliPositionY = 100;
    int outlineWidth = 3;

    heliPad = new Rectangle(100, 100);
    heliPad.setStroke(Color.GRAY);
    heliPad.setStrokeWidth(outlineWidth);
    heliPad.setFill(Color.TRANSPARENT);

    Circle padCircle = new Circle();
    padCircle.setRadius(40);
    padCircle.setStroke(Color.YELLOW);
    heliPad.setStrokeWidth(outlineWidth);
    padCircle.setFill(Color.TRANSPARENT);

    heliPad.setX(padCircle.getCenterX() - heliPad.getWidth() / 2);
    heliPad.setY(padCircle.getCenterY() - heliPad.getHeight() / 2);

    translation((Game.GAME_WIDTH / 2), heliPositionY);

    makeHeliPadBound();

    add(heliPad);
    add(padCircle);
  }

  private void makeHeliPadBound() {
    createBoundingBox(heliPad.getX(), heliPad.getY(), heliPad.getWidth(),
        heliPad.getHeight());
    bbox.setStrokeWidth(3);
  }

  public Rectangle getHeliPadBound() {
    return bbox;
  }
}
