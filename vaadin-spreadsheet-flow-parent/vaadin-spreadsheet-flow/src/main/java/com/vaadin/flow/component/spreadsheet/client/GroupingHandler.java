package com.vaadin.flow.component.spreadsheet.client;

public interface GroupingHandler {
    void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed);

    void levelHeaderClicked(boolean cols, int level);
}