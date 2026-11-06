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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentLoadEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.ZoomChangeEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewerFeatureFlagProvider;
import com.vaadin.flow.component.pdfviewer.Zoom;
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

    @Test
    void zoomSignal_defaultValue() {
        Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.zoomSignal().peek());
    }

    @Test
    void zoomSignal_setZoom_signalUpdated() {
        viewer.setZoom(Zoom.of(2));
        Assertions.assertEquals(Zoom.of(2), viewer.zoomSignal().peek());
    }

    @Test
    void zoomSignal_zoomChangedOnClient_signalUpdated() {
        fakeClientPropertyChange("zoom", "page-fit");
        Assertions.assertEquals(Zoom.PAGE_FIT, viewer.zoomSignal().peek());
    }

    @Test
    void zoomSignal_isReadOnly() {
        Assertions.assertFalse(viewer.zoomSignal() instanceof ValueSignal<?>);
    }

    @TestFactory
    Stream<DynamicTest> bindZoom() {
        return generateRequiredWriteCallbackBindingTests(PdfViewer::new,
                PdfViewer::bindZoom, PdfViewer::getZoom, PdfViewer::setZoom,
                "zoom", Zoom.PAGE_FIT, Zoom.of(2), 2.0);
    }

    @Test
    void bindZoom_propertyFollowsSignal() {
        var signal = new ValueSignal<>(Zoom.PAGE_FIT);
        UI.getCurrent().add(viewer);
        viewer.bindZoom(signal, signal::set);
        Assertions.assertEquals("page-fit",
                viewer.getElement().getProperty("zoom"));

        signal.set(Zoom.of(1.5));
        Assertions.assertEquals(1.5,
                viewer.getElement().getPropertyRaw("zoom"));

        signal.set(Zoom.PAGE_WIDTH);
        Assertions.assertEquals("page-width",
                viewer.getElement().getProperty("zoom"));
    }

    @Test
    void bindZoom_nullSignalValue_pageWidth() {
        var signal = new ValueSignal<Zoom>(null);
        UI.getCurrent().add(viewer);
        viewer.bindZoom(signal, signal::set);

        Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.getZoom());
        Assertions.assertEquals("page-width",
                viewer.getElement().getProperty("zoom"));
    }

    @Test
    void bindZoom_signalChanged_zoomChangeEventFiredAndZoomSignalUpdated() {
        var signal = new ValueSignal<>(Zoom.PAGE_WIDTH);
        var events = new ArrayList<ZoomChangeEvent>();
        viewer.addZoomChangeListener(events::add);
        UI.getCurrent().add(viewer);
        viewer.bindZoom(signal, signal::set);

        signal.set(Zoom.of(2));

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(Zoom.of(2), events.get(0).getZoom());
        Assertions.assertEquals(Zoom.of(2), viewer.zoomSignal().peek());
    }

    @Test
    void bindZoom_zoomChangedOnClient_mode_writeCallbackUpdatesSignal() {
        var signal = new ValueSignal<>(Zoom.PAGE_WIDTH);
        UI.getCurrent().add(viewer);
        viewer.bindZoom(signal, signal::set);

        fakeClientPropertyChange("zoom", "page-fit");

        Assertions.assertEquals(Zoom.PAGE_FIT, signal.peek());
        Assertions.assertEquals(Zoom.PAGE_FIT, viewer.getZoom());
    }

    @Test
    void bindZoom_zoomChangedOnClient_invalidValue_pageWidth() {
        var signal = new ValueSignal<>(Zoom.of(2));
        UI.getCurrent().add(viewer);
        viewer.bindZoom(signal, signal::set);

        fakeClientPropertyChange("zoom", "invalid");

        Assertions.assertEquals(Zoom.PAGE_WIDTH, signal.peek());
        Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.getZoom());
        Assertions.assertEquals("page-width",
                viewer.getElement().getProperty("zoom"));
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
