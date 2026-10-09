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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
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
 * The freeze-pane state {@link FreezePaneState} stores and mirrors to the
 * client.
 *
 * <p>
 * Four mirrored fields: two int split positions (primitives) and two int[]
 * scroll-position arrays (objects that go through {@link Serializer#serialize}
 * before being pushed to the element). The pattern split mirrors the
 * {@link MirroredProperty} {@code integer} vs {@code serialized} factory
 * dispatch.
 */
class FreezePaneStateTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;

    private FreezePaneState state;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(spreadsheet.getElement()).thenReturn(element);
        state = new FreezePaneState(spreadsheet);
    }

    @Test
    void defaults_splitsAreZero_scrollsAreNull() {
        // Pin: int primitives default to 0, int[] references default to null
        // (Java semantics). Captured here so a future refactor that adds a
        // ctor-time element sync would surface as a test failure.
        assertEquals(0, state.getVerticalSplitPosition());
        assertEquals(0, state.getHorizontalSplitPosition());
        assertNull(state.getVerticalScrollPositions());
        assertNull(state.getHorizontalScrollPositions());
    }

    @Test
    void construction_doesNotEagerlyMirror() {
        verify(element, never()).setProperty(anyString(), anyInt());
        verify(element, never()).setProperty(anyString(), anyString());
    }

    @Test
    void setVerticalSplitPosition_updatesAndMirrors() {
        state.setVerticalSplitPosition(5);
        assertEquals(5, state.getVerticalSplitPosition());
        verify(element).setProperty("verticalSplitPosition", 5);
    }

    @Test
    void setHorizontalSplitPosition_updatesAndMirrors() {
        state.setHorizontalSplitPosition(3);
        assertEquals(3, state.getHorizontalSplitPosition());
        verify(element).setProperty("horizontalSplitPosition", 3);
    }

    @Test
    void setVerticalScrollPositions_storesReferenceAndPushesSerialized() {
        // Pin: the getter returns the SAME reference that was set (no defensive
        // copy). Callers can observe later mutations of the int[] — that's
        // the original behavior.
        int[] positions = { 10, 20, 30 };
        state.setVerticalScrollPositions(positions);
        assertSame(positions, state.getVerticalScrollPositions());
        // The push uses Serializer.serialize, producing a JsonValue.
        verify(element).setProperty(eq("verticalScrollPositions"), anyString());
    }

    @Test
    void setHorizontalScrollPositions_storesReferenceAndPushesSerialized() {
        int[] positions = { 100, 200 };
        state.setHorizontalScrollPositions(positions);
        assertSame(positions, state.getHorizontalScrollPositions());
        verify(element).setProperty(eq("horizontalScrollPositions"),
                anyString());
    }

    @Test
    void setVerticalScrollPositions_nullClearsViaSerializedNull() {
        // Pin: null gets serialized and pushed (Serializer.serialize handles
        // null per its contract, typically producing a JSON null). The cache
        // becomes null.
        state.setVerticalScrollPositions(null);
        assertNull(state.getVerticalScrollPositions());
        // Element still gets a setProperty call — null isn't a no-op.
        verify(element).setProperty(eq("verticalScrollPositions"), anyString());
    }

    @Test
    void axesAreIndependent_verticalDoesNotTouchHorizontal() {
        state.setVerticalSplitPosition(7);
        // Vertical pushed, horizontal not.
        verify(element).setProperty("verticalSplitPosition", 7);
        verify(element, never()).setProperty(eq("horizontalSplitPosition"),
                anyInt());
        assertEquals(0, state.getHorizontalSplitPosition());
    }

    @Test
    void scrollPositionAxes_areIndependent() {
        int[] vPositions = { 5, 10 };
        state.setVerticalScrollPositions(vPositions);
        verify(element).setProperty(eq("verticalScrollPositions"), anyString());
        verify(element, never()).setProperty(eq("horizontalScrollPositions"),
                anyString());
        assertNull(state.getHorizontalScrollPositions());
    }
}
