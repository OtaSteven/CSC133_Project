class HeliStateOff implements HeliState {
  Helicopter heli;

  public HeliStateOff(Helicopter heli) {
    this.heli = heli;
  }

  @Override
  public void toggleIgnition() {
    heli.changeState(new HeliStateStarting(heli));
  }

  @Override
  public int spinBlade(int spinSpeed) {
    spinSpeed = 0;
    return spinSpeed;
  }
}
