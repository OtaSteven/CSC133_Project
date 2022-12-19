import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

class Game extends Pane {
  final static int MAX_ROTATE_BLADE_SPEED = 15;
  final static double GAME_WIDTH = 800;
  final static double GAME_HEIGHT = 800;
  final static double HELI_SPAWN_AREA = 250;
  private Text msg;
  private Alert alert;
  private PondMaker pondMaker;
  private CloudMaker cloudMaker;
  private BlimpFactory blimp;
  private HeliPad heliPad;
  private Helicopter helicopter;

  public Game() {
    setScaleY(-1);
    init();
  }

  public void init() {
    getChildren().clear();

    msg = new Text();
    BackgroundImg bgImg = new BackgroundImg();
    pondMaker = new PondMaker();
    cloudMaker = new CloudMaker();
    blimp = new BlimpFactory();
    heliPad = new HeliPad();
    helicopter = new Helicopter(heliPad.myTranslation.getX(),
        heliPad.myTranslation.getY());

    getChildren().add(bgImg);
    getChildren().add(pondMaker);
    getChildren().add(cloudMaker);
    getChildren().add(blimp);
    getChildren().add(heliPad);
    getChildren().add(helicopter);
  }

  private void winLossCondition(AnimationTimer GameTimer) {
    if (helicopter.isFuelEmpty()) {
      GameTimer.stop();
      msg.setText("You have lost! Play again?");
      alert = new Alert(Alert.AlertType.CONFIRMATION, msg.getText(),
          ButtonType.YES, ButtonType.NO);
      alert.setOnHidden(event -> {
        if (alert.getResult() == ButtonType.YES) {
          init();
          run();
        } else {
          Platform.exit();
        }
      });
      alert.show();
    }
    if (!helicopter.isHelicopterTurnOn()) {
      if (averagePondCap() >= 80) {
        GameTimer.stop();
        msg.setText("You have won! Your score is: " + (int) (helicopter.getFuel()
            * ((double) pondMaker.getTotalPondCap() / 100)) + "\nPlay again?");
        alert = new Alert(Alert.AlertType.CONFIRMATION, msg.getText(),
            ButtonType.YES, ButtonType.NO);
        alert.setOnHidden(event -> {
          if (alert.getResult() == ButtonType.YES) {
            init();
            run();
          } else {
            Platform.exit();
          }
        });
        alert.show();
      }
    }
  }

  public void heliLeft() {
    helicopter.rotateLeft();
  }

  public void heliRight() {
    helicopter.rotateRight();
  }

  public void heliAccelerate() {
    helicopter.increaseSpeed();
  }

  public void heliDecelerate() {
    helicopter.decreaseSpeed();
  }

  public void fillCloud() {
    if (helicopter.isHelicopterTurnOn()) {
      for (int i = 0; i < cloudMaker.getCloudListSize(); i++) {
        if (!Shape.intersect(helicopter.getHeliBound(),
                cloudMaker.getCloudBoundingBox(i)).getBoundsInParent().
            isEmpty()) {
          cloudMaker.increaseOnCloud(i);
        }
      }
    }
  }

  private int averagePondCap() {
    return pondMaker.getTotalPondCap() / pondMaker.getPondListSize();
  }

  public void startHelicopter() {
    if (!Shape.intersect(helicopter.getHeliBound(),
        heliPad.getHeliPadBound()).getBoundsInParent().isEmpty()) {
      helicopter.toggleIgnition();
    }
  }

  public void turnOnBoundary() {
    cloudMaker.showCloudsBoundingBox();
    pondMaker.showPondsBoundingBox();
    blimp.showBlimpBoundingBox();
    heliPad.showBoundingBox();
    helicopter.showBoundingBox();
  }

  private void loseCloudSaturation() {
    for (int i = 0; i < cloudMaker.getCloudListSize(); i++)
      cloudMaker.decreaseOnCloud(i);
  }

  public void run() {
    AnimationTimer loop = new AnimationTimer() {
      private int iteration = 0;
      private int fuelIteration = 30;

      @Override
      public void handle(long now) {
        winLossCondition(this);
        helicopter.update();
        blimp.update();

        if (iteration++ % 30 == 0) {
          loseCloudSaturation();
          for (int i = 0; i < cloudMaker.getCloudListSize(); i++) {
            if (cloudMaker.isCloudCapacityOver(i)) {
              for (int j = 0; j < pondMaker.getPondListSize(); j++) {
                pondMaker.fillPonds(j, cloudMaker.getPositionOfCloud(i));
              }
            }
          }
        }
        if (!Shape.intersect(helicopter.getHeliBound(), blimp.getBlimpBound())
            .getBoundsInParent().isEmpty() && (helicopter.getHeliSpeed() >
            blimp.getBlimpSpeed() - 0.7 && helicopter.getHeliSpeed() <
            blimp.getBlimpSpeed() + 0.7)) {
          if (fuelIteration == 0) {
            blimp.decreaseBlimpRefuel();
            helicopter.increaseFuel();
          } else {
            fuelIteration--;
          }
        } else {
          fuelIteration = 100;
        }
        cloudMaker.update();
        pondMaker.update();
      }
    };
    loop.start();
  }
}
