package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */


/**
 * Default MIME type for exporting if chart.exportChart() is called without specifying a type option.
 *
 * Defaults to image/png.
 */
public enum ExportFileType implements ChartEnum {

    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    APPLICATION_PDF("application/pdf"),
    IMAGE_SVG_XML("image/svg+xml");

    private final String type;

    private ExportFileType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
