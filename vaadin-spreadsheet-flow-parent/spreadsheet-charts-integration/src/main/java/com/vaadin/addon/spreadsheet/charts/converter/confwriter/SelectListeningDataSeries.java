package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

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

import com.vaadin.addon.charts.model.DataSeries;

@SuppressWarnings("serial")
public class SelectListeningDataSeries extends DataSeries {
    public static interface SelectListener {
        void selected();
    }

    public SelectListeningDataSeries(String name, SelectListener selectListener) {
        super(name);
        this.selectListener = selectListener;

    }

    // transient because DataSeries are serialized to JSON and this one doesn't
    // need to be serialized (and serialization fails if it's not transient)
    private transient SelectListener selectListener;

    public SelectListener getSelectListener() {
        return selectListener;
    }
}
