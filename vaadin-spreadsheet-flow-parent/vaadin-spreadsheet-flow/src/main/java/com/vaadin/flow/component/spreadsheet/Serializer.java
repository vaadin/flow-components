package com.vaadin.flow.component.spreadsheet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;

public class Serializer {

    public static String serialize(HashMap<?, ?> map) {
        if (map == null || map.size() == 0) {
            return "";
        }
        boolean escapeKey = map.keySet().iterator().next() instanceof String;
        boolean escapeVal = map.values().iterator().next() instanceof String;
        return map.entrySet().stream()
                .map(e -> (escapeKey ? escapeQuotes(e.getKey()) : e.getKey())
                        + "@"
                        + (escapeVal ? escapeQuotes(e.getValue())
                                : e.getValue()))
                .collect(Collectors.joining(","));
    }

    public static String serialize(Object value) {
        StringBuffer rs = new StringBuffer();
        if (value == null) {
            rs.append("null");
        } else {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                rs.append(((Collection<?>)value).stream().map(v -> {
                    if (v == null) {
                        return "null";
                    } else if (v instanceof String) {
                        String s = (String) v;
                        return escapeQuotes(s);
                    } else if (v instanceof MergedRegion) {
                        MergedRegion o = (MergedRegion) v;
                        return o.id
                                + "#" + o.col1
                                + "#" + o.col2
                                + "#" + o.row1
                                + "#" + o.row2;
                    } else if (v instanceof GroupingData) {
                        GroupingData o = (GroupingData) v;
                        return o.startIndex
                                + "#" + o.endIndex
                                + "#" + o.level
                                + "#" + o.uniqueIndex
                                + "#" + o.collapsed;
                    } else if (v instanceof CellData) {
                        CellData o = (CellData) v;
                        return  o.row
                                + "#" + o.col
                                + "#" + escapeSymbols(o.value)
                                + "#" + escapeSymbols(o.formulaValue)
                                + "#" + escapeSymbols(o.originalValue)
                                + "#" + escapeSymbols(o.cellStyle)
                                + "#" + o.locked
                                + "#" + o.needsMeasure
                                + "#" + o.isPercentage;
                    } else if (v instanceof SpreadsheetActionDetails) {
                        SpreadsheetActionDetails o = (SpreadsheetActionDetails) v;
                        return escapeSymbols(o.caption)
                                + "#" + escapeSymbols(o.key)
                                + "#" + o.type;
                    } else if (v instanceof PopupButton) {
                        PopupButton b = (PopupButton) v;
                        return b.getId().orElse("xxxxxxxxxx")
                                + "#" + b.getState().active
                                + "#" + b.getState().col
                                + "#" + b.getState().row
                                + "#" + b.getState().headerHidden
                                + "#" + b.getState().sheet
                                + "#" + (b.getState().popupWidth != null ? b.getState().popupWidth : "0")
                                + "#" + (b.getState().popupHeight != null ? b.getState().popupWidth : "0")
                                ;
                    } else {
                        return v.toString();
                    }
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
                rs.append(Arrays.stream((int[]) value).mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            } else if (value instanceof String[]) {
                rs.append(Arrays.stream((String[]) value)
                        .map(s -> escapeQuotes(s))
                        .collect(Collectors.joining(",")));
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                ((Map<?,?>)value).forEach((k, v) -> {
                    if (!"".equals(rs.toString())) {
                        rs.append(",");
                    }
                    rs.append(k instanceof String ? escapeQuotes(k) : k);
                    rs.append("@");
                    if (v instanceof OverlayInfo) {
                        OverlayInfo i = (OverlayInfo) v;
                        String s = "" + i.type + ""
                                + "#" + i.col
                                + "#" + i.row
                                + "#" + i.width
                                + "#" + i.height
                                + "#" + i.dy
                                + "#" + i.dx;
                        rs.append(s);
                    } else {
                        rs.append(v instanceof String? escapeQuotes(v): v);
                    }
                });
            }
        }
        return rs.toString();
    }

    private static String escapeSymbols(String value) {
        if (value == null) return null;
        else return value.replaceAll("#", "\\\\#").replaceAll(",", "\\\\,");
    }

    private static String escapeQuotes(Object o) {
        String s = o.toString();
        return "\"" + (s.contains("\"") ? s.replaceAll("\"", "\\\"") : s)+ "\"";
    }

}
