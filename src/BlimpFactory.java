import javafx.scene.shape.Rectangle;

import java.util.LinkedList;

class BlimpFactory extends GameObject {
  private final LinkedList<Blimp> blimpList = new LinkedList<>();
  private final Wind wind = new Wind();

  public BlimpFactory() {
    blimpList.add(new Blimp());
    add(blimpList.getFirst());
    wind.attach(blimpList.getFirst());
    blimpList.getFirst().updateWind(wind);
  }

  public Rectangle getBlimpBound() {
    return blimpList.getFirst().bbox;
  }

  public void showBlimpBoundingBox() {
    blimpList.getFirst().showBoundingBox();
  }

  public void decreaseBlimpRefuel() {
    blimpList.getFirst().decreaseRefuel();
  }

  public double getBlimpSpeed() {
    return blimpList.getFirst().getBlimpSpeed();
  }

  @Override
  public void update() {
    blimpList.getFirst().update();
    if (blimpList.getFirst().isObjectDead()) {
      wind.detach(blimpList.getFirst());
      getChildren().remove(blimpList.getFirst());
      blimpList.remove();

      Blimp newBlimp = new Blimp();
      blimpList.add(newBlimp);
      add(newBlimp);
      wind.attach(blimpList.getFirst());
      blimpList.getFirst().updateWind(wind);
    }
  }
}
