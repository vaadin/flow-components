package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.TextStyle;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/feature-text")
public class FeatureTextPage extends Div {
    public FeatureTextPage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        MarkerFeature marker1 = new MarkerFeature();
        marker1.setId("marker1");
        marker1.setText("Marker text 1");
        map.getFeatureLayer().addFeature(marker1);

        MarkerFeature marker2 = new MarkerFeature(new Coordinate(30, 0));
        marker2.setId("marker2");
        marker2.setText("Marker text 2");
        map.getFeatureLayer().addFeature(marker2);

        MarkerFeature marker3 = new MarkerFeature(new Coordinate(60, 0));
        marker3.setId("marker3");
        marker3.setText("Marker text 3");
        map.getFeatureLayer().addFeature(marker3);

        NativeButton updateMarkerText = new NativeButton("Update marker text",
                e -> {
                    marker1.setText("Updated text 1");
                    marker2.setText("Updated text 2");
                    marker3.setText("Updated text 3");
                });
        updateMarkerText.setId("update-marker-text");

        NativeButton removeMarkerText = new NativeButton("Remove marker texts",
                e -> {
                    marker1.setText(null);
                    marker2.setText(null);
                    marker3.setText(null);
                });
        removeMarkerText.setId("remove-marker-text");

        NativeButton setTextStyle = new NativeButton("Set custom text style",
                e -> {
                    marker1.getStyle().setTextStyle(createCustomTextStyle());
                });
        setTextStyle.setId("set-text-style");

        NativeButton updateTextStyle = new NativeButton(
                "Update custom text style", e -> {
                    if (marker1.getStyle().getTextStyle() != null) {
                        marker1.getStyle().getTextStyle()
                                .setFont("15px sans-serif");
                    }
                });
        updateTextStyle.setId("update-text-style");

        NativeButton removeTextStyle = new NativeButton(
                "Remove custom text style", e -> {
                    marker1.getStyle().setTextStyle(null);
                });
        removeTextStyle.setId("remove-text-style");

        add(map);
        add(new Div(updateMarkerText, removeMarkerText, setTextStyle,
                updateTextStyle, removeTextStyle));
    }

    private TextStyle createCustomTextStyle() {
        TextStyle textStyle = new TextStyle();
        textStyle.setFont("bold 13px monospace");
        textStyle.setOffset(30, 0);
        textStyle.setTextAlign(TextStyle.TextAlign.LEFT);
        textStyle.setTextBaseline(TextStyle.TextBaseline.BOTTOM);
        textStyle.setFill("#fff");
        textStyle.setStroke("#000", 5);
        textStyle.setBackgroundFill("#1F6B75");
        textStyle.setBackgroundStroke("#fff", 2);
        textStyle.setPadding(3);

        return textStyle;
    }
}
