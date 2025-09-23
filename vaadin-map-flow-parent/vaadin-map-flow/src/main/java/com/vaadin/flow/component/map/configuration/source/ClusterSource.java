/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PointBasedFeature;

/**
 * A source that clusters features within a certain distance.
 * <p>
 * Currently only point-based features such as {@link MarkerFeature} are
 * supported.
 */
public class ClusterSource extends VectorSource {
    private int distance = 20;
    private int minDistance = 0;

    /**
     * Creates a new cluster source.
     */
    public ClusterSource() {
        super(new Options());
    }

    /**
     * Creates a new cluster source with custom options.
     *
     * @param options
     *            the options for the cluster source
     */
    public ClusterSource(Options options) {
        super(options);
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_CLUSTER;
    }

    /**
     * Adds a feature to this source. Only point-based features such as
     * {@link MarkerFeature} are supported. Throws for other feature types.
     *
     * @param feature
     *            the feature to add
     */
    @Override
    public void addFeature(Feature feature) {
        if (!(feature instanceof PointBasedFeature)) {
            throw new IllegalArgumentException(
                    "Only point-based features such as MarkerFeature are supported in Cluster source");
        }
        super.addFeature(feature);
    }

    /**
     * The distance in pixels within which features should be clustered. Default
     * value is 20.
     *
     * @return the distance in pixels within which features should be clustered
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Sets the distance in pixels within which features should be clustered.
     *
     * @param distance
     *            the distance in pixels within which features should be
     *            clustered
     */
    public void setDistance(int distance) {
        this.distance = distance;
        markAsDirty();
    }

    /**
     * Minimum distance in pixels between clusters. Will be capped at the
     * configured distance. By default, no minimum distance is guaranteed. This
     * config can be used to avoid overlapping icons. As a trade-off, the
     * cluster feature's position will no longer be the center of all its
     * features.
     *
     * @return the minimum distance in pixels between clusters
     */
    public int getMinDistance() {
        return minDistance;
    }

    /**
     * Sets the minimum distance in pixels between clusters.
     *
     * @param minDistance
     *            the minimum distance in pixels between clusters
     */
    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
        markAsDirty();
    }
}
