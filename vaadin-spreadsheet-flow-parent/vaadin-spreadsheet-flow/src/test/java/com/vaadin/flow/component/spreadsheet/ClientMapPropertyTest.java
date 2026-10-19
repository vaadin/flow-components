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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * A map property of the element that the server never holds as {@code null},
 * with {@link ClientMapProperty}: {@code set(null)} empties the map held IN
 * PLACE, for whoever holds it, and still sends JSON's null; a map set replaces
 * the one held; {@link ClientMapProperty#reset()} empties it as
 * {@code set(null)} does.
 */
class ClientMapPropertyTest {

    @Mock
    private Element element;

    private ClientMapProperty<String, String> map;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        map = new ClientMapProperty<>(element, "myProp");
    }

    @Test
    void freshMap_isEmptyButNotNull() {
        // Callers read get() without a null check.
        assertNotNull(map.get());
        assertTrue(map.get().isEmpty());
    }

    @Test
    void declaring_writesNothing() {
        verify(element, never()).setPropertyJson(anyString(),
                any(BaseJsonNode.class));
    }

    @Test
    void setNonNull_replacesTheMapHeld_andGivesItAsJson() {
        HashMap<String, String> input = new HashMap<>();
        input.put("k", "v");

        map.set(input);

        assertSame(input, map.get());
        verify(element).setPropertyJson(eq("myProp"),
                argThat(json -> json != null && json.isObject()
                        && "v".equals(json.get("k").asString())));
    }

    @Test
    void setNull_emptiesTheMapHeldInPlace() {
        HashMap<String, String> initial = new HashMap<>();
        initial.put("k", "v");
        map.set(initial);

        map.set(null);

        assertSame(initial, map.get());
        assertTrue(initial.isEmpty());
    }

    @Test
    void setNull_stillSendsJsonNull() {
        // The server holds an empty map, the client JSON's null: the client
        // treats the two alike.
        map.set(null);

        verify(element).setPropertyJson(eq("myProp"),
                argThat(json -> json != null && json.isNull()));
    }

    @Test
    void repeatedSetNull_isSafe() {
        map.set(null);
        map.set(null);

        verify(element, times(2)).setPropertyJson(eq("myProp"),
                argThat(json -> json != null && json.isNull()));
        assertTrue(map.get().isEmpty());
    }

    @Test
    void setNonNull_doesNotMerge() {
        HashMap<String, String> first = new HashMap<>();
        first.put("a", "1");
        map.set(first);
        HashMap<String, String> second = new HashMap<>();
        second.put("b", "2");

        map.set(second);

        assertSame(second, map.get());
        assertEquals(1, map.get().size());
    }

    @Test
    void reset_emptiesTheMapHeld_andSendsJsonNull() {
        HashMap<String, String> held = new HashMap<>();
        held.put("k", "v");
        map.set(held);

        map.reset();

        assertSame(held, map.get());
        assertTrue(held.isEmpty());
        verify(element).setPropertyJson(eq("myProp"),
                argThat(json -> json != null && json.isNull()));
    }
}
