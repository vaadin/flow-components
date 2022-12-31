/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

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
    private Text text;

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

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        removeChild(this.text);
        this.text = text;
        addChild(text);
    }
}
