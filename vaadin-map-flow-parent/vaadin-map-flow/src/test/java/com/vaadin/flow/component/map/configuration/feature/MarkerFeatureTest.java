package com.vaadin.flow.component.map.configuration.feature;

import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.style.Icon;
import org.junit.Assert;
import org.junit.Test;

public class MarkerFeatureTest {

    @Test
    public void defaults() {
        MarkerFeature markerFeature = new MarkerFeature();

        Assert.assertNotNull(markerFeature.getCoordinates());
        Assert.assertEquals(0, markerFeature.getCoordinates().getX(), 0);
        Assert.assertEquals(0, markerFeature.getCoordinates().getY(), 0);

        Assert.assertNotNull(markerFeature.getIcon());
        Assert.assertNotNull(markerFeature.getIcon().getImg());
        Assert.assertEquals(Assets.DEFAULT_MARKER.getFileName(),
                markerFeature.getIcon().getImg().getName());

        Assert.assertNotNull(markerFeature.getIcon().getImgSize());
        Assert.assertEquals(Assets.DEFAULT_MARKER.getWidth(),
                markerFeature.getIcon().getImgSize().getWidth());
        Assert.assertEquals(Assets.DEFAULT_MARKER.getHeight(),
                markerFeature.getIcon().getImgSize().getHeight());

        Assert.assertNull(markerFeature.getIcon().getSrc());
    }

    @Test
    public void initializeWithCustomCoordinates() {
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        MarkerFeature markerFeature = new MarkerFeature(coordinate);

        Assert.assertNotNull(markerFeature.getCoordinates());
        Assert.assertEquals(coordinate.getX(),
                markerFeature.getCoordinates().getX(), 0);
        Assert.assertEquals(coordinate.getY(),
                markerFeature.getCoordinates().getY(), 0);
    }

    @Test
    public void initializeWithCustomIcon() {
        Coordinate coordinate = new Coordinate(1233058.1696443919,
                6351912.406929109);
        Icon icon = new Icon(
                new Icon.Options().setSrc("assets/custom-marker.png"));
        MarkerFeature markerFeature = new MarkerFeature(coordinate, icon);

        Assert.assertNotNull(markerFeature.getIcon());
        Assert.assertEquals(markerFeature.getIcon(), markerFeature.getIcon());
    }

    @Test
    public void doesNotAcceptNullValues() {
        Assert.assertThrows(NullPointerException.class,
                () -> new MarkerFeature(null));
        Assert.assertThrows(NullPointerException.class,
                () -> new MarkerFeature(new Coordinate(), null));
    }
}