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

    protected Source(BaseOptions<?> options) {
        Objects.requireNonNull(options);
        this.attributions = options.attributions;
        this.attributionsCollapsible = options.attributionsCollapsible;
        this.projection = options.projection;
    }

    /**
     * @return list of attributions to display for this source
     */
    public List<String> getAttributions() {
        return attributions;
    }

    /**
     * Sets the attributions to display for the source. Attributions can be
     * copyrights and other information that needs to be displayed in order to
     * use map data from a service.
     *
     * @param attributions
     *            the new attributions
     */
    public void setAttributions(List<String> attributions) {
        this.attributions = attributions;
        notifyChange();
    }

    /**
     * Determines whether attributions are collapsible. Default is {@code true}.
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

    protected static class BaseOptions<T extends BaseOptions<T>> {
        private List<String> attributions;
        private boolean attributionsCollapsible = true;
        private String projection;

        /**
         * Extracted to avoid littering unchecked type-casts in all setters
         */
        protected T getThis() {
            return (T) this;
        }

        /**
         * @see Source#setAttributions(List)
         */
        public T setAttributions(List<String> attributions) {
            this.attributions = attributions;
            return getThis();
        }

        /**
         * @see Source#isAttributionsCollapsible()
         */
        public T setAttributionsCollapsible(boolean attributionsCollapsible) {
            this.attributionsCollapsible = attributionsCollapsible;
            return getThis();
        }

        /**
         * @see Source#getProjection()
         */
        public T setProjection(String projection) {
            this.projection = projection;
            return getThis();
        }
    }
}
