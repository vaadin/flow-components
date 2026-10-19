/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * A property of the element as the server sets it for the client, with
 * {@link ClientProperty}: declared without writing anything, kept and given to
 * the element on every set, through the overload of {@link Element} its type
 * takes, and set back to the value the client starts from by
 * {@link ClientProperty#reset()}.
 */
class ClientPropertyTest {

    @Mock
    private Element element;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void declaring_writesNothing() {
        // Declared with the value the client starts from: writing it would
        // shift what the first response sends.
        ClientProperty.bool(element, "sheetProtected", true);
        ClientProperty.integer(element, "rows", 3);
        ClientProperty.json(element, "mergedRegions");

        verify(element, never()).setProperty(anyString(), any(Boolean.class));
        verify(element, never()).setProperty(anyString(), anyDouble());
        verify(element, never()).setPropertyJson(anyString(),
                any(BaseJsonNode.class));
    }

    @Test
    void get_isTheInitialValue_beforeAnySet() {
        assertEquals(Boolean.TRUE,
                ClientProperty.bool(element, "sheetProtected", true).get());
        assertEquals(Integer.valueOf(200),
                ClientProperty.integer(element, "rowBufferSize", 200).get());
        assertNull(ClientProperty.json(element, "mergedRegions").get());
    }

    @Test
    void set_keepsABoolean_andGivesItToTheElement() {
        ClientProperty<Boolean> p = ClientProperty.bool(element,
                "sheetProtected", false);

        p.set(true);

        assertEquals(Boolean.TRUE, p.get());
        verify(element).setProperty(eq("sheetProtected"), eq(true));
    }

    @Test
    void set_givesAWholeNumberAsANumber() {
        // An element property has no whole-number overload: it goes as a
        // double.
        ClientProperty<Integer> p = ClientProperty.integer(element,
                "rowBufferSize", 200);

        p.set(150);

        assertEquals(Integer.valueOf(150), p.get());
        verify(element).setProperty(eq("rowBufferSize"), eq(150.0));
    }

    @Test
    void set_givesANumberWithAFractionAsANumber() {
        ClientProperty<Float> p = ClientProperty.decimal(element, "defRowH", 0);

        p.set(15.75f);

        assertEquals(15.75f, p.get());
        verify(element).setProperty(eq("defRowH"), eq(15.75));
    }

    @Test
    void set_givesATextAsItIs_andNullTakesItOff() {
        ClientProperty<String> p = ClientProperty.string(element,
                "infoLabelValue", "initial");

        p.set("hello");
        p.set(null);

        assertNull(p.get());
        verify(element).setProperty(eq("infoLabelValue"), eq("hello"));
        verify(element).setProperty(eq("infoLabelValue"), (String) eq(null));
    }

    @Test
    void set_givesAnObjectAsJson_andNullAsJsonNull() {
        ClientProperty<List<String>> p = ClientProperty.json(element,
                "namedRanges");

        p.set(List.of("Total"));
        p.set(null);

        verify(element).setPropertyJson(eq("namedRanges"), argThat(
                json -> json != null && "[\"Total\"]".equals(json.toString())));
        verify(element).setPropertyJson(eq("namedRanges"),
                argThat(json -> json != null && json.isNull()));
    }

    @Test
    void set_givesAnObjectInTheShapeTheWireTakes_keepingItAsItIs() {
        ClientProperty<float[]> p = ClientProperty.json(element, "rowH",
                SheetSizes::runs);
        float[] heights = { 15, 15, 20 };

        p.set(heights);

        assertArrayEquals(heights, p.get());
        verify(element).setPropertyJson(eq("rowH"), argThat(json -> json != null
                && "{\"runs\":[15.0,2,20.0,1]}".equals(json.toString())));
    }

    @Test
    void set_givesEverySet_evenOfTheSameValue() {
        // Flow sends the client the last value of a round trip, and nothing
        // again for a value it already has: no check of its own is needed.
        ClientProperty<Boolean> p = ClientProperty.bool(element,
                "sheetProtected", false);

        p.set(true);
        p.set(true);

        verify(element, times(2)).setProperty(eq("sheetProtected"), eq(true));
    }

    @Test
    void reset_setsBackTheValueTheClientStartsFrom() {
        ClientProperty<Integer> rows = ClientProperty.integer(element, "rows",
                0);
        ClientProperty<Boolean> gridlines = ClientProperty.bool(element,
                "displayGridlines", true);
        ClientProperty<List<String>> names = ClientProperty.json(element,
                "namedRanges");
        rows.set(40);
        gridlines.set(false);
        names.set(List.of("Total"));

        rows.reset();
        gridlines.reset();
        names.reset();

        assertEquals(Integer.valueOf(0), rows.get());
        assertEquals(Boolean.TRUE, gridlines.get());
        assertNull(names.get());
        verify(element).setProperty(eq("rows"), eq(0.0));
        verify(element).setProperty(eq("displayGridlines"), eq(true));
        verify(element).setPropertyJson(eq("namedRanges"),
                argThat(json -> json != null && json.isNull()));
    }
}
