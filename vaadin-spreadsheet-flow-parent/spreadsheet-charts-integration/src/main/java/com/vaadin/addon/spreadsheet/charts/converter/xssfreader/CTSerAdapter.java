package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
