/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

/**
 * How many extra pixels of content the client should render outside the visible
 * viewport for smoother scrolling. Two axes, defaulting to 200px each.
 *
 * <p>
 * Every setter mirrors the value into the corresponding element property so the
 * client gets the updated buffer on the next render — without this mirror, the
 * server-side value would silently diverge from the client's.
 */
final class ViewportBufferConfig implements Serializable {

    private final Spreadsheet spreadsheet;

    private int rowBufferSize = 200;
    private int columnBufferSize = 200;

    ViewportBufferConfig(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    int getRowBufferSize() {
        return rowBufferSize;
    }

    void setRowBufferSize(int rowBufferInPixels) {
        this.rowBufferSize = rowBufferInPixels;
        spreadsheet.getElement().setProperty("rowBufferSize",
                rowBufferInPixels);
    }

    int getColumnBufferSize() {
        return columnBufferSize;
    }

    void setColumnBufferSize(int colBufferInPixels) {
        this.columnBufferSize = colBufferInPixels;
        spreadsheet.getElement().setProperty("columnBufferSize",
                colBufferInPixels);
    }
}
