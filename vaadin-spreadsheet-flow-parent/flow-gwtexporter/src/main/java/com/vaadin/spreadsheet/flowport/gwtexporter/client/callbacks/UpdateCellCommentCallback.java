package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface UpdateCellCommentCallback {

    void apply(String text, int col, int row);

}
