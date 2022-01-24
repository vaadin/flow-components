package com.vaadin.flow.component.map.configuration.geometry;

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

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;

import java.util.Objects;

public class Point extends SimpleGeometry {

    private Coordinate coordinates;

    @Override
    public String getType() {
        return Constants.OL_GEOMETRY_POINT;
    }

    public Point(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates;
        notifyChange();
    }
}
