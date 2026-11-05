/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import java.io.Serializable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentLoadEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewerFeatureFlagProvider;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;
import com.vaadin.tests.EnableFeatureFlagExtension;

class PdfViewerSignalTest extends AbstractSignalsTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            PdfViewerFeatureFlagProvider.PDF_VIEWER_COMPONENT);

    private final PdfViewer viewer = new PdfViewer();

    @Test
    void pageSignal_defaultValue() {
        Assertions.assertEquals(1, viewer.pageSignal().peek());
    }

    @Test
    void pageSignal_pageChangedOnClient_signalUpdated() {
        fakeClientPropertyChange("page", 4.0);
        Assertions.assertEquals(4, viewer.pageSignal().peek());
    }

    @Test
    void pageSignal_isReadOnly() {
        Assertions.assertFalse(viewer.pageSignal() instanceof ValueSignal<?>);
    }

    @Test
    void pageCountSignal_defaultValue() {
        Assertions.assertEquals(0, viewer.pageCountSignal().peek());
    }

    @Test
    void pageCountSignal_documentLoaded_signalUpdated() {
        ComponentUtil.fireEvent(viewer,
                new DocumentLoadEvent(viewer, true, 6, ""));
        Assertions.assertEquals(6, viewer.pageCountSignal().peek());
    }

    @Test
    void pageCountSignal_isReadOnly() {
        Assertions.assertFalse(
                viewer.pageCountSignal() instanceof ValueSignal<?>);
    }

    private void fakeClientPropertyChange(String property, Serializable value) {
        try {
            viewer.getElement().getNode().getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient(property, value).run();
        } catch (PropertyChangeDeniedException e) {
            throw new RuntimeException(e);
        }
    }
}
