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

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all map sources
 */
public abstract class Source extends AbstractConfigurationObject {

    private List<String> attributions;
    private final boolean attributionsCollapsible;
    private final String projection;

    protected Source(Options options) {
        Objects.requireNonNull(options);
        this.attributions = options.attributions;
        this.attributionsCollapsible = options.attributionsCollapsible;
        this.projection = options.projection;
    }

    /**
     * The attributions to display for the source. Attributions can be
     * copyrights and other information that needs to be displayed in order to
     * use map data from a service.
     * <p>
     * This property uses a list to allow displaying a number of attributions.
     * Multiple attributions will be displayed next to each other in the
     * attribution container.
     * <p>
     * By default, the value is {@code null}, which means that default
     * attributions will be displayed, if the specific type of source has any.
     * This should only be the case for {@link OSMSource}.
     *
     * @return the list of current attributions
     */
    public List<String> getAttributions() {
        return attributions;
    }

    /**
     * Sets the attributions to display for the source.
     * <p>
     * Setting this to {@code null} displays the default attributions, if the
     * specific type of source has any. This should only be the case for
     * {@link OSMSource}. Otherwise, the attributions will be cleared.
     *
     * @param attributions
     *            the new attributions
     */
    public void setAttributions(List<String> attributions) {
        this.attributions = attributions;
        markAsDirty();
    }

    /**
     * Determines whether attributions are collapsible. Default is {@code true}.
     * <p>
     * <b>NOTE:</b> Specific types of sources, such as {@link OSMSource}, might
     * not allow collapsing the attributions.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return whether attributions are collapsible
     */
    public boolean isAttributionsCollapsible() {
        return attributionsCollapsible;
    }

    /**
     * The type of coordinate projection to use for this source. For example
     * {@code "EPSG:4326"} or {@code "EPSG:3857"}. Default is null, which uses
     * the projection from the view.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the custom projection to use, or null
     */
    public String getProjection() {
        return projection;
    }

    protected static abstract class Options {
        private List<String> attributions;
        private boolean attributionsCollapsible = true;
        private String projection;

        /**
         * @see Source#getAttributions()
         */
        public void setAttributions(List<String> attributions) {
            this.attributions = attributions;
        }

        /**
         * @see Source#isAttributionsCollapsible()
         */
        public void setAttributionsCollapsible(
                boolean attributionsCollapsible) {
            this.attributionsCollapsible = attributionsCollapsible;
        }

        /**
         * @see Source#getProjection()
         */
        public void setProjection(String projection) {
            this.projection = projection;
        }
    }
}
