class HeliStateReady implements HeliState {
  Helicopter heli;

  public HeliStateReady(Helicopter heli) {
    this.heli = heli;
  }

  @Override
  public void toggleIgnition() {
    heli.changeState(new HeliStateStopping(heli));
  }

  @Override
  public int spinBlade(int spinSpeed) {
    if (spinSpeed < Game.MAX_ROTATE_BLADE_SPEED)
      spinSpeed++;
    return spinSpeed;
  }
}
