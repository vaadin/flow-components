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
import com.vaadin.flow.component.map.configuration.source.TileSource;
import com.vaadin.flow.component.map.configuration.source.UrlTileSource;

import java.util.Objects;

/**
 * Layer for displaying tiled map data
 */
public class TileLayer extends Layer {
    private TileSource source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_TILE;
    }

    /**
     * @return source for this layer, null by default
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public TileSource getSource() {
        return source;
    }

    /**
     * Sets the source for this layer. The source must be a subclass of
     * {@link TileSource}, which means that it must provide tiled map data.
     *
     * @param source
     *            the new source for the layer, not null
     */
    public void setSource(UrlTileSource source) {
        Objects.requireNonNull(source);

        removeChild(this.source);
        this.source = source;
        addChild(source);
    }
}
