package com.vaadin.flow.component.map;

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

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import java.util.ArrayList;
import java.util.List;

public class Assets {

    public static final ImageAsset DEFAULT_MARKER = new ImageAsset(
            "defaultMarker", "default-marker.png",
            "/META-INF/resources/frontend/vaadin-map/assets/default-marker.png",
            100, 146);

    private final List<ImageAsset> imageAssets;

    public List<ImageAsset> getImageAssets() {
        return imageAssets;
    }

    public Assets() {
        imageAssets = new ArrayList<>();
        imageAssets.add(DEFAULT_MARKER);
    }

    public static class ImageAsset {
        private final String name;
        private final Image image;
        private final int width;
        private final int height;

        private ImageAsset(String name, String fileName, String resourcePath,
                int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
            StreamResource streamResource = new StreamResource(fileName,
                    () -> getClass().getResourceAsStream(resourcePath));
            image = new Image(streamResource, "");
        }

        public String getName() {
            return name;
        }

        public Image getImage() {
            return image;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
