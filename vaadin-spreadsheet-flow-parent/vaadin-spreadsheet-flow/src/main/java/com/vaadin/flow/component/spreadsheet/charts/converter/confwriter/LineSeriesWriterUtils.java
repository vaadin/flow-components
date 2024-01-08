/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.DashStyle;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.MarkerSymbolEnum;
import com.vaadin.flow.component.spreadsheet.charts.converter.Utils;

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
