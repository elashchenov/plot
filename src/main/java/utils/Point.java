package utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.DataFormat;

import java.text.DecimalFormat;

public class Point {
    private DoubleProperty x;
    private DoubleProperty y;

    public Point(Double x, Double y) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }


    public void setX(double x) {
        this.x.set(x);
    }

    public void setY(double y) {
        this.y.set(y);
    }


    public DoubleProperty xProperty() {
        return x;
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double distance(Point point) {
        return Math.sqrt(Math.pow(getX() - point.getX(), 2) +  Math.pow(getY() - point.getY(), 2));
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#0.000");
        return "(" + df.format(x.get()) + "; " + df.format(y.get()) + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Point)) return false;
        Point otherPoint = (Point) other;
        return (getX() == otherPoint.getX()) && (getY() == otherPoint.getY());
    }
}
