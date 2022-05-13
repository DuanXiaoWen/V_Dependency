package toolWindow.LocalToolWindow.entity;

import java.awt.*;

public class Label {

    public Label(String text,Color color){
        this.text=text;
        this.color=color;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private String text;
    private Color color;

}
