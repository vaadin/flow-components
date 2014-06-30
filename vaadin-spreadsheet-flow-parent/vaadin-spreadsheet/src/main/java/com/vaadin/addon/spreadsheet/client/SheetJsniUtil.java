package com.vaadin.addon.spreadsheet.client;

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
                flags = 1;
            } else if(code > 47 && code < 58) {
                if(flags === 0) {
                    c = c * 10 + code - 48;
                } else {
                    r = r * 10 + code - 48;
                }
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

    /**
     * Adds the given selector into the specific rule in the stylesheet. The
     * selector should be a single selector or a list of selectors, but should
     * NOT end in a comma (",").
     * 
     * @param stylesheet
     * @param selector
     * @param ruleindex
     */
    public native int addSelector(StyleElement stylesheet, String selector,
            int ruleindex)
    /*-{
        var cssText = selector + ","+stylesheet.sheet.cssRules[ruleindex].cssText;
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(cssText, ruleindex);
     }-*/;

    public native String getSelector(StyleElement stylesheet, int ruleindex)
    /*-{
        var x = stylesheet.sheet.cssRules[ruleindex].selectorText;
        return x;
    }-*/;

    /** Search and update a given CSS rule in a stylesheet */
    public native void updateCSSRule(StyleElement stylesheet, String selector,
            String property, String value)
    /*-{
            var classes = stylesheet.sheet.cssRules;
            for(var x=0;x<classes.length;x++) {
                    if(classes[x].selectorText.toLowerCase()==selector) {
                            classes[x].style[property]=value;
                    }
            }       
    }-*/;

    public native int replaceCssRule(StyleElement stylesheet, String css,
            int ruleindex)
    /*-{
        stylesheet.sheet.deleteRule(ruleindex);
        return stylesheet.sheet.insertRule(css, ruleindex);
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
}
