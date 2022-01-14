package com.vaadin.flow.component.map.configuration;

import com.vaadin.flow.component.map.configuration.geometry.SimpleGeometry;
import com.vaadin.flow.component.map.configuration.style.Style;

public class Feature extends AbstractConfigurationObject {

    private SimpleGeometry geometry;
    private Style style;

    @Override
    public String getType() {
        return Constants.OL_FEATURE;
    }

    public SimpleGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(SimpleGeometry geometry) {
        updateNestedPropertyObserver(this.geometry, geometry);
        this.geometry = geometry;
        notifyChange();
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        updateNestedPropertyObserver(this.style, style);
        this.style = style;
        notifyChange();
    }
}
