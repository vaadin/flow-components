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

import java.util.Map;
import java.util.Objects;

/**
 * Source for WMS servers providing single, untiled images
 */
public class ImageWMSSource extends ImageSource {

    private String url;
    private final Map<String, Object> params;
    private final String serverType;
    private final String crossOrigin;
    private final float ratio;

    public ImageWMSSource(Options options) {
        super(options);
        Objects.requireNonNull(options.params,
                "WMS request parameters must not be null");
        Objects.requireNonNull(options.params.get("LAYERS"),
                "WMS request parameter LAYERS must not be null");
        this.url = options.url;
        this.params = options.params;
        this.serverType = options.serverType;
        this.crossOrigin = options.crossOrigin;
        this.ratio = options.ratio;
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_IMAGE_WMS;
    }

    /**
     * The WMS service URL
     *
     * @return the current URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the WMS service URL
     *
     * @param url
     *            the new URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The WMS request parameters for requesting images from the WMS server. At
     * least the {@code LAYERS} parameter is required. By default,
     * {@code VERSION} is {@code 1.3.0}, and {@code STYLES} is {@code ""}.
     * {@code WIDTH}, {@code HEIGHT}, {@code BBOX}, and {@code CRS} /
     * {@code SRS} will be set dynamically.
     * <p>
     * For individual parameters please refer to the documentation of the WMS
     * server as well as the <a href="https://www.ogc.org/standards/wms">WMS
     * specification</a>.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the WMS parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * The type of WMS server.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the type of WMS server
     */
    public String getServerType() {
        return serverType;
    }

    /**
     * The {@code crossOrigin} attribute for loaded images.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the crossOrigin attribute used for loaded images
     */
    public String getCrossOrigin() {
        return crossOrigin;
    }

    /**
     * The ratio for the size of requested images compared to the map's
     * viewport. Ratio {@code 1} means image requests are the size of the
     * viewport, a ratio of {@code 2} means twice the size of the viewport, and
     * so on. Default is {code 1.5}.
     * <p>
     * This value can not be changed after constructing an instance, it can only
     * be set initially by passing an options object to the constructor.
     *
     * @return the ratio
     */
    public float getRatio() {
        return ratio;
    }

    public static class Options extends ImageSource.Options {
        private String url;
        private Map<String, Object> params;
        private String serverType;
        private String crossOrigin;
        private float ratio = 1.5f;

        /**
         * @see ImageWMSSource#getUrl()
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * @see ImageWMSSource#getParams()
         */
        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        /**
         * @see ImageWMSSource#getServerType()
         */
        public void setServerType(String serverType) {
            this.serverType = serverType;
        }

        /**
         * @see ImageWMSSource#getCrossOrigin()
         */
        public void setCrossOrigin(String crossOrigin) {
            this.crossOrigin = crossOrigin;
        }

        /**
         * @see ImageWMSSource#getRatio()
         */
        public void setRatio(float ratio) {
            this.ratio = ratio;
        }
    }
}
