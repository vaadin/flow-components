package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

import com.vaadin.addon.spreadsheet.charts.converter.Utils;

/**
 * This is an adapter for CT*Ser classes to use reflection to call some common
 * methods, as they don't declare implementing a common interface, although
 * share many methods.
 */
public class CTSerAdapter {
    private XmlObject ctSer;

    public CTSerAdapter(XmlObject ctSer) {
        this.ctSer = ctSer;
    }

    public CTSerTx getTx() {
        return Utils.callMethodUsingReflection(ctSer, "getTx");
    }

    public CTAxDataSource getCat() {
        return Utils.callMethodUsingReflection(ctSer, "getCat");
    }

    public CTNumDataSource getVal() {
        return Utils.callMethodUsingReflection(ctSer, "getVal");
    }
}
