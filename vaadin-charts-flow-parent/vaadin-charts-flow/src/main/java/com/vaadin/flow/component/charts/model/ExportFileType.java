package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Default MIME type for exporting if chart.exportChart() is called without
 * specifying a type option.
 *
 * Defaults to image/png.
 */
public enum ExportFileType implements ChartEnum {

    IMAGE_PNG("image/png"), IMAGE_JPEG("image/jpeg"), APPLICATION_PDF(
            "application/pdf"), IMAGE_SVG_XML("image/svg+xml");

    private final String type;

    private ExportFileType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
