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
 * Re-runs the visibility check on every registered pop-up button and attaches
 * or detaches it accordingly. Called after a sheet change or scroll, when the
 * set of visible cells may have shifted under the buttons.
 */
final class PopupButtonRefresher implements Serializable {

    private final Spreadsheet spreadsheet;
    private final PopupButtonRegistry registry;
    private final PopupButtonBinding binding;

    PopupButtonRefresher(Spreadsheet spreadsheet, PopupButtonRegistry registry,
            PopupButtonBinding binding) {
        this.spreadsheet = spreadsheet;
        this.registry = registry;
        this.binding = binding;
    }

    void refresh() {
        String activeSheetName = spreadsheet.getActiveSheet().getSheetName();
        for (PopupButton button : registry.values()) {
            if (!activeSheetName
                    .equals(button.getCellReference().getSheetName())) {
                continue;
            }
            int column = button.getColumn() + 1;
            int row = button.getRow() + 1;
            if (spreadsheet.isCellVisible(row, column)) {
                binding.attach(button);
            } else {
                binding.detach(button);
            }
        }
    }
}
