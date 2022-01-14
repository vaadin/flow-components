package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.geometry.Point;
import com.vaadin.flow.component.map.configuration.layer.Layer;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.configuration.feature.CircleFeature;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.source.VectorSource;
import com.vaadin.flow.component.map.configuration.style.CircleStyle;
import com.vaadin.flow.component.map.configuration.style.Fill;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.router.Route;

import java.util.concurrent.ThreadLocalRandom;

@Route("vaadin-map/map-view")
public class MapView extends Div {
    Span numMarkers = new Span();

    public MapView() {
        Map map = new Map();
        map.setWidthFull();
        map.setHeight("400px");

        VectorLayer vectorLayer = new VectorLayer();
        VectorSource vectorSource = new VectorSource();
        vectorLayer.setSource(vectorSource);
        map.addLayer(vectorLayer);

        CircleFeature nurembergMarker = new CircleFeature(new Coordinate(1233058.1696443919, 6351912.406929109));
        vectorSource.addFeature(nurembergMarker);

        NativeButton toggleLayerVisible = new NativeButton("Toggle Layer", e -> {
            Layer layer = map.getBaseLayer();
            layer.setVisible(!layer.isVisible());
        });

        NativeButton useOpenCycleMap = new NativeButton("Use OpenCycleMap", e -> {
            OSMSource source = new OSMSource(new OSMSource.Options().setUrl("https://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey=187baf2db9fc454896c700ef9e87f499"));
            TileLayer layer = new TileLayer();
            layer.setSource(source);
            map.setBaseLayer(layer);
        });

        NativeButton useOpenStreetMap = new NativeButton("Use OpenStreetMap", e -> {
            TileLayer layer = (TileLayer) map.getBaseLayer();
            layer.getSource().setUrl("https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png");
        });

        NativeButton addSeaMapLayer = new NativeButton("Add OpenSeaMap layer", e -> {
            OSMSource seaMapSource = new OSMSource(
                    new OSMSource.Options()
                            .setUrl("https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png")
                            .setOpaque(false));
            TileLayer seaMapLayer = new TileLayer();
            seaMapLayer.setSource(seaMapSource);
            map.addLayer(seaMapLayer);
        });

        NativeButton showNuremberg = new NativeButton("Show Nuremberg", e -> {
            map.getView().setCenter(new Coordinate(1233058.1696443919, 6351912.406929109));
            map.getView().setZoom(10);
        });

        NativeButton showSaintNazaire = new NativeButton("Show Saint Nazaire", e -> {
            map.getView().setCenter(new Coordinate(-244780.24508882355, 5986452.183179816));
            map.getView().setZoom(15);
        });

        NativeButton addHighLevelMarker = new NativeButton("Add high-level marker", e -> {
            Feature marker = setupHighLevelCircle();
            vectorSource.addFeature(marker);
            updateNumMarkers(vectorSource);
        });

        NativeButton addLowLevelMarker = new NativeButton("Add low-level marker", e -> {
            Feature marker = setupLowLevelCircle();
            vectorSource.addFeature(marker);
            updateNumMarkers(vectorSource);
        });

        NativeButton changeMarkerColor = new NativeButton("Change marker color", e -> {
            nurembergMarker.setFillColor("green");
        });

        add(map);
        add(new Div(
                toggleLayerVisible,
                useOpenCycleMap,
                useOpenStreetMap,
                addSeaMapLayer,
                addHighLevelMarker,
                addLowLevelMarker,
                changeMarkerColor,
                showNuremberg,
                showSaintNazaire
        ));

        add(new Div(numMarkers));
    }

    private void updateNumMarkers(VectorSource vectorSource) {
        numMarkers.setText("Number of markers: " + vectorSource.getFeatures().size());
    }

    private Feature setupLowLevelCircle() {
        CircleStyle circleStyle = new CircleStyle();
        circleStyle.setRadius(7);
        Fill fill = new Fill();
        fill.setColor("hsla(214, 100%, 49%, 0.76)");
        Stroke stroke = new Stroke();
        stroke.setColor("hsla(214, 41%, 17%, 0.83)");
        stroke.setWidth(2);
        circleStyle.setFill(fill);
        circleStyle.setStroke(stroke);

        Style pointStyle = new Style();
        pointStyle.setImage(circleStyle);

        double x = ThreadLocalRandom.current().nextDouble(-20026376.39, 20026376.39);
        double y = ThreadLocalRandom.current().nextDouble(-20048966.10, 20048966.10);
        Point point = new Point(new Coordinate(x, y));

        Feature feature = new Feature();
        feature.setGeometry(point);
        feature.setStyle(pointStyle);

        return feature;
    }

    private Feature setupHighLevelCircle() {
        double x = ThreadLocalRandom.current().nextDouble(-20026376.39, 20026376.39);
        double y = ThreadLocalRandom.current().nextDouble(-20048966.10, 20048966.10);
        return new CircleFeature(new Coordinate(x, y));
    }
}
