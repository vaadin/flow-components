/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.ImageSource;

/**
 * Layer for server-rendered images that are available in arbitrary extents and
 * resolutions.
 */
public class ImageLayer extends Layer {

    private ImageSource source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_IMAGE;
    }

    /**
     * @return source for this layer, null by default
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public ImageSource getSource() {
        return source;
    }

    /**
     * Sets the source for this layer. The source must be a subclass of
     * {@link ImageSource}, which means that it provides single, untiled images.
     *
     * @param source
     *            the new source for the layer, not null
     */
    public void setSource(ImageSource source) {
        Objects.requireNonNull(source);
        removeChild(this.source);
        this.source = source;
        addChild(source);
    }
}
