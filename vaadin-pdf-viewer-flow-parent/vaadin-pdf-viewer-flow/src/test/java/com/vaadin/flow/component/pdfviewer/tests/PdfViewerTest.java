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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.pdfviewer.DocumentErrorReason;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentErrorEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentLoadEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.PageChangeEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import com.vaadin.flow.shared.JsonConstants;
import com.vaadin.flow.shared.Registration;
import com.vaadin.tests.MockUIExtension;

class PdfViewerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final PdfViewer viewer = new PdfViewer();

    @Test
    void defaultConstructor_noSrc() {
        Assertions.assertNull(viewer.getSrc());
        Assertions.assertFalse(viewer.getElement().hasAttribute("src"));
    }

    @Test
    void srcConstructor_srcSet() {
        Assertions.assertEquals("report.pdf",
                new PdfViewer("report.pdf").getSrc());
    }

    @Test
    void downloadHandlerConstructor_srcSet() {
        var handler = createDownloadHandler();
        var viewer = new PdfViewer(handler);
        Assertions.assertTrue(
                viewer.getSrc().startsWith("VAADIN/dynamic/resource/"));
        Assertions.assertTrue(handler.isInline());
    }

    @Test
    void setSrc_attributeSet() {
        viewer.setSrc("report.pdf");
        Assertions.assertEquals("report.pdf",
                viewer.getElement().getAttribute("src"));
        Assertions.assertEquals("report.pdf", viewer.getSrc());
    }

    @Test
    void setSrc_null_attributeRemoved() {
        viewer.setSrc("report.pdf");
        viewer.setSrc((String) null);
        Assertions.assertFalse(viewer.getElement().hasAttribute("src"));
        Assertions.assertNull(viewer.getSrc());
    }

    @Test
    void setSrc_downloadHandler_attributeSet() {
        viewer.setSrc(createDownloadHandler());
        Assertions.assertTrue(
                viewer.getSrc().startsWith("VAADIN/dynamic/resource/"));
    }

    @Test
    void setSrc_abstractDownloadHandler_servedInline() {
        var handler = createDownloadHandler();
        Assertions.assertFalse(handler.isInline());
        viewer.setSrc(handler);
        Assertions.assertTrue(handler.isInline());
    }

    @Test
    void setSrc_nullDownloadHandler_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> viewer.setSrc((DownloadHandler) null));
    }

    @Test
    void defaultPage() {
        Assertions.assertEquals(1, viewer.getPage());
    }

    @Test
    void setPage_propertySet() {
        viewer.setPage(3);
        Assertions.assertEquals(3, viewer.getElement().getProperty("page", 0));
        Assertions.assertEquals(3, viewer.getPage());
    }

    @Test
    void setPage_lessThanOne_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> viewer.setPage(0));
    }

    @Test
    void setPage_pageChangeEventFired() {
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        viewer.setPage(3);

        Assertions.assertEquals(1, events.size());
        Assertions.assertFalse(events.get(0).isFromClient());
        Assertions.assertEquals(3, events.get(0).getPage());
    }

    @Test
    void setSamePage_pageChangeEventNotFired() {
        viewer.setPage(3);
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        viewer.setPage(3);

        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void pageChangedOnClient_pageChangeEventFired() {
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        fakeClientPropertyChange("page", 4);

        Assertions.assertEquals(1, events.size());
        Assertions.assertTrue(events.get(0).isFromClient());
        Assertions.assertEquals(4, events.get(0).getPage());
    }

    @Test
    void pageSynchronizedOnPageChangedEvent() {
        Assertions.assertTrue(getSyncExpressions("page-changed")
                .contains(JsonConstants.SYNCHRONIZE_PROPERTY_TOKEN + "page"));
    }

    @Test
    void defaultPageCount() {
        Assertions.assertEquals(0, viewer.getPageCount());
    }

    @Test
    void pageCountChangedOnClient_pageCountUpdated() {
        fakeClientPropertyChange("pageCount", 6);
        Assertions.assertEquals(6, viewer.getPageCount());
    }

    @Test
    void pageCountSynchronizedOnPageCountChangedEvent() {
        Assertions.assertTrue(getSyncExpressions("page-count-changed").contains(
                JsonConstants.SYNCHRONIZE_PROPERTY_TOKEN + "pageCount"));
    }

    @Test
    void documentLoadListener_eventFired() {
        var eventRef = new AtomicReference<DocumentLoadEvent>();
        viewer.addDocumentLoadListener(eventRef::set);

        ComponentUtil.fireEvent(viewer,
                new DocumentLoadEvent(viewer, true, 6, "Title"));

        var event = eventRef.get();
        Assertions.assertNotNull(event);
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(6, event.getPageCount());
        Assertions.assertEquals("Title", event.getTitle());
    }

    @Test
    void documentLoadListener_registrationRemoved_eventNotFired() {
        var eventRef = new AtomicReference<DocumentLoadEvent>();
        Registration registration = viewer
                .addDocumentLoadListener(eventRef::set);
        registration.remove();

        ComponentUtil.fireEvent(viewer,
                new DocumentLoadEvent(viewer, true, 6, "Title"));

        Assertions.assertNull(eventRef.get());
    }

    @Test
    void documentErrorListener_eventFired() {
        var eventRef = new AtomicReference<DocumentErrorEvent>();
        viewer.addDocumentErrorListener(eventRef::set);

        ComponentUtil.fireEvent(viewer,
                new DocumentErrorEvent(viewer, true, "invalid"));

        var event = eventRef.get();
        Assertions.assertNotNull(event);
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(DocumentErrorReason.INVALID, event.getReason());
    }

    @Test
    void documentErrorEvent_reasonMappedFromClientValue() {
        Map.of("network", DocumentErrorReason.NETWORK, "invalid",
                DocumentErrorReason.INVALID, "password",
                DocumentErrorReason.PASSWORD)
                .forEach((clientValue, reason) -> Assertions.assertEquals(
                        reason,
                        new DocumentErrorEvent(viewer, true, clientValue)
                                .getReason()));
    }

    @Test
    void documentErrorEvent_unknownClientValue_nullReason() {
        Assertions.assertNull(
                new DocumentErrorEvent(viewer, true, "unknown").getReason());
        Assertions.assertNull(
                new DocumentErrorEvent(viewer, true, null).getReason());
    }

    @Test
    void implementsHasSize() {
        Assertions.assertTrue(HasSize.class.isAssignableFrom(PdfViewer.class));
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(
                HasAriaLabel.class.isAssignableFrom(PdfViewer.class));
    }

    private Set<String> getSyncExpressions(String eventName) {
        return viewer.getElement().getNode()
                .getFeature(ElementListenerMap.class).getExpressions(eventName);
    }

    private void fakeClientPropertyChange(String property, Serializable value) {
        try {
            viewer.getElement().getNode().getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient(property, value).run();
        } catch (PropertyChangeDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStreamDownloadHandler createDownloadHandler() {
        return DownloadHandler
                .fromInputStream(event -> DownloadResponse.error(500));
    }
}
