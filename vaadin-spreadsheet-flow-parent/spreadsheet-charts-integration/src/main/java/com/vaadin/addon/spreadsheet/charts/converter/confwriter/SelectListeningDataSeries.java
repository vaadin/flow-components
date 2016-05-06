package com.vaadin.addon.spreadsheet.charts.converter.confwriter;

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
