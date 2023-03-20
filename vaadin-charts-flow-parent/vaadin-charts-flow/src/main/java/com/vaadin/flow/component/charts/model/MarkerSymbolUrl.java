/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Symbol that is fetched from the url, implementing ChartEnum to provide
 * correct serialization
 */
public class MarkerSymbolUrl extends AbstractConfigurationObject
        implements MarkerSymbol, ChartEnum {

    private String url;

    /**
     * Constructs a MarkerSymbol with the given URL
     * 
     * @param url
     */
    public MarkerSymbolUrl(String url) {
        this.setUrl(url);
    }

    /**
     * Sets the URL of the marker symbol
     * 
     * @param url
     */
    public void setUrl(String url) {
        this.url = "url(" + url + ")";
    }

    /**
     * @see #setUrl(String)
     */
    public String getUrl() {
        return url.substring("url(".length(), url.length() - 1);
    }

    @Override
    public String toString() {
        return url;
    }

}
