// PROG2 VT2025, inlämningsuppgift
// grupp 75
// Sama Matloub
// Yasin Akdeve
// Petter Rosén pero0033
package se.su.inlupp;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setFill(selected ? Color.RED : Color.BLUE);
    }

    @Override
    public String toString() {
        return name;
    }
}