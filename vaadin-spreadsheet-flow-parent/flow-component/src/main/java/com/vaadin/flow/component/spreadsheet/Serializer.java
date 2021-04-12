package com.vaadin.flow.component.spreadsheet;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;

public class Serializer {
    public static String serialize(Object value) {
        StringBuffer rs = new StringBuffer();
        if (value != null) {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                rs.append(((Collection)value).stream().map(v -> {
                    if (v == null) {
                        return "null";
                    } else if (v instanceof String) {
                        String s = (String) v;
                        return "\"" + s.replaceAll("\"", "\\\"") + "\"";
                    } else if (v instanceof MergedRegion) {
                        MergedRegion o = (MergedRegion) v;
                        return "" + o.id
                                + "#" + o.col1
                                + "#" + o.col2
                                + "#" + o.row1
                                + "#" + o.row2;
                    } else if (v instanceof GroupingData) {
                        GroupingData o = (GroupingData) v;
                        return "" + o.startIndex
                                + "#" + o.endIndex
                                + "#" + o.level
                                + "#" + o.uniqueIndex
                                + "#" + o.collapsed;
                    } else if (v instanceof CellData) {
                        CellData o = (CellData) v;
                        return "" + o.row
                                + "#" + o.col
                                + "#" + escape(o.value)
                                + "#" + escape(o.formulaValue)
                                + "#" + escape(o.originalValue)
                                + "#" + escape(o.cellStyle)
                                + "#" + o.locked
                                + "#" + o.needsMeasure
                                + "#" + o.isPercentage;
                    } else if (v instanceof SpreadsheetActionDetails) {
                        SpreadsheetActionDetails o = (SpreadsheetActionDetails) v;
                        return "" + escape(o.caption)
                                + "#" + escape(o.key)
                                + "#" + o.type;
                    } else return v.toString();
                }).collect(Collectors.joining(",")));
            } else if (value instanceof float[]) {
                StringBuffer s = new StringBuffer();
                boolean first = true;
                for (float v : (float[]) value) {
                    if (first) first = false;
                    else s.append(",");
                    s.append(v);
                }
                rs.append(s);
            } else if (value instanceof int[]) {
                rs.append(Arrays.stream((int[]) value).mapToObj(String::valueOf).collect(Collectors.joining(",")));
            } else if (value instanceof String[]) {
                rs.append(Arrays.stream((String[]) value).map(s -> "\"" + s.replaceAll("\"", "\\\"") + "\"").collect(Collectors.joining(",")));
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                ((Map)value).forEach((k, v) -> {
                    if (!"".equals(rs.toString())) rs.append(",");
                    rs.append(k instanceof String?"\"" + ((String)k).replaceAll("\"", "\\\"") + "\"":k);
                    rs.append("@");
                    if (v instanceof OverlayInfo) {
                        OverlayInfo i = (OverlayInfo) v;
                        /*
                                    info.type = OverlayInfo.Type.valueOf(ts2[0]);
            info.col = Integer.parseInt(ts2[1]);
            info.row = Integer.parseInt(ts2[2]);
            info.width = Float.parseFloat(ts2[3]);
            info.height = Float.parseFloat(ts2[4]);
            info.dy = Float.parseFloat(ts2[5]);
            info.dx = Float.parseFloat(ts2[6]);

                         */
                        String s = "" + i.type + ""
                                + "#" + i.col
                                + "#" + i.row
                                + "#" + i.width
                                + "#" + i.height
                                + "#" + i.dy
                                + "#" + i.dx;
                        rs.append(s);
                    } else rs.append(v instanceof String?"\"" + ((String)v).replaceAll("\"", "\\\"") + "\"":v);
                });
            }
        } else rs.append("null");
        return rs.toString();
    }

    private static String escape(String value) {
        if (value == null) return null;
        else return value.replaceAll("#", "\\\\#").replaceAll(",", "\\\\,");
    }

}
