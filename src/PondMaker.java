import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

import java.util.LinkedList;

class PondMaker extends GameObject {
  private final LinkedList<Pond> pondList = new LinkedList<>();

  public PondMaker() {
    for (int i = 0; i < 3; i++) {
      Pond p = new Pond();
      pondList.add(p);
      add(p);
    }
    checkPondIntersect();
  }

  private void checkPondIntersect() {
    for (int i = 0; i < pondList.size(); i++) {
      for (int j = 0; j < pondList.size(); j++) {
        if (i != j && (!Shape.intersect(pondList.get(i).getPondBound(),
            pondList.get(j).getPondBound()).getBoundsInParent().isEmpty())) {
          pondList.get(j).resetPond();
          checkPondIntersect();
        }
      }
    }
  }

  public int getPondListSize() {
    return pondList.size();
  }

  public void showPondsBoundingBox() {
    for (Pond pond : pondList) {
      pond.showBoundingBox();
    }
  }

  public int getTotalPondCap() {
    int totalPondCap = 0;
    for (Pond pond : pondList) {
      totalPondCap += pond.pondCap();
    }
    return totalPondCap;
  }

  public void fillPonds(int n, Point2D cloudPos) {
    pondList.get(n).fillingPond(cloudPos);
  }

  public void update() {
    for (Pond p : pondList) {
      p.update();
    }
  }
}
