package com.vaadin.flow.component.map.configuration.feature;

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

import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Style;

public class MarkerFeature extends PointBasedFeature {

    private static final Icon DEFAULT_MARKER_ICON;

    static {
        Icon.ImageSize imageSize = new Icon.ImageSize(
                Assets.DEFAULT_MARKER.getWidth(),
                Assets.DEFAULT_MARKER.getHeight());

        DEFAULT_MARKER_ICON = new Icon(
                new Icon.Options().setImgName(Assets.DEFAULT_MARKER.getName())
                        .setImgSize(imageSize).setScale(0.3f)
                        .setAnchorOrigin(Icon.AnchorOrigin.BOTTOM_LEFT)
                        .setAnchor(new Icon.Anchor(0.5f, 0)));
    }

    public MarkerFeature() {
        this(new Coordinate(0, 0), DEFAULT_MARKER_ICON);
    }

    public MarkerFeature(Coordinate coordinates) {
        this(coordinates, DEFAULT_MARKER_ICON);
    }

    public MarkerFeature(Coordinate coordinates, Icon icon) {
        super(coordinates);
        Style style = new Style();
        style.setImage(icon);
        setStyle(style);
    }

    public Icon getIcon() {
        return (Icon) getStyle().getImage();
    }
}
