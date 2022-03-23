package com.vaadin.flow.component.map.configuration.layer;

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
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.Source;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.util.Objects;

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
