package com.vaadin.flow.component.map.configuration;

public class View extends AbstractConfigurationObject {

    private Coordinate center;
    private float rotation;
    private float zoom;

    public View() {
        this.center = new Coordinate(0,0);
        this.rotation = 0;
        this.zoom = 0;
    }

    @Override
    public String getType() {
        return Constants.OL_VIEW;
    }

    public Coordinate getCenter() {
        return center;
    }

    public void setCenter(Coordinate center) {
        this.center = center;
        this.notifyChange();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        this.notifyChange();
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.notifyChange();
    }
}
