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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.pdfviewer.DocumentErrorReason;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentErrorEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentLoadEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.PageChangeEvent;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.shared.JsonConstants;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

class PdfViewerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final PdfViewer viewer = new PdfViewer();

    @Test
    void srcConstructor_srcSet() {
        Assertions.assertEquals("report.pdf",
                new PdfViewer("report.pdf").getSrc());
    }

    @Test
    void downloadHandlerConstructor_srcSet() {
        var viewer = new PdfViewer(createDownloadHandler());
        Assertions.assertTrue(
                viewer.getSrc().startsWith("VAADIN/dynamic/resource/"));
    }

    @Test
    void setSrc_getSrc() {
        Assertions.assertNull(viewer.getSrc());
        Assertions.assertFalse(viewer.getElement().hasAttribute("src"));

        viewer.setSrc("report.pdf");
        Assertions.assertEquals("report.pdf",
                viewer.getElement().getAttribute("src"));
        Assertions.assertEquals("report.pdf", viewer.getSrc());

        viewer.setSrc((String) null);
        Assertions.assertFalse(viewer.getElement().hasAttribute("src"));
        Assertions.assertNull(viewer.getSrc());
    }

    @Test
    void setSrc_downloadHandler() {
        viewer.setSrc(createDownloadHandler());
        Assertions.assertTrue(
                viewer.getSrc().startsWith("VAADIN/dynamic/resource/"));

        Assertions.assertThrows(NullPointerException.class,
                () -> viewer.setSrc((DownloadHandler) null));
    }

    @Test
    void setSrc_downloadHandler_disabledUpdateModeIsAlways() {
        var element = Mockito.mock(Element.class, Mockito.RETURNS_DEEP_STUBS);
        var viewer = new PdfViewer() {
            @Override
            public Element getElement() {
                return element;
            }
        };

        viewer.setSrc(createDownloadHandler());

        var captor = ArgumentCaptor.forClass(DownloadHandler.class);
        Mockito.verify(element).setAttribute(Mockito.eq("src"),
                captor.capture());
        Assertions.assertEquals(DisabledUpdateMode.ALWAYS,
                captor.getValue().getDisabledUpdateMode());
    }

    @Test
    void defaultPage() {
        Assertions.assertEquals(1, viewer.getPage());
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
    void pageChangedOnClient_setSrc_pageReset() {
        fakeClientPropertyChange("page", 4);

        viewer.setSrc("report.pdf");

        Assertions.assertEquals(1, viewer.getPage());
        Assertions.assertEquals(1, viewer.pageSignal().peek());
    }

    @Test
    void pageChangedOnClient_setSrcDownloadHandler_pageReset() {
        fakeClientPropertyChange("page", 4);

        viewer.setSrc(createDownloadHandler());

        Assertions.assertEquals(1, viewer.getPage());
        Assertions.assertEquals(1, viewer.pageSignal().peek());
    }

    @Test
    void pageChangedOnClient_setSrc_pageChangeEventFired() {
        fakeClientPropertyChange("page", 4);
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        viewer.setSrc("report.pdf");

        Assertions.assertEquals(1, events.size());
        Assertions.assertFalse(events.get(0).isFromClient());
        Assertions.assertEquals(1, events.get(0).getPage());
    }

    @Test
    void setSrc_pageAlreadyFirst_noPageChangeEvent() {
        var events = new ArrayList<PageChangeEvent>();
        viewer.addPageChangeListener(events::add);

        viewer.setSrc("report.pdf");

        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void pageSynchronizedOnPageChangedEvent() {
        Assertions.assertTrue(getSyncExpressions("page-changed")
                .contains(JsonConstants.SYNCHRONIZE_PROPERTY_TOKEN + "page"));
        Assertions.assertEquals(DisabledUpdateMode.ALWAYS,
                viewer.getElement().getNode()
                        .getFeature(ElementListenerMap.class)
                        .getPropertySynchronizationMode("page"));
    }

    @Test
    void documentLoaded_pageCountUpdatedBeforeDocumentLoadListener() {
        var pageCount = new AtomicReference<Integer>();
        viewer.addDocumentLoadListener(
                event -> pageCount.set(viewer.getPageCount()));

        fireDocumentLoad(6);

        Assertions.assertEquals(6, pageCount.get());
    }

    @Test
    void documentLoaded_pageChangedOnClient_documentError_documentStateReset() {
        fireDocumentLoad(6);
        fakeClientPropertyChange("page", 4);

        ComponentUtil.fireEvent(viewer,
                new DocumentErrorEvent(viewer, true, "invalid"));

        Assertions.assertEquals(1, viewer.getPage());
        Assertions.assertEquals(1, viewer.pageSignal().peek());
        Assertions.assertEquals(0, viewer.getPageCount());
    }

    @Test
    void documentLoaded_setSrc_pageCountReset() {
        fireDocumentLoad(6);

        viewer.setSrc("report.pdf");

        Assertions.assertEquals(0, viewer.getPageCount());
    }

    @Test
    void documentLoaded_setSrcDownloadHandler_pageCountReset() {
        fireDocumentLoad(6);

        viewer.setSrc(createDownloadHandler());

        Assertions.assertEquals(0, viewer.getPageCount());
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
    void documentLoadListener_disabledParent_eventFired() {
        var eventRef = new AtomicReference<DocumentLoadEvent>();
        viewer.addDocumentLoadListener(eventRef::set);
        addToDisabledParent();

        var eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.pageCount", 6);
        eventData.put("event.detail.title", "Title");
        fakeClientDomEvent("document-load", eventData);

        var event = eventRef.get();
        Assertions.assertNotNull(event);
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(6, event.getPageCount());
        Assertions.assertEquals("Title", event.getTitle());
    }

    @Test
    void documentErrorListener_disabledParent_eventFired() {
        var eventRef = new AtomicReference<DocumentErrorEvent>();
        viewer.addDocumentErrorListener(eventRef::set);
        addToDisabledParent();

        var eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.reason", "invalid");
        fakeClientDomEvent("document-error", eventData);

        var event = eventRef.get();
        Assertions.assertNotNull(event);
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(DocumentErrorReason.INVALID, event.getReason());
    }

    @Test
    void implementsHasSize() {
        Assertions.assertTrue(HasSize.class.isAssignableFrom(PdfViewer.class));
    }

    @Test
    void implementsFocusable() {
        Assertions
                .assertTrue(Focusable.class.isAssignableFrom(PdfViewer.class));
    }

    private Set<String> getSyncExpressions(String eventName) {
        return viewer.getElement().getNode()
                .getFeature(ElementListenerMap.class).getExpressions(eventName);
    }

    private void fireDocumentLoad(int pageCount) {
        ComponentUtil.fireEvent(viewer,
                new DocumentLoadEvent(viewer, true, pageCount, ""));
    }

    private void addToDisabledParent() {
        var parent = new Element("div");
        parent.appendChild(viewer.getElement());
        parent.setEnabled(false);
        Assertions.assertFalse(viewer.getElement().isEnabled());
    }

    private void fakeClientDomEvent(String eventName, ObjectNode eventData) {
        viewer.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(viewer.getElement(), eventName,
                        eventData));
    }

    private void fakeClientPropertyChange(String property, Serializable value) {
        try {
            viewer.getElement().getNode().getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient(property, value).run();
        } catch (PropertyChangeDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    private DownloadHandler createDownloadHandler() {
        return DownloadHandler
                .fromInputStream(event -> DownloadResponse.error(500));
    }
}
