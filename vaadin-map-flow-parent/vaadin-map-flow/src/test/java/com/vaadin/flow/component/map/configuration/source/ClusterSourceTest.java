/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;

public class ClusterSourceTest {
    ClusterSource source;

    @Before
    public void setup() {
        source = new ClusterSource();
    }

    @Test
    public void defaultValues() {
        Assert.assertEquals(50, source.getDistance());
        Assert.assertEquals(50, source.getMinDistance());
        Assert.assertEquals(Constants.OL_SOURCE_CLUSTER, source.getType());
    }

    @Test
    public void setDistance() {
        source.setDistance(50);

        Assert.assertEquals(50, source.getDistance());
    }

    @Test
    public void setMinDistance() {
        source.setMinDistance(10);

        Assert.assertEquals(10, source.getMinDistance());
    }

    @Test
    public void addFeature_withMarkerFeature_succeeds() {
        MarkerFeature marker = new MarkerFeature();

        source.addFeature(marker);

        Assert.assertEquals(1, source.getFeatures().size());
        Assert.assertTrue(source.getFeatures().contains(marker));
    }

    @Test
    public void addFeature_withPolygonFeature_throwsException() {
        PolygonFeature polygon = new PolygonFeature();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> source.addFeature(polygon));
    }

    @Test
    public void addFeature_multipleMarkerFeatures_allAdded() {
        MarkerFeature marker1 = new MarkerFeature();
        MarkerFeature marker2 = new MarkerFeature();
        MarkerFeature marker3 = new MarkerFeature();

        source.addFeature(marker1);
        source.addFeature(marker2);
        source.addFeature(marker3);

        Assert.assertEquals(3, source.getFeatures().size());
        Assert.assertTrue(source.getFeatures().contains(marker1));
        Assert.assertTrue(source.getFeatures().contains(marker2));
        Assert.assertTrue(source.getFeatures().contains(marker3));
    }
}
