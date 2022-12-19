import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

class GameText extends GameObject {
  Text text;

  public GameText(String textString) {
    text = new Text(textString);
    text.setScaleY(-1);
    text.setFill(Color.WHITE);
    text.setFont(Font.font(18));
    add(text);
  }

  public void setText(String textString) {
    text.setText(textString);
  }

  public void setColor(Color col) {
    text.setFill(col);
  }
}
