import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

abstract class GameObject extends Group implements Updatable {
  protected Translate myTranslation;
  protected Rotate myRotation;
  protected Scale myScale;
  protected Rectangle bbox;

  public GameObject() {
    myTranslation = new Translate();
    myRotation = new Rotate();
    myScale = new Scale();
    this.getTransforms().addAll(myTranslation, myRotation, myScale);
  }

  public void translation(double tx, double ty) {
    myTranslation.setX(tx);
    myTranslation.setY(ty);
  }

  public void rotation(double degree) {
    myRotation.setAngle(degree);
    myRotation.setPivotX(0);
    myRotation.setPivotY(0);
  }

  public void scale(double sx, double sy) {
    myScale.setX(sx);
    myScale.setY(sy);
  }

  public double getMyRotation() {
    return myRotation.getAngle();
  }

  public void update() {
    for (Node n : getChildren()) {
      if (n instanceof Updatable)
        ((Updatable) n).update();
    }
  }

  public void createBoundingBox(double minX, double minY, double width,
                                double height) {
    bbox = new Rectangle(minX, minY, width, height);
    bbox.setStroke(Color.YELLOW);
    bbox.setFill(Color.TRANSPARENT);
    bbox.setVisible(!bbox.isVisible());
    add(bbox);
  }

  public void showBoundingBox() {
    bbox.setVisible(!bbox.isVisible());
  }

  public void add(Node node) {
    this.getChildren().add(node);
  }
}
