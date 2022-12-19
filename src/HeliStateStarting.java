class HeliStateStarting implements HeliState {
  Helicopter heli;

  public HeliStateStarting(Helicopter heli) {
    this.heli = heli;
  }

  @Override
  public void toggleIgnition() {
    heli.changeState(new HeliStateStopping(heli));
  }

  @Override
  public int spinBlade(int spinSpeed) {
    spinSpeed += 1;
    if (spinSpeed >= 7) {
      heli.changeState(new HeliStateReady(heli));
    }
    return spinSpeed;
  }
}
