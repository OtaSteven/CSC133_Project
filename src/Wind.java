import java.util.LinkedList;
import java.util.Random;

class Wind implements Subject {
  private final Random rand = new Random();
  private final LinkedList<Observer> observers = new LinkedList<>();

  @Override
  public void attach(Observer o) {
    observers.add(o);
  }

  @Override
  public void detach(Observer o) {
    observers.remove(o);
  }

  @Override
  public double windSpeed() {
    return rand.nextDouble(2) + 1;
  }
}
