class TransientGameObject extends GameObject {
  protected TransientState objectState;

  public void changeState(TransientState state) {
    this.objectState = state;
  }

  public boolean isObjectDead() {
    return objectState instanceof Dead;
  }

  interface TransientState {
    void movingObject();
  }

  class Created implements TransientState {
    private final GameObject object;

    public Created(GameObject object) {
      this.object = object;
    }

    @Override
    public void movingObject() {
      if (object.myTranslation.getX() > 0) {
        changeState(new InView(object));
      }
    }
  }

  class InView implements TransientState {
    private final GameObject object;

    public InView(GameObject object) {
      this.object = object;
    }

    @Override
    public void movingObject() {
      if (object.myTranslation.getX() >= Game.GAME_WIDTH +
          object.getBoundsInLocal().getWidth() / 2) {
        changeState(new Dead());
      }
    }
  }

  static class Dead implements TransientState {
    public Dead() {
      // Object is consider dead and will no longer be in use
    }

    @Override
    public void movingObject() {
      // Object is dead, will be recreated in their respective class after being
      // removed
    }
  }
}
