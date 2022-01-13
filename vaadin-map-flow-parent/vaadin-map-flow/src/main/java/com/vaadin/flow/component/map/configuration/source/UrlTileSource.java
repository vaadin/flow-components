package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.Constants;

public abstract class UrlTileSource extends Source {
    private String url;

    @Override
    public String getType() {
        return Constants.OL_SOURCE_URL_TILE;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        notifyChange();
    }
}
