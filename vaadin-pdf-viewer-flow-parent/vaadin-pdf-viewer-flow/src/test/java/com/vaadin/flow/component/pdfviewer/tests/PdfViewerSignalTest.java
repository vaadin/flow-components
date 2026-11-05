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
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.PageChangeEvent;
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

    @TestFactory
    Stream<DynamicTest> bindPage() {
        return generateTwoWayBindingTests(PdfViewer::new, PdfViewer::bindPage,
                PdfViewer::getPage, PdfViewer::setPage, "page", 1, 3);
    }

    @Test
    void bindPage_clientSendsDouble_writeCallbackReceivesInteger() {
        var signal = new ValueSignal<>(1);
        UI.getCurrent().add(viewer);
        viewer.bindPage(signal, signal::set);

        fakeClientPropertyChange("page", 4.0);

        Assertions.assertEquals(4, signal.peek());
        Assertions.assertEquals(4, viewer.getPage());
    }

    @Test
    void bindPage_signalChange_pageChangeEventFired() {
        var signal = new ValueSignal<>(1);
        UI.getCurrent().add(viewer);
        viewer.bindPage(signal, null);
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        signal.set(3);

        Assertions.assertEquals(1, events.size());
        Assertions.assertFalse(events.get(0).isFromClient());
        Assertions.assertEquals(3, events.get(0).getPage());
    }

    @Test
    void pageCountSignal_defaultValue() {
        Assertions.assertEquals(0, viewer.pageCountSignal().peek());
    }

    @Test
    void pageCountSignal_pageCountChangedOnClient_signalUpdated() {
        fakeClientPropertyChange("pageCount", 6.0);
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
