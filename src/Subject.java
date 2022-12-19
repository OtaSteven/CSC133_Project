interface Subject {
  void attach(Observer o);

  void detach(Observer o);

  double windSpeed();
}
