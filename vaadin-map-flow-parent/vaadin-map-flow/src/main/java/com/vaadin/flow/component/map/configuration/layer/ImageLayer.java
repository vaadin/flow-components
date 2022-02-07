package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.ImageSource;

/**
 * Layer for server-rendered images that are available in arbitrary extents and
 * resolutions.
 */
public class ImageLayer extends Layer {

    private ImageSource source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_IMAGE;
    }

    /**
     * @return source for this layer, null by default
     */
    public ImageSource getSource() {
        return source;
    }

    /**
     * Sets the source for this layer. The source must be a subclass of
     * {@link ImageSource}, which means that it provides single, untiled images.
     *
     * @param source
     *            the new source for the layer, not null
     */
    public void setSource(ImageSource source) {
        this.source = source;
    }
}
