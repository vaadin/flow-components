/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.feature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Style;

import java.util.Objects;

/**
 * A convenience class for displaying icons or images, such as markers, at a
 * specific location on the map.
 * <p>
 * Technically this is a {@link Feature} that uses a {@link Point} geometry for
 * representation, and a visual {@link Style} configured to use an {@link Icon}.
 * <p>
 * The class provides a default marker icon if no custom icon is provided.
 */
public class MarkerFeature extends PointBasedFeature {

    /**
     * The default icon used for markers, which is a pin pointing at a location
     * on the map
     */
    public static final Icon PIN_ICON;
    /**
     * An alternative icon that displays a point
     */
    public static final Icon POINT_ICON;

    static {
        Icon.ImageSize pinImageSize = new Icon.ImageSize(Assets.PIN.getWidth(),
                Assets.PIN.getHeight());
        Icon.Options pinIconOptions = new Icon.Options();
        pinIconOptions.setImg(Assets.PIN.getResource());
        pinIconOptions.setImgSize(pinImageSize);
        pinIconOptions.setScale(0.5f);
        pinIconOptions.setAnchorOrigin(Icon.AnchorOrigin.BOTTOM_LEFT);
        // Move image slightly downwards to compensate for whitespace at
        // the bottom of the image
        pinIconOptions.setAnchor(new Icon.Anchor(0.5f, 0.12f));
        PIN_ICON = new Icon(pinIconOptions);

        Icon.ImageSize pointImageSize = new Icon.ImageSize(
                Assets.POINT.getWidth(), Assets.POINT.getHeight());
        Icon.Options pointIconOptions = new Icon.Options();
        pointIconOptions.setImg(Assets.POINT.getResource());
        pointIconOptions.setImgSize(pointImageSize);
        pointIconOptions.setScale(0.25f);
        pointIconOptions.setAnchorOrigin(Icon.AnchorOrigin.TOP_LEFT);
        pointIconOptions.setAnchor(new Icon.Anchor(0.5f, 0.5f));
        POINT_ICON = new Icon(pointIconOptions);
    }

    /**
     * Creates a new marker feature displaying a default marker icon.
     */
    public MarkerFeature() {
        this(new Coordinate(0, 0), PIN_ICON);
    }

    /**
     * Creates a new marker feature located at the specified coordinates,
     * displaying a default marker icon. Coordinates must be specified in the
     * map's user projection, which by default is {@code EPSG:4326}, also
     * referred to as GPS coordinates. If the user projection has been changed
     * using {@link Map#setUserProjection(String)}, then coordinates must be
     * specified in that projection instead.
     *
     * @param coordinates
     *            the coordinates that locate the feature
     */
    public MarkerFeature(Coordinate coordinates) {
        this(coordinates, PIN_ICON);
    }

    /**
     * Creates a new marker feature located at the specified coordinates,
     * displaying the specified custom icon. Coordinates must be specified in
     * the map's user projection, which by default is {@code EPSG:4326}, also
     * referred to as GPS coordinates. If the user projection has been changed
     * using {@link Map#setUserProjection(String)}, then coordinates must be
     * specified in that projection instead.
     * <p>
     * <b>NOTE:</b> Icon instances should be reused between features in order to
     * optimize memory-usage in the client-side component / browser. Creating a
     * new instance of an icon for each feature is considered bad practice.
     *
     * @param coordinates
     *            the coordinates that locate the feature
     * @param icon
     *            the icon to display the feature
     */
    public MarkerFeature(Coordinate coordinates, Icon icon) {
        super(coordinates);

        Objects.requireNonNull(coordinates);
        Objects.requireNonNull(icon);

        Style style = new Style();
        setStyle(style);
        setIcon(icon);
    }

    /**
     * The icon used to visually display the marker feature. By default, this is
     * a default marker icon provided by the component.
     * <p>
     * <b>CAUTION:</b> Be careful when modifying the returned icon. Icon
     * instances can, and should always be, reused between multiple markers.
     * Modifying an icon can also affect other marker features. Instead of
     * modifying the icon, consider preparing a set of distinct icons, and then
     * using {@link #setIcon(Icon)}.
     *
     * @return the current icon
     */
    @JsonIgnore
    public Icon getIcon() {
        return (Icon) getStyle().getImage();
    }

    /**
     * Sets the icon used to visually display the marker feature.
     * <p>
     * <b>NOTE:</b> Icon instances should be reused between features in order to
     * optimize memory-usage in the client-side component / browser. Creating a
     * new instance of an icon for each feature is considered bad practice.
     *
     * @param icon
     *            the new icon, not null
     */
    public void setIcon(Icon icon) {
        Objects.requireNonNull(icon);
        getStyle().setImage(icon);
    }

    @Override
    protected void handleAddToParent(AbstractConfigurationObject parent) {
        getStyle().attachImage();
    }

    @Override
    protected void handleRemoveFromParent(AbstractConfigurationObject parent) {
        getStyle().detachImage();
    }
}
