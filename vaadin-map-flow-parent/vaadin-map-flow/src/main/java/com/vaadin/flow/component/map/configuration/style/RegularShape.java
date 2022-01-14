package com.vaadin.flow.component.map.configuration.style;

public abstract class RegularShape extends ImageStyle {

    private Fill fill;
    private Stroke stroke;

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        updateNestedPropertyObserver(this.fill, fill);
        this.fill = fill;
        notifyChange();
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        updateNestedPropertyObserver(this.stroke, stroke);
        this.stroke = stroke;
        notifyChange();
    }
}
