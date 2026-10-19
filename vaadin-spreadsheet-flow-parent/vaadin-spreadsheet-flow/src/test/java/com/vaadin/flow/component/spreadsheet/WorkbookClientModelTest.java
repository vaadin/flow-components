/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.JsonNode;

/**
 * What the client is given of the workbook shown, as
 * {@link WorkbookClientModel} declares it: nothing until a value is set, and
 * each value kept and given to the element.
 */
class WorkbookClientModelTest {

    private final Element element = new Element("vaadin-spreadsheet");
    private final WorkbookClientModel model = new WorkbookClientModel(element);

    /** Every property of the element, as the client would read it. */
    private Map<String, String> onTheElement() {
        Map<String, String> properties = new TreeMap<>();
        element.getPropertyNames().forEach(name -> {
            Object raw = element.getPropertyRaw(name);
            properties.put(name, raw instanceof JsonNode json ? json.toString()
                    : String.valueOf(raw));
        });
        return properties;
    }

    @Test
    void declaring_givesTheElementNothing() {
        assertEquals(Map.of(), onTheElement());
    }

    @Test
    void beforeAnyIsSet_theFirstTabOfAnUnprotectedWorkbook() {
        assertEquals(1, model.sheetIndex.get());
        assertNull(model.sheetNames.get());
        assertFalse(model.workbookProtected.get());
        assertNull(model.cellStyleToCSSStyle.get());
        assertNull(model.verticalScrollPositions.get());
    }

    @Test
    void set_keepsEachValue_andGivesItToTheElement() {
        String[] names = { "Sales", "Costs" };
        HashMap<Integer, String> styles = new HashMap<>(
                Map.of(0, "color:red;"));
        int[] tops = { 0, 120 };

        model.sheetNames.set(names);
        model.sheetIndex.set(2);
        model.cellStyleToCSSStyle.set(styles);
        model.workbookProtected.set(true);
        model.verticalScrollPositions.set(tops);
        model.horizontalScrollPositions.set(new int[] { 30, 0 });

        assertSame(names, model.sheetNames.get());
        assertSame(styles, model.cellStyleToCSSStyle.get());
        assertSame(tops, model.verticalScrollPositions.get());
        assertEquals(
                Map.of("sheetNames", "[\"Sales\",\"Costs\"]", "sheetIndex",
                        "2.0", "cellStyleToCSSStyle", "{\"0\":\"color:red;\"}",
                        "workbookProtected", "true", "verticalScrollPositions",
                        "[0,120]", "horizontalScrollPositions", "[30,0]"),
                onTheElement());
    }
}
