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

import com.vaadin.flow.component.map.configuration.Constants;

import java.util.Objects;

public abstract class UrlTileSource extends Source {

    private String url;

    private final boolean opaque;

    protected UrlTileSource(BaseOptions options) {
        Objects.requireNonNull(options);

        this.url = options.url;
        this.opaque = options.opaque;
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_URL_TILE;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        notifyChange();
    }

    public boolean isOpaque() {
        return opaque;
    }

    protected static class BaseOptions<T extends BaseOptions<T>> {
        private String url;
        private boolean opaque = true;

        public T setUrl(String url) {
            this.url = url;
            return (T) this;
        }

        public T setOpaque(boolean opaque) {
            this.opaque = opaque;
            return (T) this;
        }
    }

    public static class Options extends BaseOptions<Options> {
    }
}
