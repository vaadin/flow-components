package com.vaadin.flow.component.map.configuration.style;

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
