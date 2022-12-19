import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class HeliBody extends GameObject {
  public HeliBody() {
    ImageView imgView = new ImageView(new Image("heliBody.png"));
    translation(0 - imgView.getBoundsInParent().getWidth() / 2,
        0 - imgView.getBoundsInParent().getHeight() / 2);
    add(imgView);
  }
}
