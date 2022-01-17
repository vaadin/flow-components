package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
