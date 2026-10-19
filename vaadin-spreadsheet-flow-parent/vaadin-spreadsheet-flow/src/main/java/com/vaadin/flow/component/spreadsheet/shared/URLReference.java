/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.shared;

import java.io.Serializable;

/**
 * @since 23.1
 * @deprecated Not used by the component, and no replacement is planned.
 */
@Deprecated(since = "25.4")
public class URLReference implements Serializable {

    private String url;

    /**
     * Returns the URL that this object refers to.
     * <p>
     * Note that the URL can use special protocols like theme://
     *
     * @return The URL for this reference or null if unknown.
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the URL that this object refers to.
     *
     * @param url
     *            the URL
     */
    public void setURL(String url) {
        this.url = url;
    }
}
