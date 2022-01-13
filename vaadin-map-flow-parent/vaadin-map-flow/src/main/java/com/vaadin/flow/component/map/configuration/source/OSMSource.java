package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.Constants;

public class OSMSource extends UrlTileSource {
    @Override
    public String getType() {
        return Constants.OL_SOURCE_OSM;
    }
}
