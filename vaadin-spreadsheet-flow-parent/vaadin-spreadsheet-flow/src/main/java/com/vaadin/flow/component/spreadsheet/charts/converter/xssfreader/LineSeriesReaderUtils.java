package com.vaadin.flow.component.spreadsheet.charts.converter.xssfreader;

/**
 * Copyright (C) 2000-2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import java.util.HashMap;
import java.util.Map;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;

import com.vaadin.flow.component.spreadsheet.charts.converter.chartdata.LineSeriesData;

public class LineSeriesReaderUtils {
    public static void setDashStyleForData(LineSeriesData seriesData,
            CTShapeProperties ctShapeProperties) {
        try {
            if (ctShapeProperties.getLn().getNoFill() != null) {
                seriesData.dashStyle = "";
            } else {
                seriesData.dashStyle = LineSeriesReaderUtils.getDashStyleString(
                        ctShapeProperties.getLn().getPrstDash().getVal());
            }
        } catch (NullPointerException e) {
            // instead of null checks
        }
    }

    public static void setLineWidthForData(LineSeriesData seriesData,
            CTShapeProperties ctShapeProperties) {
        try {
            // currently just set line width to 0 if there are no dashes between
            // scatter points
            if (ctShapeProperties.getLn().isSetNoFill()) {
                seriesData.lineWidth = 0;
            }
        } catch (NullPointerException e) {
            // instead of null checks
        }
    }

    public static void setMarkerForData(LineSeriesData seriesData,
            CTMarker marker) {
        try {
            if (marker.getSymbol().getVal() != STMarkerStyle.NONE) {
                seriesData.markerSymbol = marker.getSymbol().getVal().toString()
                        .toUpperCase();
            }
        } catch (NullPointerException e) {
            // instead of null or isSet checks
        }
    }

    private static String getDashStyleString(STPresetLineDashVal.Enum val) {
        @SuppressWarnings("serial")
        final Map<STPresetLineDashVal.Enum, String> map = new HashMap<STPresetLineDashVal.Enum, String>() {
            {
                // see comment in
                // com.vaadin.flow.component.spreadsheet.chartconverter.chartdata.LineSeriesData.dashStyle
                put(STPresetLineDashVal.SOLID, "SOLID");
                put(STPresetLineDashVal.DASH, "DASH");
                put(STPresetLineDashVal.DASH_DOT, "DASHDOT");
                put(STPresetLineDashVal.LG_DASH, "LONGDASH");
                put(STPresetLineDashVal.LG_DASH_DOT, "LONGDASHDOT");
                put(STPresetLineDashVal.LG_DASH_DOT_DOT, "LONGDASHDOTDOT");
                put(STPresetLineDashVal.SYS_DASH, "SHORTDASH");
                put(STPresetLineDashVal.SYS_DASH_DOT, "SHORTDASHDOT");
                put(STPresetLineDashVal.SYS_DASH_DOT_DOT, "SHORTDASHDOTDOT");
                put(STPresetLineDashVal.DOT, "DOT");
                put(STPresetLineDashVal.SYS_DOT, "SHORTDOT");
            }
        };

        if (map.containsKey(val))
            return map.get(val);
        else
            return "";
    }

}
