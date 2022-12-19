import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

class BackgroundImg extends Pane {
  public BackgroundImg() {
    Image img = new Image("desertBackground.png");
    ImageView imgView = new ImageView(img);

    getChildren().addAll(imgView);
  }
}
