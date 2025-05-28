package se.su.inlupp;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

//Places innehåller noderna (utan kopplingar)
public class Place extends Circle {
    private final String name;
    //private final double x; no need
    //private final double y;

    private boolean selected = false;

    public Place(String name, double x, double y){
        this.name = name;
        //this.x = x; No need
        //this.y = y;
        setCenterX(x);
        setCenterY(y);

        setRadius(5);
        setFill(Color.BLUE);
        //addEventHandler(MouseEvent.MOUSE_CLICKED, new ClickHandler());
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

    /*public void setSelected(boolean selected) {
        if (this.selected == selected){
            //selected = true;
            setFill(Color.RED);
        }
        else{
            //selected =false;
            setFill(Color.BLUE);
        }
    }*/

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setFill(Color.RED);
        } else {
            setFill(Color.BLUE);
        }
    }

    /*class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            if (!selected){
                setSelected(true);
            } else {
                setSelected(false);
            }
        }
    }*/

    @Override
    public String toString() {
        return name;
    }
}
