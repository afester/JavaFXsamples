package afester.javafx.examples.board.view;

import afester.javafx.examples.board.model.Pin;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A Pad is a junction which refers to a specific pin of a Part.
 */
public class PadViewTop extends AbstractNodeView {

    private Pin pad;

    /**
     * Creates a new PadView.
     */
    public PadViewTop(Pin pad) {
        super(pad);
        this.pad = pad;

        Shape c = new Circle(pad.getLocalPos().getX(), pad.getLocalPos().getY(), 0.4); // drill*2);

        Text padName = new Text(pad.getLocalPos().getX(), pad.getLocalPos().getY(), this.pad.getPadName());

        // TODO: The rendered text is messed up if the size is too small!
        // A possible solution seems to be to keep the text size larger and 
        // apply an appropriate scaling (and translation) to the Text node
        Font theFont = Font.font("Courier", 10.0);
        padName.setScaleX(0.1);
        padName.setScaleY(0.1);
        padName.setTranslateX(-3);
        padName.setFont(theFont);
        padName.setFill(Color.RED);
        padName.setTextOrigin(VPos.CENTER);

        getChildren().addAll(c, padName);
    }


    @Override
    public String getRepr() {
        return "Pad: " + pad.getPadId();
    }


    @Override
    public String toString() {
        return String.format("PadView[pad=%s]", pad); 
    }


    @Override
    public void setSelected(boolean isSelected) {
    }
}