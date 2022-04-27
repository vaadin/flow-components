package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface UpdateCellCommentCallback {

    void apply(String text, int col, int row);

}
