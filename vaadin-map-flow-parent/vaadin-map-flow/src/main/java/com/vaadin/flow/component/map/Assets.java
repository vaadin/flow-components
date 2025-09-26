/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.io.Serializable;

import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadHandler;

/**
 * Defines the default assets that the Map component provides
 */
public class Assets {
    public static final ImageAsset PIN = new ImageAsset("pin.png",
            "/META-INF/resources/frontend/vaadin-map/assets/pin.png", 80, 104);
    public static final ImageAsset POINT = new ImageAsset("point.png",
            "/META-INF/resources/frontend/vaadin-map/assets/point.png", 128,
            128);
    public static final ImageAsset CLUSTER = new ImageAsset("cluster.png",
            "/META-INF/resources/frontend/vaadin-map/assets/cluster.png", 72,
            72);

    public static class Asset implements Serializable {
        private final String fileName;
        private final StreamResource resource;
        private final DownloadHandler handler;

        private Asset(String fileName, String resourcePath) {
            this.fileName = fileName;
            this.resource = null;
            // change disposition to inline in pre-defined handlers,
            // where it is 'attachment' by default
            this.handler = DownloadHandler
                    .forClassResource(getClass(), resourcePath, fileName)
                    .inline();
        }

        @Deprecated(since = "24.8", forRemoval = true)
        private Asset(String fileName, StreamResource resource) {
            this.fileName = fileName;
            this.resource = resource;
            this.handler = null;
        }

        private Asset(String fileName, DownloadHandler handler) {
            this.fileName = fileName;
            this.resource = null;
            if (handler instanceof AbstractDownloadHandler<?> preDefinedHandler) {
                // change disposition to inline in pre-defined handlers,
                // where it is 'attachment' by default
                preDefinedHandler.inline();
            }
            this.handler = handler;
        }

        public String getFileName() {
            return fileName;
        }

        @Deprecated(since = "24.8", forRemoval = true)
        public StreamResource getResource() {
            return resource;
        }

        public DownloadHandler getHandler() {
            return handler;
        }
    }

    public static class ImageAsset extends Asset {
        private final int width;
        private final int height;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        private ImageAsset(String fileName, String resourcePath, int width,
                int height) {
            super(fileName, resourcePath);
            this.width = width;
            this.height = height;
        }
    }
}
