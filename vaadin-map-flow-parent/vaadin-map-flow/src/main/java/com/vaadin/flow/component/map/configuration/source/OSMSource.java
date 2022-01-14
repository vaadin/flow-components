package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.Constants;

public class OSMSource extends UrlTileSource {

    public OSMSource() {
        this(new Options());
    }

    public OSMSource(Options options) {
        super(options);
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_OSM;
    }

    public static class Options extends UrlTileSource.BaseOptions<Options> {
        public Options() {
            setUrl("https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png");
        }
    }
}
