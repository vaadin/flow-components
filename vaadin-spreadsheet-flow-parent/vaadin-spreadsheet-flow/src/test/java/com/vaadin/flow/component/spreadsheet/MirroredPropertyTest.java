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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

/**
 * Storing a value and mirroring it to the client with {@link MirroredProperty}.
 *
 * <p>
 * Pins three properties of the abstraction:
 *
 * <ul>
 * <li>Construction does NOT eagerly sync the initial value (matches the
 * pre-abstraction setX-only-mirrors-on-set behavior).</li>
 * <li>{@link MirroredProperty#set} updates the cached value AND pushes via the
 * mirror strategy.</li>
 * <li>The factory methods route through the correct {@link Element} setProperty
 * overload for the primitive / serialized cases.</li>
 * </ul>
 */
class MirroredPropertyTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(spreadsheet.getElement()).thenReturn(element);
    }

    @Test
    void construction_doesNotEagerlySync() {
        // Pin: original setX() pattern only mirrored on explicit set —
        // matching that behavior is critical so migrating fields doesn't
        // shift initial-render timing.
        MirroredProperty.bool(spreadsheet, "chartsEnabled", true);
        verify(element, never()).setProperty(anyString(), any(Boolean.class));
    }

    @Test
    void get_returnsInitialValue_beforeAnySet() {
        MirroredProperty<Boolean> p = MirroredProperty.bool(spreadsheet,
                "chartsEnabled", true);
        assertEquals(Boolean.TRUE, p.get());
    }

    @Test
    void set_updatesValueAndPushesToElement_forBoolean() {
        MirroredProperty<Boolean> p = MirroredProperty.bool(spreadsheet,
                "chartsEnabled", false);
        p.set(true);
        assertEquals(Boolean.TRUE, p.get());
        verify(element).setProperty(eq("chartsEnabled"), eq(true));
    }

    @Test
    void set_updatesValueAndPushesToElement_forInteger() {
        // Pin: integer factory routes through the double overload via
        // widening — Element doesn't have a setProperty(String, int) overload,
        // so we have to call the double one explicitly.
        MirroredProperty<Integer> p = MirroredProperty.integer(spreadsheet,
                "rowBufferSize", 200);
        p.set(150);
        assertEquals(Integer.valueOf(150), p.get());
        verify(element).setProperty(eq("rowBufferSize"), eq(150.0));
    }

    @Test
    void set_updatesValueAndPushesToElement_forString() {
        MirroredProperty<String> p = MirroredProperty.string(spreadsheet,
                "infoLabelValue", "");
        p.set("hello");
        assertEquals("hello", p.get());
        verify(element).setProperty(eq("infoLabelValue"), eq("hello"));
    }

    @Test
    void set_handlesNullValue_forString() {
        // Pin: passing null to setProperty(name, String) clears the property
        // on the client per Flow conventions. The mirror passes the null
        // through unchanged.
        MirroredProperty<String> p = MirroredProperty.string(spreadsheet,
                "infoLabelValue", "initial");
        p.set(null);
        assertNull(p.get());
        verify(element).setProperty(eq("infoLabelValue"), (String) eq(null));
    }

    @Test
    void set_pushesAtEverySetCall_notDeduplicated() {
        // Pin: even setting the same value twice triggers two pushes.
        // Mirrors the original setX behavior — there's no equality check.
        MirroredProperty<Boolean> p = MirroredProperty.bool(spreadsheet,
                "chartsEnabled", false);
        p.set(true);
        p.set(true);
        verify(element, org.mockito.Mockito.times(2))
                .setProperty(eq("chartsEnabled"), eq(true));
    }
}
