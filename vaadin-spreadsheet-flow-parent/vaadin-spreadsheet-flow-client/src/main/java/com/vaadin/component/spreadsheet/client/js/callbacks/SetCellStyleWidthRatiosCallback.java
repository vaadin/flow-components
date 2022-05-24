package com.vaadin.component.spreadsheet.client.js.callbacks;

import java.util.HashMap;

@FunctionalInterface
public interface SetCellStyleWidthRatiosCallback {

    void apply(HashMap<Integer, Float> cellStyleWidthRatioMap);

}
