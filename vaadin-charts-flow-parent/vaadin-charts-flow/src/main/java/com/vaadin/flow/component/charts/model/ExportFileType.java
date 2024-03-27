/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

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
