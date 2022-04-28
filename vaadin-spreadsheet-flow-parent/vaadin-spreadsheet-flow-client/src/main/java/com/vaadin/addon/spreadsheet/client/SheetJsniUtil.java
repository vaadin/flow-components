package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.dom.client.StyleElement;

public class SheetJsniUtil {

    private int parsedCol;
    private int parsedRow;

    public final native void parseColRow(String str)
    /*-{
        var strlen = str.length;
        var i=0;
        var code = 0;
        var flags = 0;
        var r = 0;
        var c = 0;
        while(i<strlen) {
            code = str.charCodeAt(i);
            if(code === 32) {
                flags = flags + 1;
            } else if(code > 47 && code < 58) {
                if(flags === 0) {
                    c = c * 10 + code - 48;
                } else {
                    r = r * 10 + code - 48;
                }
            }
            if (flags === 2) {
                break;
            }
            i++;
        }
        this.@com.vaadin.addon.spreadsheet.client.SheetJsniUtil::parsedRow = r;
        this.@com.vaadin.addon.spreadsheet.client.SheetJsniUtil::parsedCol = c;

    }-*/;

    public int getParsedCol() {
        return parsedCol;
    }

    public int getParsedRow() {
        return parsedRow;
    }

    /** returns 1 for row 2 for column 0 for not header */
    public final native int isHeader(String str)
    /*-{
        try {
            var c = str.charAt(0);
            if (c === 'r' ) {
                c = str.charAt(1);
                if (c === 'h') {
                    return 1;
                }
            } else if (c === 'c') {
                c = str.charAt(1);
                if (c === 'h') {
                return 2;
                }
            }
        } catch (e) {
        }
        return 0;
     }-*/;

    /** returns the header index */
    public final native int parseHeaderIndex(String str)
    /*-{
        var strlen = str.length;
        var i = 0;
        var code = 0;
        var index = 0;
        while(i<strlen) {
            code = str.charCodeAt(i);
            if(code > 47 && code < 58) {
                index = index * 10 + code - 48;
            }
            i++;
        }
        return index;
     }-*/;

    public final native String convertUnicodeIntoCharacter(int charCode)
    /*-{
        return String.fromCharCode(charCode);
     }-*/;

    /** Insert one CSS rule to the end of given stylesheet */
    public native int insertRule(StyleElement stylesheet, String css)
    /*-{
        return stylesheet.sheet.insertRule(css, stylesheet.sheet.cssRules.length);
    }-*/;

    public native void deleteRule(StyleElement stylesheet, int ruleindex)
    /*-{
        stylesheet.sheet.deleteRule(ruleindex);
     }-*/;

    public native int replaceSelector(StyleElement stylesheet, String selector,
            int ruleindex)
    /*-{
        var oldSelector = stylesheet.sheet.cssRules[ruleindex].selectorText;
        var cssText = stylesheet.sheet.cssRules[ruleindex].cssText.replace(oldSelector, selector);
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(cssText, ruleindex);
    }-*/;

    /** Clears the rules starting from the given index */
    public native void clearCSSRules(StyleElement stylesheet)
    /*-{
        var rules = stylesheet.sheet.cssRules? stylesheet.sheet.cssRules : stylesheet.sheet.rules;
        while ( rules.length > 0 ) {
            if (stylesheet.sheet.deleteRule) {
                stylesheet.sheet.deleteRule(0);
            } else {
                stylesheet.sheet.removeRule(0);
            }
        }
    }-*/;

    /** Gets all Overlay rules */
    public native String[] getOverlayRules(StyleElement stylesheet,
            String[] overlaySelectors)
    /*-{
        var overlayRules = [];
        var rules = stylesheet.sheet.cssRules ? stylesheet.sheet.cssRules : stylesheet.sheet.rules;
        for (var ruleIndex = 0; ruleIndex < rules.length; ruleIndex++) {
            var rule = rules[ruleIndex];
            for (var i = 0; i < overlaySelectors.length; i++) {
                // checking for ".rowX," prevents from matching ".rowXY"
                if (rule["selectorText"].indexOf(".row" + overlaySelectors[i]+",") !== -1) {
                    overlayRules.push(rule["cssText"]);
                }
            }
        }
        return overlayRules;
    }-*/;

}
