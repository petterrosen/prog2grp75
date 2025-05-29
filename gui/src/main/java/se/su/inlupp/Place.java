// PROG2 VT2025, inlämningsuppgift del 2
// grupp 75
// Sama Matloub sama3201
// Yasin Akdeve yakk1087
// Petter Rosén pero0033

package se.su.inlupp;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

//Places innehåller noder utan kopplingar
public class Place extends Circle {
    private final String name;
    private boolean selected = false;

    public Place(String name, double x, double y){
        this.name = name;
          setCenterX(x);
        setCenterY(y);

        setRadius(5);
        setFill(Color.BLUE);
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return getCenterX();
    }

    public double getY() {
        return getCenterY();
    }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setFill(Color.RED);
        } else {
            setFill(Color.BLUE);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
