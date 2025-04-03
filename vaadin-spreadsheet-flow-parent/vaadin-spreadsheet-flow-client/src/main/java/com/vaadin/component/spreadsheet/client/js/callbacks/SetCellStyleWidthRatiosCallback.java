/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.component.spreadsheet.client.js.callbacks;

import java.util.HashMap;

@FunctionalInterface
public interface SetCellStyleWidthRatiosCallback {

    void apply(HashMap<Integer, Float> cellStyleWidthRatioMap);

}
