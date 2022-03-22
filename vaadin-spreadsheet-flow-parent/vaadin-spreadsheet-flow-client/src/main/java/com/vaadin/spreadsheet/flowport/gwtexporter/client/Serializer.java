package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.Map;

public class Serializer {
    public static String serializeMapIntegerFloat(Map<Integer, Float> value) {
        StringBuffer rs = new StringBuffer();
        ((Map)value).forEach((k, v) -> {
            if (!"".equals(rs.toString())) rs.append(",");
            rs.append(k instanceof String?"\"" + ((String)k).replaceAll("\"", "\\\"") + "\"":k);
            rs.append("#");
            rs.append(v);
        });
        return rs.toString();
    }

    public static String serializeMapIntegerInteger(Map<Integer, Integer> value) {
        StringBuffer rs = new StringBuffer();
        ((Map)value).forEach((k, v) -> {
            if (!"".equals(rs.toString())) rs.append(",");
            rs.append(k instanceof String?"\"" + ((String)k).replaceAll("\"", "\\\"") + "\"":k);
            rs.append("#");
            rs.append(v);
        });
        return rs.toString();
    }
}
