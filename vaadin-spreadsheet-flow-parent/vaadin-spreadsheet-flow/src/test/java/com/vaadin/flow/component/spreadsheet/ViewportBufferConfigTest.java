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
 * The viewport buffer sizes {@link ViewportBufferConfig} stores and mirrors to
 * the client.
 *
 * <p>
 * Pins the two-axis buffer-size state + element-property mirroring. Both
 * defaults to 200px; setters update the cached field AND push to the client via
 * {@code setProperty(name, int)}.
 *
 * <p>
 * Construction is NOT supposed to push anything to the client (Flow handles
 * initial state separately). This is the same contract `MirroredProperty<T>`
 * has — pinned here as a precondition for any future migration that swaps this
 * helper's two fields for `MirroredProperty` instances.
 */
class ViewportBufferConfigTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;

    private ViewportBufferConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(spreadsheet.getElement()).thenReturn(element);
        config = new ViewportBufferConfig(spreadsheet);
    }

    @Test
    void defaults_areTwoHundredForBothAxes() {
        // Pin: per the field initializers, both default to 200.
        assertEquals(200, config.getRowBufferSize());
        assertEquals(200, config.getColumnBufferSize());
    }

    @Test
    void construction_doesNotEagerlyMirror() {
        // Pin: same contract as MirroredProperty — construction must not
        // call setProperty. Migration to MirroredProperty depends on this.
        verify(element, never()).setProperty(anyString(), anyInt());
    }

    @Test
    void setRowBufferSize_updatesCacheAndPushesToElement() {
        config.setRowBufferSize(300);
        assertEquals(300, config.getRowBufferSize());
        verify(element).setProperty("rowBufferSize", 300);
    }

    @Test
    void setColumnBufferSize_updatesCacheAndPushesToElement() {
        config.setColumnBufferSize(150);
        assertEquals(150, config.getColumnBufferSize());
        verify(element).setProperty("columnBufferSize", 150);
    }

    @Test
    void setRowBufferSize_doesNotAffectColumnBufferSize() {
        // Pin: the two axes are independent.
        config.setRowBufferSize(999);
        assertEquals(200, config.getColumnBufferSize());
        verify(element, never()).setProperty(eq("columnBufferSize"), anyInt());
    }

    @Test
    void setColumnBufferSize_doesNotAffectRowBufferSize() {
        config.setColumnBufferSize(999);
        assertEquals(200, config.getRowBufferSize());
        verify(element, never()).setProperty(eq("rowBufferSize"), anyInt());
    }

    @Test
    void set_pushesAtEveryCall_notDeduplicated() {
        // Pin: setting the same value twice triggers two pushes — there's
        // no equality check. Same contract as MirroredProperty.
        config.setRowBufferSize(200);
        config.setRowBufferSize(200);
        verify(element, org.mockito.Mockito.times(2))
                .setProperty("rowBufferSize", 200);
    }
}
