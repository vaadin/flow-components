package com.vaadin.flow.component.map.configuration.style;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;

/**
 * Defines how to visually represent a {@link Feature}
 */
public class Style extends AbstractConfigurationObject {

    private ImageStyle image;
    private Fill fill;
    private Stroke stroke;

    @Override
    public String getType() {
        return Constants.OL_STYLE_STYLE;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public ImageStyle getImage() {
        return image;
    }

    public void setImage(ImageStyle image) {
        removeChild(this.image);
        this.image = image;
        addChild(image);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        removeChild(this.fill);
        this.fill = fill;
        addChild(fill);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        removeChild(this.stroke);
        this.stroke = stroke;
        addChild(stroke);
    }
}
