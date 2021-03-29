package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.OverlayInfo;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.shared.GroupingData;

public class Parser {

    public static List<GroupingData> parseListOfGroupingData(String colGroupingData) {
        List<GroupingData> l = new ArrayList<>();
        List<String> tokens = parse(colGroupingData);
        for (String token : tokens) {
            String[] ts = token.split("#");
            l.add(new GroupingData(
                    Integer.parseInt(ts[0])
                    , Integer.parseInt(ts[1])
                    , Integer.parseInt(ts[2])
                    , Integer.parseInt(ts[3])
                    , Boolean.parseBoolean(ts[4])
            ));
        }
        return l;
    }

    public static String[] parseArrayOfStrings(String sheetNames) {
        List<String> tokens = parse(sheetNames);
        String[] l = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            l[i] = tokens.get(i);
        }
        return l;
    }

    public static HashMap<Integer, String> parseMapIntegerString(String cellStyleToCSSStyle) {
        List<String> tokens = parse(cellStyleToCSSStyle);
        consoleLog("received: " + cellStyleToCSSStyle);
        tokens.forEach(s -> consoleLog("-->" + s));
        HashMap<Integer, String> l = new HashMap<>();
        for (String token : tokens) {
            List<String> ts = parse(token, '@');
            l.put(Integer.parseInt(ts.get(0)), ts.get(1).substring(1, ts.get(1).length() - 2));
        }
        return l;
    }

    public static HashMap<Integer, Integer> parseMapIntegerInteger(String rowIndexToStyleIndex) {
        List<String> tokens = parse(rowIndexToStyleIndex);
        HashMap<Integer, Integer> l = new HashMap<>();
        for (String token : tokens) {
            String[] ts = token.split("@");
            l.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
        }
        return l;
    }

    public static Set<Integer> parseSetInteger(String lockedColumnIndexes) {
        List<String> tokens = parse(lockedColumnIndexes);
        Set<Integer> l = new HashSet<>();
        for (String token : tokens) {
            l.add(Integer.parseInt(token));
        }
        return l;
    }

    public static ArrayList<String> parseArraylistString(String shiftedCellBorderStyles) {
        ArrayList<String> tokens = parse(shiftedCellBorderStyles);
        return tokens;
    }

    public static ArrayList<Integer> parseArraylistInteger(String hiddenColumnIndexes) {
        List<String> tokens = parse(hiddenColumnIndexes);
        ArrayList<Integer> l = new ArrayList<>();
        for (String token : tokens) {
            l.add(Integer.parseInt(token));
        }
        return l;
    }

    public static int[] parseArrayInt(String verticalScrollPositions) {
        List<String> tokens = parse(verticalScrollPositions);
        int[] l = new int[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            l[i] = Integer.parseInt(tokens.get(i));
        }
        return l;
    }

    public static HashMap<String, String> parseMapStringString(String cellKeysToEditorIdMap) {
        List<String> tokens = parse(cellKeysToEditorIdMap);
        HashMap<String, String> l = new HashMap<>();
        for (String token : tokens) {
            List<String> ts = parse(token, '@');
            l.put(ts.get(0).substring(1, ts.get(0).length() - 2), ts.get(1).substring(1, ts.get(1).length() - 2));
        }
        return l;
    }

    public static Set<String> parseSetString(String invalidFormulaCells) {
        List<String> tokens = parse(invalidFormulaCells);
        Set<String> l = new HashSet<>();
        for (String token : tokens) {
            l.add(token);
        }
        return l;
    }

    public static HashMap<String, OverlayInfo> parseMapStringOverlayInfo(String overlays) {
        List<String> tokens = parse(overlays);
        HashMap<String, OverlayInfo> l = new HashMap<>();
        for (String token : tokens) {
            List<String> ts = parse(token, '@');
            String[] ts2 = ts.get(1).split("#");
            OverlayInfo info = new OverlayInfo();
            info.type = OverlayInfo.Type.valueOf(ts2[0]);
            info.col = Integer.parseInt(ts2[1]);
            info.row = Integer.parseInt(ts2[2]);
            info.width = Float.parseFloat(ts2[3]);
            info.height = Float.parseFloat(ts2[4]);
            info.dy = Float.parseFloat(ts2[5]);
            info.dx = Float.parseFloat(ts2[6]);
            l.put(ts.get(0).substring(1, ts.get(0).length() - 2), info);
        }
        return l;
    }

    public static ArrayList<MergedRegion> parseArrayMergedRegion(String mergedRegions) {
        ArrayList<MergedRegion> l = new ArrayList<>();
        List<String> tokens = parse(mergedRegions);
        for (String token : tokens) {
            String[] ts = token.split("#");
            MergedRegion data = new MergedRegion();
            data.id = Integer.parseInt(ts[0]);
            data.col1 = Integer.parseInt(ts[1]);
            data.col2 = Integer.parseInt(ts[2]);
            data.row1 = Integer.parseInt(ts[3]);
            data.row2 = Integer.parseInt(ts[4]);
            l.add(data);
        }
        return l;
    }

    native static void consoleLog(String message) /*-{
      console.log("parser", message );
  }-*/;

    public static ArrayList<CellData> parseArraylistOfCellData(String cellData) {
        ArrayList<CellData> l = new ArrayList<>();
        consoleLog("received " + cellData);
        List<String> tokens = parse(cellData);
        tokens.forEach(s -> consoleLog("-->" + s));
        for (String token : tokens) {
            String[] ts = token.split("#");
            CellData data = new CellData();
            data.row = Integer.parseInt(ts[0]);
            data.col = Integer.parseInt(ts[1]);
            data.value = ts[2] != null && "null".equals(ts[2])?null:ts[2];
            data.formulaValue = ts[3] != null && "null".equals(ts[3])?null:ts[3];
            data.originalValue = ts[4] != null && "null".equals(ts[4])?null:ts[4];
            data.cellStyle = ts[5] != null && "null".equals(ts[5])?null:ts[5];
            data.locked = Boolean.parseBoolean(ts[6]);
            data.needsMeasure = Boolean.parseBoolean(ts[7]);
            data.isPercentage = Boolean.parseBoolean(ts[8]);
            l.add(data);
        }
        return l;
    }

    public static ArrayList<SpreadsheetActionDetails> parseArraylistSpreadsheetActionDetails(String actionDetails) {
        ArrayList<SpreadsheetActionDetails> l = new ArrayList<>();
        List<String> tokens = parse(actionDetails);
        for (String token : tokens) {
            String[] ts = token.split("#");
            SpreadsheetActionDetails details = new SpreadsheetActionDetails();
            details.caption = ts[0] != null && "null".equals(ts[0])?null:ts[0];
            details.key = ts[1] != null && "null".equals(ts[1])?null:ts[1];
            details.type = Integer.parseInt(ts[2]);
            l.add(details);
        }
        return l;
    }

    private static ArrayList<String> parse(String payload) {
        return parse(payload, ',');
    }

    private static ArrayList<String> parse(String payload, char separator) {
        ArrayList<String> tokens = new ArrayList<>();
        if (payload != null) {
            int pos = 0;
            int start = 0;
            boolean hasNonString = false;
            boolean insideString = false;
            boolean escaped = false;
            while (pos < payload.length()) {
                if (separator == payload.charAt(pos) && !insideString) {
                    if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1));
                    else tokens.add("");
                    start = pos + 1;
                    hasNonString = false;
                } else if ('"' == payload.charAt(pos)) {
                    if (!escaped) {
                        if (insideString) { // end of string
                            insideString = false;
                        } else { // start of string
                            insideString = true;
                        }
                    } else {
                        escaped = false;
                    }
                } else if ('\\' == payload.charAt(pos)) {
                    escaped = true;
                } else if (!insideString) hasNonString = true;
                pos++;
            }
            if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1));
        }
        return tokens;
    }

    public static void main(String[] args) {
        String s = "";
        ArrayList<String> l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));


        s = "\"\"";
        l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));

        s = "swsw\"\"";
        l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));

        s = "swsw\"\",33,\"aaa\"";
        l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));


        s = "swsw\"\",33,\"aa,a\"";
        l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));
    }

}
