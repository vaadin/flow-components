package com.vaadin.flow.component.charts.model;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
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
