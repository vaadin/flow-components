package com.vaadin.flow.component.map;

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

import com.vaadin.flow.server.StreamResource;

/**
 * Defines the default assets that the Map component provides
 */
public class Assets {
    public static final ImageAsset PIN = new ImageAsset("pin.png",
            "/META-INF/resources/frontend/vaadin-map/assets/pin.png", 80, 104);
    public static final ImageAsset POINT = new ImageAsset("point.png",
            "/META-INF/resources/frontend/vaadin-map/assets/point.png", 128,
            128);

    public static class Asset {
        private final String fileName;
        private final StreamResource resource;

        private Asset(String fileName, String resourcePath) {
            StreamResource resource = new StreamResource(fileName,
                    () -> getClass().getResourceAsStream(resourcePath));

            this.fileName = fileName;
            this.resource = resource;
        }

        private Asset(String fileName, StreamResource resource) {
            this.fileName = fileName;
            this.resource = resource;
        }

        public String getFileName() {
            return fileName;
        }

        public StreamResource getResource() {
            return resource;
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
