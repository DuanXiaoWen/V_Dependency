package toolWindow.LocalToolWindow.entity;

import java.awt.geom.Point2D;
import java.util.Map;

public class BlueprintTuple {
    private Map<String, Point2D.Float> blueprint;
    private float height;
    private float width;


    public BlueprintTuple(Map<String, Point2D.Float> blueprint, float height, float width) {
        this.blueprint = blueprint;
        this.height = height;
        this.width = width;
    }

    public Map<String, Point2D.Float> getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Map<String, Point2D.Float> blueprint) {
        this.blueprint = blueprint;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}
