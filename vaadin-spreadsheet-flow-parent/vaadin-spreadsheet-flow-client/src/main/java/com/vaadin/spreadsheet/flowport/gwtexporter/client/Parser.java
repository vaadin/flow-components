package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.OverlayInfo;
import com.vaadin.addon.spreadsheet.client.PopupButtonState;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.shared.GroupingData;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JsonUtil;

public class Parser {

    public static List<PopupButtonState> parseListOfPopupButtons(String raw) {
        return parseArrayJstype(raw, PopupButtonState::new);
    }

    public static List<GroupingData> parseListOfGroupingData(String raw){
        return parseArrayJstype(raw, GroupingData::new);
    }

    public static String[] parseArrayOfStrings(String raw) {
        return parseArray(raw, JsonValue::asString).toArray(new String[0]);
    }

    public static HashMap<Integer, String> parseMapIntegerString(String raw) {
        return parseMap(raw, Integer::valueOf, JsonValue::asString);
    }

    public static HashMap<Integer, Integer> parseMapIntegerInteger(String raw) {
        return parseMap(raw, Integer::valueOf, v -> (int)v.asNumber());
    }

    public static Set<Integer> parseSetInteger(String raw) {
        return parseSet(raw, v -> (int)v.asNumber());
    }

    public static ArrayList<String> parseArraylistString(String raw) {
        return parseArray(raw, JsonValue::asString);
    }

    public static ArrayList<Integer> parseArraylistInteger(String raw) {
        return parseArray(raw, v -> (int) v.asNumber());
    }

    public static int[] parseArrayInt(String raw) {
        return parseArray(raw, JsonValue::asNumber).stream().mapToInt(Double::intValue).toArray();
    }

    public static float[] parseArrayFloat(String raw) {
        double[] arr = parseArrayDouble(raw);
        float [] ret = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = (float)arr[i];
        }
        return ret;
    }

    private static double[] parseArrayDouble(String raw) {
        return parseArray(raw, JsonValue::asNumber).stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static HashMap<String, String> parseMapStringString(String raw) {
        return parseMap(raw, String::valueOf, JsonValue::asString);
    }

    public static Set<String> parseSetString(String raw) {
        return parseSet(raw, JsonValue::asString);
    }

    public static HashMap<String, OverlayInfo> parseMapStringOverlayInfo(String raw) {
        return parseMapStringJstype(raw, OverlayInfo::new);
    }

    public static ArrayList<MergedRegion> parseArrayMergedRegion(String raw) {
        return parseArrayJstype(raw, MergedRegion::new);
    }

    public static ArrayList<CellData> parseArraylistOfCellData(String raw) {
        return parseArrayJstype(raw, CellData::new);
    }

    public static ArrayList<SpreadsheetActionDetails> parseArraylistSpreadsheetActionDetails(String raw) {
        return parseArrayJstype(raw, SpreadsheetActionDetails::new);
    }

    private static <T> Set<T> parseSet(String raw, Function<JsonValue, T> jsToJava) {
        List<T> ret = parseArray(raw, jsToJava);
        return ret == null ? null : new HashSet<T>(ret);
    }

    private static <T> ArrayList<T> parseArrayJstype(String raw, Supplier<T> constructor) {
        return parseArray(raw, jsBean -> {
            T javaBean = constructor.get();
            copyJsToJava(jsBean, javaBean);
            return javaBean;
        });
    }

    private static <T> ArrayList<T> parseArray(String raw, Function<JsonValue, T> jsToJava) {
        ArrayList<T> javaArr = new ArrayList<>();
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return javaArr;
        }
        JsonArray jsArr = JsonUtil.parse(raw);
        for (int i = 0; i < jsArr.length(); i++) {
            JsonValue val = jsArr.get(i);
            javaArr.add(jsToJava.apply(val));
        }
        return javaArr;
    }

    private static <T> HashMap<String, T> parseMapStringJstype(String raw, Supplier<T> constructor) {
        return parseMap(raw, String::valueOf, jsBean -> {
            T javaBean = constructor.get();
            copyJsToJava(jsBean, javaBean);
            return javaBean;
        });
    }

    private static <I, T> HashMap<I, T> parseMap(String raw, Function<String, I> strToKey, Function<JsonValue, T> jsToJava) {
        if (raw == null || raw.isEmpty() || "null".equals(raw)) {
            return null;
        }
        JsonObject jsObj = JsonUtil.parse(raw);
        HashMap<I, T> hash = new HashMap<>();
        for (int i = 0; i < jsObj.keys().length; i++) {
            String key = jsObj.keys()[i];
            JsonValue val = jsObj.get(key);
            hash.put(strToKey.apply(key), jsToJava.apply(val));
        }
        return hash;
    }

    private native static void copyJsToJava(JsonValue j, Object o) /*-{
      Object.assign(o, j);
    }-*/;
}
