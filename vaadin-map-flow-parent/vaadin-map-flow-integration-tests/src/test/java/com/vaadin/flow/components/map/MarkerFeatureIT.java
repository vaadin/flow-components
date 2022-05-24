package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TestPath("vaadin-map/marker-feature")
public class MarkerFeatureIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement addDefaultMarkerFeature;
    private TestBenchElement addCustomMarkerFeature;
    private TestBenchElement updateMarkerCoordinates;
    private TestBenchElement updateMarkerIcon;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).first();
        addDefaultMarkerFeature = $("button").id("add-default-marker-feature");
        addCustomMarkerFeature = $("button").id("add-custom-marker-feature");
        updateMarkerCoordinates = $("button").id("update-marker-coordinates");
        updateMarkerIcon = $("button").id("update-marker-icon");
    }

    @Test
    public void defaultMarkerFeature() {
        addDefaultMarkerFeature.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();

        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate coordinates = feature.getGeometry()
                .getCoordinates();
        Assert.assertEquals(0, coordinates.getX(), 0.001);
        Assert.assertEquals(0, coordinates.getY(), 0.001);

        MapElement.IconReference icon = feature.getStyle().getImage();
        Assert.assertEquals(1, icon.getOpacity(), 0.001);
        Assert.assertEquals(0, icon.getRotation(), 0.001);
        Assert.assertEquals(0.5, icon.getScale(), 0.001);
        Assert.assertNull(icon.getColor());

        // Icon URL is dynamic resource URL
        Pattern urlPattern = Pattern.compile(
                "VAADIN/dynamic/resource/.*/" + Assets.PIN.getFileName());
        Matcher matcher = urlPattern.matcher(icon.getSrc());
        Assert.assertTrue("Icon URL does not match expected pattern: "
                + urlPattern.pattern(), matcher.matches());
    }

    @Test
    public void customMarkerFeature() {
        addCustomMarkerFeature.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate coordinates = feature.getGeometry()
                .getCoordinates();
        Assert.assertEquals(1233058.1696443919, coordinates.getX(), 0.001);
        Assert.assertEquals(6351912.406929109, coordinates.getY(), 0.001);

        MapElement.IconReference icon = feature.getStyle().getImage();
        Assert.assertEquals(0.8f, icon.getOpacity(), 0.001);
        Assert.assertEquals(Math.PI, icon.getRotation(), 0.001);
        Assert.assertEquals(2, icon.getScale(), 0.001);
        Assert.assertEquals("rgb(0, 0, 255)", icon.getColor());
        Assert.assertEquals("assets/custom-marker.png", icon.getSrc());
    }

    @Test
    public void updateMarkerCoordinates() {
        addDefaultMarkerFeature.click();
        updateMarkerCoordinates.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.Coordinate coordinates = feature.getGeometry()
                .getCoordinates();
        Assert.assertEquals(1233058.1696443919, coordinates.getX(), 0.001);
        Assert.assertEquals(6351912.406929109, coordinates.getY(), 0.001);
    }

    @Test
    public void updateMarkerIcon() {
        addDefaultMarkerFeature.click();
        updateMarkerIcon.click();

        MapElement.FeatureCollectionReference features = getDefaultFeatureLayerFeatures();
        Assert.assertEquals(1, features.getLength());

        MapElement.FeatureReference feature = features.getFeature(0);
        Assert.assertTrue(feature.exists());

        MapElement.IconReference icon = feature.getStyle().getImage();
        Assert.assertEquals(0.8f, icon.getOpacity(), 0.001);
        Assert.assertEquals(Math.PI, icon.getRotation(), 0.001);
        Assert.assertEquals(2, icon.getScale(), 0.001);
        Assert.assertEquals("rgb(0, 0, 255)", icon.getColor());
        Assert.assertEquals("assets/custom-marker.png", icon.getSrc());
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
