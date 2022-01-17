package com.vaadin.flow.component.map.configuration.source;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
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
