/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.feature.PolygonFeature;

class ClusterSourceTest {
    ClusterSource source;

    @BeforeEach
    void setup() {
        source = new ClusterSource();
    }

    @Test
    void defaultValues() {
        Assertions.assertEquals(50, source.getDistance());
        Assertions.assertEquals(50, source.getMinDistance());
        Assertions.assertEquals(Constants.OL_SOURCE_CLUSTER, source.getType());
    }

    @Test
    void setDistance() {
        source.setDistance(50);

        Assertions.assertEquals(50, source.getDistance());
    }

    @Test
    void setMinDistance() {
        source.setMinDistance(10);

        Assertions.assertEquals(10, source.getMinDistance());
    }

    @Test
    void addFeature_withMarkerFeature_succeeds() {
        MarkerFeature marker = new MarkerFeature();

        source.addFeature(marker);

        Assertions.assertEquals(1, source.getFeatures().size());
        Assertions.assertTrue(source.getFeatures().contains(marker));
    }

    @Test
    void addFeature_withPolygonFeature_throwsException() {
        PolygonFeature polygon = new PolygonFeature();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> source.addFeature(polygon));
    }

    @Test
    void addFeature_multipleMarkerFeatures_allAdded() {
        MarkerFeature marker1 = new MarkerFeature();
        MarkerFeature marker2 = new MarkerFeature();
        MarkerFeature marker3 = new MarkerFeature();

        source.addFeature(marker1);
        source.addFeature(marker2);
        source.addFeature(marker3);

        Assertions.assertEquals(3, source.getFeatures().size());
        Assertions.assertTrue(source.getFeatures().contains(marker1));
        Assertions.assertTrue(source.getFeatures().contains(marker2));
        Assertions.assertTrue(source.getFeatures().contains(marker3));
    }
}
