package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TestPath("vaadin-map/feature-layer")
public class FeatureLayerIT extends AbstractComponentIT {
    private MapElement map;
    private TestBenchElement addDefaultMarkerFeature;
    private TestBenchElement addCustomMarkerFeature;
    private TestBenchElement removeFirstFeature;

    @Before
    public void init() {
        open();
        map = $(MapElement.class).first();
        addDefaultMarkerFeature = $("button").id("add-default-marker-feature");
        addCustomMarkerFeature = $("button").id("add-custom-marker-feature");
        removeFirstFeature = $("button").id("remove-first-feature");
    }

    @Test
    public void defaultMarkerFeature() {
        addDefaultMarkerFeature.click();

        long numFeatures = (long) map.evaluateOLExpression(
                map.getFeatureCollectionExpression() + ".getLength()");
        Assert.assertEquals(1, numFeatures);

        // Default values for MarkerFeature
        ExpectedMarkerFeatureValues expected = new ExpectedMarkerFeatureValues();
        expected.coordinate = new Coordinate(0, 0);
        expected.iconOpacity = 1;
        expected.iconRotation = 0;
        expected.iconScale = 0.3;
        expected.iconColor = null;
        expected.iconSource = Pattern.compile("VAADIN/dynamic/resource/.*/"
                + Assets.DEFAULT_MARKER.getFileName());

        String firstFeature = map.getFeatureCollectionExpression() + ".item(0)";
        assertMarkerFeature(firstFeature, expected);
    }

    @Test
    public void customMarkerFeature() {
        addCustomMarkerFeature.click();

        long numFeatures = (long) map.evaluateOLExpression(
                map.getFeatureCollectionExpression() + ".getLength()");
        Assert.assertEquals(1, numFeatures);

        // Custom values from test page
        ExpectedMarkerFeatureValues expected = new ExpectedMarkerFeatureValues();
        expected.coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        expected.iconOpacity = 0.8f;
        expected.iconRotation = Math.PI;
        expected.iconScale = 2;
        expected.iconColor = new ColorValue(0L, 0L, 255L); // "blue"
        expected.iconSource = Pattern.compile("assets/custom-marker.png");

        String firstFeature = map.getFeatureCollectionExpression() + ".item(0)";
        assertMarkerFeature(firstFeature, expected);
    }

    @Test
    public void multipleFeatures() {
        final int expectedFeatures = 10;

        for (int i = 0; i < expectedFeatures; i++) {
            addDefaultMarkerFeature.click();
        }

        long actualFeatures = (long) map.evaluateOLExpression(
                map.getFeatureCollectionExpression() + ".getLength()");
        Assert.assertEquals(expectedFeatures, actualFeatures);
    }

    @Test
    public void removeFeatures() {
        addDefaultMarkerFeature.click();
        addDefaultMarkerFeature.click();
        addDefaultMarkerFeature.click();
        removeFirstFeature.click();
        removeFirstFeature.click();
        removeFirstFeature.click();

        long actualFeatures = (long) map.evaluateOLExpression(
                map.getFeatureCollectionExpression() + ".getLength()");
        Assert.assertEquals(0, actualFeatures);
    }

    private void assertMarkerFeature(String featureExp,
            ExpectedMarkerFeatureValues expected) {
        // Verify point geometry and position
        String firstFeatureGeometry = featureExp + ".getGeometry()";
        String geometryType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(firstFeatureGeometry));
        double coordinateX = asDouble(map.evaluateOLExpression(
                firstFeatureGeometry + ".getCoordinates()[0]"));
        double coordinateY = asDouble(map.evaluateOLExpression(
                firstFeatureGeometry + ".getCoordinates()[1]"));
        Assert.assertEquals("Point", geometryType);
        Assert.assertEquals(expected.coordinate.getX(), coordinateX, 0.01);
        Assert.assertEquals(expected.coordinate.getY(), coordinateY, 0.01);

        // Verify icon
        String firstFeatureStyle = featureExp + ".getStyle()";
        String firstFeatureImage = firstFeatureStyle + ".getImage()";
        String imageType = (String) map.evaluateOLExpression(
                map.getOLTypeNameExpression(firstFeatureImage));
        double iconOpacity = asDouble(
                map.evaluateOLExpression(firstFeatureImage + ".getOpacity()"));
        double iconRotation = asDouble(
                map.evaluateOLExpression(firstFeatureImage + ".getRotation()"));
        double iconScale = asDouble(
                map.evaluateOLExpression(firstFeatureImage + ".getScale()"));
        ColorValue iconColor = ColorValue.fromObject(
                map.evaluateOLExpression(firstFeatureImage + ".getColor()"));
        String iconSource = (String) map
                .evaluateOLExpression(firstFeatureImage + ".getSrc()");
        Assert.assertEquals("Icon", imageType);
        Assert.assertEquals(expected.iconOpacity, iconOpacity, 0.01);
        Assert.assertEquals(expected.iconRotation, iconRotation, 0.01);
        Assert.assertEquals(expected.iconScale, iconScale, 0.01);
        Assert.assertEquals(expected.iconColor, iconColor);
        Assert.assertNotNull(iconSource);

        Matcher matcher = expected.iconSource.matcher(iconSource);
        Assert.assertTrue(
                "Icon source does not match pattern: value=" + iconSource
                        + "; pattern=" + expected.iconSource.toString(),
                matcher.matches());
    }

    private static double asDouble(Object value) {
        if (value instanceof Double)
            return (double) value;
        if (value instanceof Long)
            return ((Long) value).doubleValue();

        throw new IllegalArgumentException(
                "Value is not convertible to double");
    }

    private static class ExpectedMarkerFeatureValues {
        private Coordinate coordinate;
        private double iconOpacity;
        private double iconRotation;
        private double iconScale;
        private ColorValue iconColor;
        private Pattern iconSource;
    }

    private static class ColorValue {
        private final Long r;
        private final Long g;
        private final Long b;

        public ColorValue(long r, long g, long b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        private static ColorValue fromObject(Object colorLike) {
            if (colorLike == null)
                return null;
            if (colorLike instanceof List) {
                List<Long> valuesList = (List<Long>) colorLike;
                return new ColorValue(valuesList.get(0), valuesList.get(1),
                        valuesList.get(2));
            }
            throw new IllegalArgumentException("Unexpected color value");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ColorValue that = (ColorValue) o;
            return r.equals(that.r) && g.equals(that.g) && b.equals(that.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b);
        }
    }
}
