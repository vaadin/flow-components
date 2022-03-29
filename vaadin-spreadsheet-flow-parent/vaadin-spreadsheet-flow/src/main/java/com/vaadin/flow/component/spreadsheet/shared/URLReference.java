/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.spreadsheet.shared;

import java.io.Serializable;

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
     */
    public void setURL(String url) {
        this.url = url;
    }
}
