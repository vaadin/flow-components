/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * The loading options control the appearance of the loading screen that covers
 * the plot area on chart operations. This screen only appears after an explicit
 * call to <code>chart.showLoading()</code>. It is a utility for developers to
 * communicate to the end user that something is going on, for example while
 * retrieving new data via an XHR connection. The "Loading..." text itself is
 * not part of this configuration object, but part of the <code>lang</code>
 * object.
 */
public class Loading extends AbstractConfigurationObject {

    private Number hideDuration;
    private Number showDuration;

    public Loading() {
    }

    /**
     * @see #setHideDuration(Number)
     */
    public Number getHideDuration() {
        return hideDuration;
    }

    /**
     * The duration in milliseconds of the fade out effect.
     * <p>
     * Defaults to: 100
     */
    public void setHideDuration(Number hideDuration) {
        this.hideDuration = hideDuration;
    }

    /**
     * @see #setShowDuration(Number)
     */
    public Number getShowDuration() {
        return showDuration;
    }

    /**
     * The duration in milliseconds of the fade in effect.
     * <p>
     * Defaults to: 100
     */
    public void setShowDuration(Number showDuration) {
        this.showDuration = showDuration;
    }
}
