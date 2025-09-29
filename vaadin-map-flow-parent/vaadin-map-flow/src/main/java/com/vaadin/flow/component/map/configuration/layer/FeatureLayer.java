/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.layer;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PointBasedFeature;
import com.vaadin.flow.component.map.configuration.source.ClusterSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.map.configuration.style.TextStyle;

/**
 * Layer that allows to conveniently display a number of geographic features. A
 * {@link Feature} can be anything that should be displayed on top of a map,
 * such as points of interest, vehicles or people.
 * <p>
 * The layer is a high-level abstraction built on top of {@link VectorLayer},
 * and uses a {@link VectorSource} by default.
 * <p>
 * Clustering of point-based features can be enabled using
 * {@link #setClusteringEnabled(boolean)}. When clustering is enabled, only
 * features that extend from {@link PointBasedFeature}, such as
 * {@link MarkerFeature} can be added to the layer. Enabling clustering will
 * switch the layer's source to a {@link ClusterSource}.
 */
public class FeatureLayer extends VectorLayer {

    private boolean clusteringEnabled = false;
    private int clusterDistance = 50;
    private int clusterMinDistance = 50;
    private Style clusterStyle;

    public FeatureLayer() {
        setSource(new VectorSource());
        setClusterStyle(createDefaultClusterStyle());
    }

    @Override
    public String getType() {
        return Constants.OL_LAYER_FEATURE;
    }

    /**
     * The source for this layer. For the feature layer this must always be a
     * {@link VectorSource}
     *
     * @return the source of the layer
     */
    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public VectorSource getSource() {
        return (VectorSource) super.getSource();
    }

    /**
     * The features managed by this layer. This returns an immutable collection,
     * which means it can not be modified. Use {@link #addFeature(Feature)} and
     * {@link #removeFeature(Feature)} instead.
     *
     * @return the features managed by the layer, immutable
     */
    @JsonIgnore
    public List<Feature> getFeatures() {
        return getSource().getFeatures();
    }

    /**
     * Adds a feature to the layer. When clustering is enabled, only features
     * that extend from {@link PointBasedFeature}, such as {@link MarkerFeature}
     * are supported.
     *
     * @param feature
     *            the feature to be added
     * @throws IllegalArgumentException
     *             if clustering is enabled and the feature is not a
     *             {@link PointBasedFeature}
     */
    public void addFeature(Feature feature) {
        this.getSource().addFeature(feature);
    }

    /**
     * Removes a feature from the layer
     *
     * @param feature
     *            the feature to be removed
     */
    public void removeFeature(Feature feature) {
        this.getSource().removeFeature(feature);
    }

    /**
     * Removes all features from the layer
     */
    public void removeAllFeatures() {
        this.getSource().removeAllFeatures();
    }

    /**
     * Returns whether clustering is enabled for this layer.
     *
     * @return true if clustering is enabled, false otherwise
     */
    @JsonIgnore
    public boolean isClusteringEnabled() {
        return clusteringEnabled;
    }

    /**
     * Enables or disables clustering for this layer. When clustering is
     * enabled, the layer will use a {@link ClusterSource} instead of a regular
     * {@link VectorSource}.
     * <p>
     * Only features that extend from {@link PointBasedFeature}, such as
     * {@link MarkerFeature}, are supported for clustering. When enabling
     * clustering, any existing features that are not point based will be
     * removed from the layer's source.
     *
     * @param clusteringEnabled
     *            true to enable clustering, false to disable
     */
    public void setClusteringEnabled(boolean clusteringEnabled) {
        if (this.clusteringEnabled != clusteringEnabled) {
            this.clusteringEnabled = clusteringEnabled;
            switchSourceType();
        }
    }

    /**
     * The distance in pixels within which features should be clustered. Default
     * is 50 pixels.
     * <p>
     * Only applies when clustering is enabled.
     *
     * @return the distance in pixels within which features should be clustered
     */
    @JsonIgnore
    public int getClusterDistance() {
        return clusterDistance;
    }

    /**
     * Sets the distance in pixels within which features should be clustered.
     * <p>
     * Only applies when clustering is enabled.
     *
     * @param clusterDistance
     *            the distance in pixels within which features should be
     *            clustered
     */
    public void setClusterDistance(int clusterDistance) {
        this.clusterDistance = clusterDistance;
        if (clusteringEnabled && getSource() instanceof ClusterSource) {
            ((ClusterSource) getSource()).setDistance(clusterDistance);
        }
    }

    /**
     * Minimum distance in pixels between clusters. Will be capped at the
     * configured distance. By default, no minimum distance is guaranteed. This
     * config can be used to avoid overlapping icons. As a trade-off, the
     * cluster feature's position will no longer be the center of all its
     * features. Default is 50 pixels.
     * <p>
     * Only applies when clustering is enabled.
     *
     * @return the minimum distance between clusters in pixels
     */
    @JsonIgnore
    public int getClusterMinDistance() {
        return clusterMinDistance;
    }

    /**
     * Sets the minimum distance in pixels between clusters.
     * <p>
     * Only applies when clustering is enabled.
     *
     * @param clusterMinDistance
     *            the minimum distance between clusters in pixels
     */
    public void setClusterMinDistance(int clusterMinDistance) {
        this.clusterMinDistance = clusterMinDistance;
        if (clusteringEnabled && getSource() instanceof ClusterSource) {
            ((ClusterSource) getSource()).setMinDistance(clusterMinDistance);
        }
    }

    /**
     * The {@link Style} that defines how individual clusters should be rendered
     * when clustering is enabled. By default, uses an image of a circle with
     * text displaying the number of features in the cluster. Non-clustered
     * features are rendered using their own style.
     *
     * @return the current cluster style
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Style getClusterStyle() {
        return clusterStyle;
    }

    /**
     * Sets the style for individual clusters. This style is applied when
     * clustering is enabled.
     *
     * @param clusterStyle
     *            the new cluster style, not null
     */
    public void setClusterStyle(Style clusterStyle) {
        Objects.requireNonNull(clusterStyle);
        removeChild(this.clusterStyle);
        this.clusterStyle = clusterStyle;
        addChild(clusterStyle);
    }

    private void switchSourceType() {
        VectorSource currentSource = getSource();
        List<Feature> currentFeatures = List
                .copyOf(currentSource.getFeatures());

        VectorSource newSource;
        VectorSource.Options newOptions = createOptionsFromSource(
                currentSource);

        if (clusteringEnabled) {
            ClusterSource clusterSource = new ClusterSource(newOptions);
            clusterSource.setDistance(clusterDistance);
            clusterSource.setMinDistance(clusterMinDistance);
            newSource = clusterSource;
        } else {
            newSource = new VectorSource(newOptions);
        }

        setSource(newSource);

        for (Feature feature : currentFeatures) {
            // When enabling clustering, only add PointBasedFeature types
            if (!clusteringEnabled || feature instanceof PointBasedFeature) {
                newSource.addFeature(feature);
            }
        }
    }

    private VectorSource.Options createOptionsFromSource(VectorSource source) {
        VectorSource.Options options = new VectorSource.Options();
        options.setAttributions(source.getAttributions());
        options.setAttributionsCollapsible(source.isAttributionsCollapsible());
        options.setProjection(source.getProjection());
        return options;
    }

    private static Style createDefaultClusterStyle() {
        Icon.Options iconOptions = new Icon.Options();
        iconOptions.setImg(Assets.CLUSTER.getHandler());
        iconOptions.setImgSize(new Icon.ImageSize(Assets.CLUSTER.getWidth(),
                Assets.CLUSTER.getHeight()));
        iconOptions.setScale(0.5f);
        iconOptions.setAnchorOrigin(Icon.AnchorOrigin.TOP_LEFT);
        iconOptions.setAnchor(new Icon.Anchor(0.5f, 0.5f));
        Icon clusterIcon = new Icon(iconOptions);

        TextStyle textStyle = new TextStyle();
        textStyle.setFont("bold 12px sans-serif");
        textStyle.setFill(new Fill("#fff"));
        textStyle.setStroke(null);
        textStyle.setOffset(0, 0);

        Style style = new Style();
        style.setImage(clusterIcon);
        style.setTextStyle(textStyle);

        return style;
    }
}
