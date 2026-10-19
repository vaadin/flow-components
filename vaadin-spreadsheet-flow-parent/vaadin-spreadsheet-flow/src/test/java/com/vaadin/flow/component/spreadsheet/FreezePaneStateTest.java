/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * The scroll positions {@link FreezePaneState} stores and mirrors to the
 * client: two int[] arrays, which go through {@link Serializer#toJson} before
 * being pushed to the element.
 */
class FreezePaneStateTest {

    @Mock
    private Element element;

    private FreezePaneState state;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        state = new FreezePaneState(element);
    }

    @Test
    void defaults_scrollsAreNull() {
        assertNull(state.getVerticalScrollPositions());
        assertNull(state.getHorizontalScrollPositions());
    }

    @Test
    void construction_doesNotEagerlyMirror() {
        verifyNoInteractions(element);
    }

    @Test
    void setVerticalScrollPositions_storesReferenceAndPushesJson() {
        // Pin: the getter returns the SAME reference that was set (no defensive
        // copy). Callers can observe later mutations of the int[] — that's
        // the original behavior.
        int[] positions = { 10, 20, 30 };
        state.setVerticalScrollPositions(positions);
        assertSame(positions, state.getVerticalScrollPositions());
        // Pushed as JSON.
        verify(element).setPropertyJson(eq("verticalScrollPositions"),
                argThat(json -> "[10,20,30]".equals(json.toString())));
    }

    @Test
    void setHorizontalScrollPositions_storesReferenceAndPushesJson() {
        int[] positions = { 100, 200 };
        state.setHorizontalScrollPositions(positions);
        assertSame(positions, state.getHorizontalScrollPositions());
        verify(element).setPropertyJson(eq("horizontalScrollPositions"),
                any(BaseJsonNode.class));
    }

    @Test
    void setVerticalScrollPositions_nullClearsViaSerializedNull() {
        // Pin: null is pushed as JSON's null. The cache becomes null.
        state.setVerticalScrollPositions(null);
        assertNull(state.getVerticalScrollPositions());
        // Element still gets a call — null isn't a no-op.
        verify(element).setPropertyJson(eq("verticalScrollPositions"),
                argThat(BaseJsonNode::isNull));
    }

    @Test
    void scrollPositionAxes_areIndependent() {
        int[] vPositions = { 5, 10 };
        state.setVerticalScrollPositions(vPositions);
        verify(element).setPropertyJson(eq("verticalScrollPositions"),
                any(BaseJsonNode.class));
        verify(element, never()).setPropertyJson(
                eq("horizontalScrollPositions"), any(BaseJsonNode.class));
        assertNull(state.getHorizontalScrollPositions());
    }
}
