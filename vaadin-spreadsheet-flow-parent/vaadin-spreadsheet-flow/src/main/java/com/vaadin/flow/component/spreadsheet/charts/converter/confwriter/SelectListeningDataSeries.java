/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.charts.converter.confwriter;

import com.vaadin.flow.component.charts.model.DataSeries;

@SuppressWarnings("serial")
public class SelectListeningDataSeries extends DataSeries {
    public static interface SelectListener {
        void selected();
    }

    public SelectListeningDataSeries(String name,
            SelectListener selectListener) {
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
