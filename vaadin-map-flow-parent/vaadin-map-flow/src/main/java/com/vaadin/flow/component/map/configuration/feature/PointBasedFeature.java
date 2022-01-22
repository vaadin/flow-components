package com.vaadin.flow.component.map.configuration.feature;

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

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.Point;

import java.util.Objects;

public abstract class PointBasedFeature extends Feature {

    private final Point point;

    protected PointBasedFeature() {
        this(new Coordinate(0, 0));
    }

    protected PointBasedFeature(Coordinate coordinates) {
        point = new Point(coordinates);
        setGeometry(point);
    }

    public Coordinate getCoordinates() {
        return point.getCoordinates();
    }

    public void setCoordinates(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        point.setCoordinates(coordinates);
    }
}
