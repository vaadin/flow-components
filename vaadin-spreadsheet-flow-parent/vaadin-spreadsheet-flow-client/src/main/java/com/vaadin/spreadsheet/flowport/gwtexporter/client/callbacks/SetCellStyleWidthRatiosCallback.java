package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

import java.util.HashMap;

@FunctionalInterface
public interface SetCellStyleWidthRatiosCallback {

    void apply(HashMap<Integer, Float> cellStyleWidthRatioMap);

}
