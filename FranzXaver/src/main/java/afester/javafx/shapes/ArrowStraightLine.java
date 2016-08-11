package afester.javafx.shapes;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;

public class ArrowStraightLine extends ArrowLine {

    //private LineTo lineTo = new LineTo();
    private Line line = new Line();

    public ArrowStraightLine(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        update();
    }

    private static int count = 0;

    private void update() {
        System.err.printf("update(%s)%n", count++);

        // update arrow positions and line start/end position
        double rot = Math.toDegrees(Math.atan2(endY - startY, endX - startX));

        start.setPosition(startX, startY, rot + 270, startStyle);
        end.setPosition(endX, endY, rot + 90, endStyle);

        line.setStartX(start.getConnX());
        line.setStartY(start.getConnY());
        line.setEndX(end.getConnX());
        line.setEndY(end.getConnY());
//        moveTo.setX(start.getConnX());
//        moveTo.setY(start.getConnY());
//        lineTo.setX(end.getConnX());
//        lineTo.setY(end.getConnY());

     // Note: A Path always has the same style for all elements.
     // It is not possible to specify different color, different dash style or
     // different widths to specific path elements.
     // hence we need to separately specify the line and the arrows - otherwise we
     // could not specify for example the line dash without impact on the arrow stroke!
     // Also, a path element can not be another path - only basic elements such as
     // line, curve, arc can be part ot a path.

        // reset path elements
        getChildren().setAll(line, start, end); // )Elements().setAll(moveTo, lineTo);
        //getElements().addAll(start.getElements());
        //getElements().addAll(end.getElements());
    }


// TODO: How to delay update() to a later time so that the setters are not unnecessarily
// costly? Means, can we set the new values here only and "schedule" a "repaint"?
    public void setEndX(double x) {
        this.endX = x;
        update();
    }

    public void setEndY(double y) {
        this.endY = y;
        update();
    }


    public void setStartX(double x) {
        this.startX = x;
        update();
    }

    public void setStartY(double y) {
        this.startY = y;
        update();
    }

    public Point2D[] getEditHandles() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("ArrowLine[x1=%s, y1=%s, x2=%s, y2=%s]", -1, -1, -1, -1);
    }

    public void setStartArrow(ArrowStyle style) {
        this.startStyle = style;
        update();
    }


    public void setEndArrow(ArrowStyle style) {
        this.endStyle = style;
        update();
    }


    public ObservableList<Double> getStrokeDashArray() {
        return line.getStrokeDashArray();
    }


    public void setStrokeWidth(Double newWidth) {
        line.setStrokeWidth(newWidth);
        start.setStrokeWidth(newWidth);
        end.setStrokeWidth(newWidth);
    }
}