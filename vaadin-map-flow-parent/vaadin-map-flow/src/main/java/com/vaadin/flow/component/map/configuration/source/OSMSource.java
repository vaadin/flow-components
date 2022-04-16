package com.vaadin.flow.component.map.configuration.source;

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

import com.vaadin.flow.component.map.configuration.Constants;

/**
 * Map source for loading tiled images from an OpenStreetMap service. The source
 * will use the official OpenStreetMap service by default. A custom URL can be
 * configured to load data from a different service.
 */
public class OSMSource extends XYZSource {

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

    /**
     * Determines whether attributions are collapsible. For {@link OSMSource}
     * the default is {@code false}, and this value can not be changed in the
     * options.
     *
     * @return whether attributions are collapsible
     */
    @Override
    public boolean isAttributionsCollapsible() {
        return super.isAttributionsCollapsible();
    }

    public static class Options extends XYZSource.Options {
        public Options() {
            setUrl("https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png");
            setAttributionsCollapsible(false);
        }

        @Override
        public void setAttributionsCollapsible(
                boolean attributionsCollapsible) {
            if (attributionsCollapsible) {
                throw new IllegalArgumentException(
                        "OSMSource does not allow to collapse attributions");
            }
            super.setAttributionsCollapsible(false);
        }
    }
}
