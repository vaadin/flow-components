package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.UrlTileSource;

import java.util.Objects;

public class TileLayer extends Layer {
    private UrlTileSource source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_TILE;
    }

    public UrlTileSource getSource() {
        return source;
    }

    public void setSource(UrlTileSource source) {
        Objects.requireNonNull(source);

        updateNestedPropertyObserver(this.source, source);

        this.source = source;
        notifyChange();
    }
}
