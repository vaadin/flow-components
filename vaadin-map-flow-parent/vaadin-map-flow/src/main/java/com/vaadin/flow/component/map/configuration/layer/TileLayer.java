package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.UrlTileSource;

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
        this.source = source;
    }
}
