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

    public static List<GroupingData> parseListOfGroupingData(String raw) {
        if ("null".equals(raw)) return null;
        List<GroupingData> l = new ArrayList<>();
        List<String> tokens = parse(raw);
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

    public static String[] parseArrayOfStrings(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        String[] l = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            l[i] = tokens.get(i);
        }
        return l;
    }

    public static HashMap<Integer, String> parseMapIntegerString(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        consoleLog("received: " + raw);
        tokens.forEach(s -> consoleLog("-->" + s));
        HashMap<Integer, String> l = new HashMap<>();
        for (String token : tokens) {
            List<String> ts = parse(token, '@');
            l.put(Integer.parseInt(ts.get(0)), ts.get(1));
        }
        return l;
    }

    public static HashMap<Integer, Integer> parseMapIntegerInteger(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        HashMap<Integer, Integer> l = new HashMap<>();
        for (String token : tokens) {
            String[] ts = token.split("@");
            l.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
        }
        return l;
    }

    public static Set<Integer> parseSetInteger(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        Set<Integer> l = new HashSet<>();
        for (String token : tokens) {
            l.add(Integer.parseInt(token));
        }
        return l;
    }

    public static ArrayList<String> parseArraylistString(String raw) {
        if ("null".equals(raw)) return null;
        ArrayList<String> tokens = parse(raw);
        return tokens;
    }

    public static ArrayList<Integer> parseArraylistInteger(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        ArrayList<Integer> l = new ArrayList<>();
        for (String token : tokens) {
            l.add(Integer.parseInt(token));
        }
        return l;
    }

    public static int[] parseArrayInt(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        int[] l = new int[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            l[i] = Integer.parseInt(tokens.get(i));
        }
        return l;
    }

    public static float[] parseArrayFloat(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        float[] l = new float[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            l[i] = Float.parseFloat(tokens.get(i));
        }
        return l;
    }

    public static HashMap<String, String> parseMapStringString(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        HashMap<String, String> l = new HashMap<>();
        for (String token : tokens) {
            List<String> ts = parse(token, '@');
            l.put(ts.get(0), ts.get(1));
        }
        return l;
    }

    public static Set<String> parseSetString(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
        Set<String> l = new HashSet<>();
        for (String token : tokens) {
            l.add(token);
        }
        return l;
    }

    public static HashMap<String, OverlayInfo> parseMapStringOverlayInfo(String raw) {
        if ("null".equals(raw)) return null;
        List<String> tokens = parse(raw);
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
            l.put(ts.get(0), info);
        }
        return l;
    }

    public static ArrayList<MergedRegion> parseArrayMergedRegion(String raw) {
        if ("null".equals(raw)) return null;
        ArrayList<MergedRegion> l = new ArrayList<>();
        List<String> tokens = parse(raw);
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

    public static ArrayList<CellData> parseArraylistOfCellData(String raw) {
        if ("null".equals(raw)) return null;
        ArrayList<CellData> l = new ArrayList<>();
        consoleLog("received " + raw);
        List<String> tokens = parse(raw);
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

    public static ArrayList<SpreadsheetActionDetails> parseArraylistSpreadsheetActionDetails(String raw) {
        if ("null".equals(raw)) return null;
        ArrayList<SpreadsheetActionDetails> l = new ArrayList<>();
        List<String> tokens = parse(raw);
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
                if (!escaped && separator == payload.charAt(pos) && !insideString) {
                    if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1).replaceAll("\\\\", ""));
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
                } else {
                    if (escaped) escaped = false;
                    if (!insideString) hasNonString = true;
                }
                pos++;
            }
            if (pos > start) tokens.add(payload.substring(hasNonString?start:start + 1, hasNonString?pos:pos - 1).replaceAll("\\\\", ""));
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


        s = "1#1#Season#null#Season#cs3#false#false#false,1#2#Month#null#Month#cs4#false#false#false,1#3#Temp.#null#Temp.#cs1#false#false#false,2#1#Spring#null#Spring#cs6#false#false#false,2#2#March#null#March#cs1#false#false#false,2#3#-1\\,80#null#-1\\,8#cs7 r#false#false#false,3#1##null##cs6#false#false#false,3#2#April#null#April#cs1#false#false#false,3#3#3\\,40#null#3\\,4#cs7 r#false#false#false,4#1##null##cs6#false#false#false,4#2#May#null#May#cs1#false#false#false,4#3#10\\,00#null#10#cs7 r#false#false#false,5#1#Spring Avg.#null#Spring Avg.#cs5#false#false#false,5#3#3\\,87#SUBTOTAL(1;C2:C4)#SUBTOTAL(1\\,C2:C4)#cs7 r#false#false#false,6#1#Summer#null#Summer#cs6#false#false#false,6#2#June#null#June#cs1#false#false#false,6#3#14\\,70#null#14\\,7#cs7 r#false#false#false,7#1##null##cs6#false#false#false,7#2#July#null#July#cs1#false#false#false,7#3#16\\,90#null#16\\,9#cs7 r#false#false#false,8#1##null##cs6#false#false#false,8#2#August#null#August#cs1#false#false#false,8#3#15\\,50#null#15\\,5#cs7 r#false#false#false,9#1#Summer Avg.#null#Summer Avg.#cs5#false#false#false,9#3#15\\,70#SUBTOTAL(1;C6:C8)#SUBTOTAL(1\\,C6:C8)#cs7 r#false#false#false,10#1#Autumn#null#Autumn#cs6#false#false#false,10#2#September#null#September#cs1#false#false#false,10#3#10\\,30#null#10\\,3#cs7 r#false#false#false,11#1##null##cs6#false#false#false,11#2#October#null#October#cs1#false#false#false,11#3#5\\,50#null#5\\,5#cs7 r#false#false#false,12#1##null##cs6#false#false#false,12#2#November#null#November#cs1#false#false#false,12#3#0\\,70#null#0\\,7#cs7 r#false#false#false,13#1#Autumn Avg.#null#Autumn Avg.#cs5#false#false#false,13#3#5\\,50#SUBTOTAL(1;C10:C12)#SUBTOTAL(1\\,C10:C12)#cs7 r#false#false#false,14#1#Winter#null#Winter#cs6#false#false#false,14#2#December#null#December#cs1#false#false#false,14#3#-2\\,70#null#-2\\,7#cs7 r#false#false#false,15#1##null##cs6#false#false#false,15#2#January#null#January#cs1#false#false#false,15#3#-4\\,50#null#-4\\,5#cs7 r#false#false#false,16#1##null##cs6#false#false#false,16#2#February#null#February#cs1#false#false#false,16#3#-5\\,30#null#-5\\,3#cs7 r#false#false#false,17#1#Winter Avg.#null#Winter Avg.#cs5#false#false#false,17#3#-4\\,17#SUBTOTAL(1;C14:C16)#SUBTOTAL(1\\,C14:C16)#cs7 r#false#false#false,18#1#Average Temp.#null#Average Temp.#cs5#false#false#false,18#3#5\\,23#SUBTOTAL(1;C2:C16)#SUBTOTAL(1\\,C2:C16)#cs7 r#false#false#false";
        l = parse(s, ',');
        System.out.println("input:" + s);
        l.forEach(x -> System.out.println("output:" + x));
    }

}
