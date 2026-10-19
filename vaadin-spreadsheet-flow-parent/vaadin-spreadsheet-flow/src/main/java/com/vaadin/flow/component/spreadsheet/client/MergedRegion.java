/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @since 23.1
 */
public class MergedRegion implements Serializable {
    /**
     * A number the server gives each region it lists. Not sent to the client,
     * which identifies a region by its cells: numbered anew on every reload, it
     * made a list of unchanged regions differ from the one sent before, so the
     * list was sent again.
     */
    @JsonIgnore
    public int id;
    public int col1;
    public int col2;
    public int row1;
    public int row2;

    public MergedRegion() {
    }

    public MergedRegion(int c1, int r1, int c2, int r2) {
        col1 = c1;
        row1 = r1;
        col2 = c2;
        row2 = r2;
    }
}
