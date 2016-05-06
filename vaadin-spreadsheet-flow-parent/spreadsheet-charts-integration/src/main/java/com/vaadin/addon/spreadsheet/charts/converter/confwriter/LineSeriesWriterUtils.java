package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.MarkerSymbolEnum;
import com.vaadin.addon.spreadsheet.charts.converter.Utils;

public class LineSeriesWriterUtils {
    public static Marker getMarker(String markerSymbol) {
        if (markerSymbol.isEmpty())
            return new Marker(false);

        Marker marker = new Marker();
        marker.setSymbol(Utils.getEnumValueOrDefault(MarkerSymbolEnum.class,
                markerSymbol, MarkerSymbolEnum.CIRCLE));

        return marker;
    }

    public static DashStyle getDashStyle(String dashStyle) {
        return Utils.getEnumValueOrDefault(DashStyle.class, dashStyle,
                DashStyle.SOLID);
    }
}
