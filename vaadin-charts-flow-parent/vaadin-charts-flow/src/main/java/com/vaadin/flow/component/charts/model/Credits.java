/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.server.InitParameters;

/**
 * Highchart by default puts a credits label in the lower right corner of the
 * chart. This can be changed using these options.
 */
public class Credits extends AbstractConfigurationObject {

    private Boolean enabled;
    private String href;
    private Position position;
    private Style style;
    private String text;

    public Credits() {
    }

    public Credits(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Whether to show the credits text.
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHref(String)
     */
    public String getHref() {
        return href;
    }

    /**
     * The URL for the credits label.
     * <p>
     * Defaults to: http://www.highcharts.com
     *
     * @throws IllegalArgumentException
     *             if {@code href} uses a scheme that is not considered safe
     *             according to
     *             {@link DeploymentConfiguration#getUrlSafeSchemes()}; see
     *             {@link #setUnsafeHref(String)} and the
     *             {@value InitParameters#URL_SAFE_SCHEMES} configuration
     *             property
     * @see #setUnsafeHref(String)
     */
    public void setHref(String href) {
        if (href != null && !UrlUtil.isSafeUrl(href)) {
            throw new IllegalArgumentException(UrlUtil.getUnsafeUrlMessage(
                    "href", href, "setUnsafeHref(String)"));
        }
        this.href = href;
    }

    /**
     * Sets the URL for the credits label without validating its scheme.
     * <p>
     * Unlike {@link #setHref(String)}, this method does not reject URLs based
     * on the {@value InitParameters#URL_SAFE_SCHEMES} configuration. Use it
     * only for URLs that are fully under your control and known to be safe.
     * Passing untrusted input here can expose the application to cross-site
     * scripting (XSS) attacks.
     *
     * @see #setHref(String)
     *
     * @param href
     *            the URL for the credits label
     */
    public void setUnsafeHref(String href) {
        this.href = href;
    }

    /**
     * @see #setPosition(Position)
     */
    public Position getPosition() {
        if (position == null) {
            position = new Position();
        }
        return position;
    }

    /**
     * Position configuration for the credits label.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        if (style == null) {
            style = new Style();
        }
        return style;
    }

    /**
     * CSS styles for the credits label.
     * <p>
     * Defaults to: { "cursor": "pointer", "color": "#999999", "fontSize":
     * "10px" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    public Credits(String text) {
        this.text = text;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * The text for the credits label.
     * <p>
     * Defaults to: Highcharts.com
     */
    public void setText(String text) {
        this.text = text;
    }
}
