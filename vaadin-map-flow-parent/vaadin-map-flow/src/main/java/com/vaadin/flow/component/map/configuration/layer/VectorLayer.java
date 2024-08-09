/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.Source;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

/**
 * Layer for rendering vector data client-side
 */
public class VectorLayer extends Layer {
    // Store source as the more general Source instead of VectorSource
    // In the future we might want to support setting a VectorTileSource as
    // well, which doesn't share the same class hierarchy as VectorSource
    private Source source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_VECTOR;
    }

    /**
     * @return source for this layer, null by default
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Source getSource() {
        return source;
    }

    /**
     * Sets the source for this layer. The source must be a subclass of
     * {@link VectorSource}, which means that it must provide vector data.
     *
     * @param source
     *            the new source for the layer, not null
     */
    public void setSource(VectorSource source) {
        Objects.requireNonNull(source);

        removeChild(this.source);
        this.source = source;
        addChild(this.source);
    }
}
