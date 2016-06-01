package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
