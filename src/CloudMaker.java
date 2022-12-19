import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

import java.util.LinkedList;
import java.util.Random;

class CloudMaker extends GameObject {
  private final LinkedList<Cloud> cloudList = new LinkedList<>();
  private final Random rand = new Random();
  private final Wind wind = new Wind();

  public CloudMaker() {
    for (int i = 0; i < rand.nextInt(3) + 2; i++) {
      Cloud newCloud = new Cloud();
      cloudList.add(newCloud);
      wind.attach(newCloud);
      newCloud.updateWind(wind);
      add(newCloud);
    }
  }

  public int getCloudListSize() {
    return cloudList.size();
  }

  public void showCloudsBoundingBox() {
    for (Cloud c : cloudList) {
      c.showBoundingBox();
    }
  }

  public boolean isCloudCapacityOver(int n) {
    return cloudList.get(n).isCloudCapacityOver();
  }

  public Rectangle getCloudBoundingBox(int n) {
    return cloudList.get(n).getCloudBound();
  }

  public void increaseOnCloud(int i) {
    cloudList.get(i).increaseCloud();
  }

  public void decreaseOnCloud(int i) {
    cloudList.get(i).decreaseCloud();
  }

  public Point2D getPositionOfCloud(int i) {
    return new Point2D(cloudList.get(i).myTranslation.getX(),
        cloudList.get(i).myTranslation.getY());
  }

  public void update() {
    for (int i = 0; i < cloudList.size(); i++) {
      cloudList.get(i).update();
      if (cloudList.get(i).isObjectDead()) {
        if (cloudList.size() <= 2) {
          for (int j = 0; j < rand.nextInt(3) + 1; j++) {
            Cloud newCloud = new Cloud();
            cloudList.add(newCloud);
            wind.attach(newCloud);
            newCloud.updateWind(wind);
            add(newCloud);
          }
        }
        if (rand.nextInt(2) == 1) {
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
