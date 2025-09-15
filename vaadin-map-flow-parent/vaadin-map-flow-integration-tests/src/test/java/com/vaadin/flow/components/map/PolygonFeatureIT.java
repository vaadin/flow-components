/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.components.map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-map/polygon-feature")
public class PolygonFeatureIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement addSimplePolygonFeature;
    private TestBenchElement addPolygonFeatureWithHole;
    private TestBenchElement movePolygonFeature;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).waitForFirst();
        addSimplePolygonFeature = $("button").id("add-simple-polygon-feature");
        addPolygonFeatureWithHole = $("button")
                .id("add-polygon-feature-with-hole");
        movePolygonFeature = $("button").id("move-polygon-feature");
    }

    @Test
    public void simplePolygonFeature() {
        addSimplePolygonFeature.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();

        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate[][] coordinates = feature.getGeometry()
                .getPolygonCoordinates();

        Assert.assertEquals(1, coordinates.length);
        Assert.assertEquals(5, coordinates[0].length);

        Assert.assertEquals(5.0, coordinates[0][0].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][0].getY(), 0.0001);

        Assert.assertEquals(5.0, coordinates[0][1].getX(), 0.0001);
        Assert.assertEquals(55.0, coordinates[0][1].getY(), 0.0001);

        Assert.assertEquals(15.0, coordinates[0][2].getX(), 0.0001);
        Assert.assertEquals(55.0, coordinates[0][2].getY(), 0.0001);

        Assert.assertEquals(15.0, coordinates[0][3].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][3].getY(), 0.0001);

        Assert.assertEquals(5.0, coordinates[0][4].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][4].getY(), 0.0001);
    }

    @Test
    public void polygonFeatureWithHole() {
        addPolygonFeatureWithHole.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();

        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate[][] coordinates = feature.getGeometry()
                .getPolygonCoordinates();

        Assert.assertEquals(2, coordinates.length);
        Assert.assertEquals(5, coordinates[0].length);

        Assert.assertEquals(5.0, coordinates[0][0].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][0].getY(), 0.0001);

        Assert.assertEquals(5.0, coordinates[0][1].getX(), 0.0001);
        Assert.assertEquals(55.0, coordinates[0][1].getY(), 0.0001);

        Assert.assertEquals(15.0, coordinates[0][2].getX(), 0.0001);
        Assert.assertEquals(55.0, coordinates[0][2].getY(), 0.0001);

        Assert.assertEquals(15.0, coordinates[0][3].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][3].getY(), 0.0001);

        Assert.assertEquals(5.0, coordinates[0][4].getX(), 0.0001);
        Assert.assertEquals(47.0, coordinates[0][4].getY(), 0.0001);

        Assert.assertEquals(5, coordinates[1].length);

        Assert.assertEquals(6.0, coordinates[1][0].getX(), 0.0001);
        Assert.assertEquals(48.0, coordinates[1][0].getY(), 0.0001);

        Assert.assertEquals(6.0, coordinates[1][1].getX(), 0.0001);
        Assert.assertEquals(54.0, coordinates[1][1].getY(), 0.0001);

        Assert.assertEquals(14.0, coordinates[1][2].getX(), 0.0001);
        Assert.assertEquals(54.0, coordinates[1][2].getY(), 0.0001);

        Assert.assertEquals(14.0, coordinates[1][3].getX(), 0.0001);
        Assert.assertEquals(48.0, coordinates[1][3].getY(), 0.0001);

        Assert.assertEquals(6.0, coordinates[1][4].getX(), 0.0001);
        Assert.assertEquals(48.0, coordinates[1][4].getY(), 0.0001);
    }

    @Test
    public void movePolygonFeature() {
        // Add a polygon first
        addSimplePolygonFeature.click();

        // Get initial coordinates
        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        MapElement.FeatureReference feature = features.getFeature(0);
        MapElement.Coordinate[][] originalCoordinates = feature.getGeometry()
                .getPolygonCoordinates();

        // Move the polygon
        movePolygonFeature.click();

        // Get the updated coordinates after translation
        features = getDefaultFeatureLayerFeatures();
        feature = features.getFeature(0);
        MapElement.Coordinate[][] translatedCoordinates = feature.getGeometry()
                .getPolygonCoordinates();

        // Verify all points were moved by the expected amount (5 units right, 3
        // units down)
        for (int i = 0; i < originalCoordinates[0].length; i++) {
            Assert.assertEquals(originalCoordinates[0][i].getX() + 5.0,
                    translatedCoordinates[0][i].getX(), 0.0001);
            Assert.assertEquals(originalCoordinates[0][i].getY() - 3.0,
                    translatedCoordinates[0][i].getY(), 0.0001);
        }
    }

    private MapElement.FeatureCollectionReference getDefaultFeatureLayerFeatures() {
        MapElement.MapReference mapReference = map.getMapReference();
        MapElement.LayerReference featureLayer = mapReference.getLayers()
                .getLayer(1);
        MapElement.VectorSourceReference source = featureLayer.getSource()
                .asVectorSource();
        return source.getFeatures();
    }
}
