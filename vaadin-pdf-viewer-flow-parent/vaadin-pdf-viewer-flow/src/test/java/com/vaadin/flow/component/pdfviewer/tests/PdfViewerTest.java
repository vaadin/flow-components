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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.pdfviewer.DocumentErrorReason;
import com.vaadin.flow.component.pdfviewer.PdfViewer;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentErrorEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.DocumentLoadEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.PageChangeEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewer.ZoomChangeEvent;
import com.vaadin.flow.component.pdfviewer.PdfViewerFeatureFlagProvider;
import com.vaadin.flow.component.pdfviewer.PdfViewerI18n;
import com.vaadin.flow.component.pdfviewer.Zoom;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.shared.JsonConstants;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

class PdfViewerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            PdfViewerFeatureFlagProvider.PDF_VIEWER_COMPONENT);

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
    void setFileName_getFileName() {
        Assertions.assertNull(viewer.getFileName());
        Assertions.assertFalse(viewer.getElement().hasProperty("fileName"));

        viewer.setFileName("report.pdf");
        Assertions.assertEquals("report.pdf",
                viewer.getElement().getProperty("fileName"));
        Assertions.assertEquals("report.pdf", viewer.getFileName());

        viewer.setFileName(null);
        Assertions.assertFalse(viewer.getElement().hasProperty("fileName"));
        Assertions.assertNull(viewer.getFileName());
    }

    @Test
    void setDownloadButtonVisible_isDownloadButtonVisible() {
        Assertions.assertTrue(viewer.isDownloadButtonVisible());
        Assertions.assertFalse(
                viewer.getElement().hasProperty("downloadButtonHidden"));

        viewer.setDownloadButtonVisible(false);
        Assertions.assertTrue(
                viewer.getElement().getProperty("downloadButtonHidden", false));
        Assertions.assertFalse(viewer.isDownloadButtonVisible());

        viewer.setDownloadButtonVisible(true);
        Assertions.assertFalse(
                viewer.getElement().getProperty("downloadButtonHidden", true));
        Assertions.assertTrue(viewer.isDownloadButtonVisible());
    }

    @Test
    void download_downloadCalledOnClient() {
        ui.add(viewer);

        viewer.download();

        Assertions.assertEquals(List.of("download"), dumpCalledFunctionNames());
    }

    @Test
    void download_detached_downloadNotCalledOnAttach() {
        viewer.download();

        ui.add(viewer);

        Assertions.assertEquals(List.of(), dumpCalledFunctionNames());
    }

    @Test
    void defaultPage() {
        Assertions.assertEquals(1, viewer.getPage());
        Assertions.assertEquals(1, viewer.getElement().getProperty("page", 0));
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
    }

    @Test
    void defaultZoom() {
        Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.getZoom());
        Assertions.assertEquals("page-width",
                viewer.getElement().getProperty("zoom"));
    }

    @Test
    void setZoom_getZoom() {
        viewer.setZoom(Zoom.PAGE_FIT);
        Assertions.assertEquals("page-fit",
                viewer.getElement().getProperty("zoom"));
        Assertions.assertEquals(Zoom.PAGE_FIT, viewer.getZoom());

        viewer.setZoom(Zoom.of(1.5));
        Assertions.assertEquals(1.5,
                viewer.getElement().getPropertyRaw("zoom"));
        Assertions.assertEquals(Zoom.of(1.5), viewer.getZoom());

        viewer.setZoom(Zoom.PAGE_WIDTH);
        Assertions.assertEquals("page-width",
                viewer.getElement().getProperty("zoom"));
        Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.getZoom());
    }

    @Test
    void setZoom_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> viewer.setZoom(null));
    }

    @Test
    void setZoom_zoomChangeEventFired() {
        var events = new ArrayList<ZoomChangeEvent>();
        viewer.addZoomChangeListener(events::add);

        viewer.setZoom(Zoom.of(2));

        Assertions.assertEquals(1, events.size());
        Assertions.assertFalse(events.get(0).isFromClient());
        Assertions.assertEquals(Zoom.of(2), events.get(0).getZoom());
    }

    @Test
    void setZoom_sameZoom_noZoomChangeEvent() {
        var events = new ArrayList<ZoomChangeEvent>();
        viewer.addZoomChangeListener(events::add);

        viewer.setZoom(Zoom.PAGE_WIDTH);
        Assertions.assertEquals(0, events.size());

        viewer.setZoom(Zoom.of(2));
        viewer.setZoom(Zoom.of(2));
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void zoomChangedOnClient_zoomChangeEventFired() {
        var events = new ArrayList<ZoomChangeEvent>();
        viewer.addZoomChangeListener(events::add);

        fakeClientPropertyChange("zoom", 2.0);
        fakeClientPropertyChange("zoom", "page-fit");

        Assertions.assertEquals(2, events.size());
        Assertions.assertTrue(events.get(0).isFromClient());
        Assertions.assertEquals(Zoom.of(2), events.get(0).getZoom());
        Assertions.assertTrue(events.get(1).isFromClient());
        Assertions.assertEquals(Zoom.PAGE_FIT, events.get(1).getZoom());
    }

    @Test
    void zoomChangedOnClient_invalidValue_pageWidth() {
        for (Serializable value : new Serializable[] { "invalid", "1.5", "",
                -1.0, 0.0, Double.NaN, true, null }) {
            fakeClientPropertyChange("zoom", 2.0);
            fakeClientPropertyChange("zoom", value);
            Assertions.assertEquals(Zoom.PAGE_WIDTH, viewer.getZoom(),
                    "Zoom for client value " + value);
        }
    }

    @Test
    void zoomChangedOnClient_setSrc_zoomKept() {
        fakeClientPropertyChange("zoom", 2.0);

        viewer.setSrc("report.pdf");

        Assertions.assertEquals(Zoom.of(2), viewer.getZoom());
    }

    @Test
    void zoomSynchronizedOnZoomChangedEvent() {
        Assertions.assertTrue(getSyncExpressions("zoom-changed")
                .contains(JsonConstants.SYNCHRONIZE_PROPERTY_TOKEN + "zoom"));
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
    void documentLoadedOnClient_documentLoadEventFired() {
        var eventRef = new AtomicReference<DocumentLoadEvent>();
        viewer.addDocumentLoadListener(eventRef::set);

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
    void documentErrorOnClient_documentErrorEventFired() {
        var eventRef = new AtomicReference<DocumentErrorEvent>();
        viewer.addDocumentErrorListener(eventRef::set);

        var eventData = JacksonUtils.createObjectNode();
        eventData.put("event.detail.reason", "invalid");
        fakeClientDomEvent("document-error", eventData);

        var event = eventRef.get();
        Assertions.assertNotNull(event);
        Assertions.assertTrue(event.isFromClient());
        Assertions.assertEquals(DocumentErrorReason.INVALID, event.getReason());
    }

    @Test
    void getI18n_null() {
        Assertions.assertNull(viewer.getI18n());
    }

    @Test
    void setI18n_propertySet() {
        var i18n = new PdfViewerI18n().setPreviousPage("previousPage")
                .setNextPage("nextPage").setPageError("pageError")
                .setZoomIn("zoomIn").setZoomOut("zoomOut")
                .setPageWidth("pageWidth").setPageFit("pageFit")
                .setDocument("document").setLink("link").setGoToPage("goToPage")
                .setExternalLink("externalLink").setDownload("download");
        viewer.setI18n(i18n);

        Assertions.assertSame(i18n, viewer.getI18n());
        var json = (ObjectNode) viewer.getElement().getPropertyRaw("i18n");
        var keys = List.of("previousPage", "nextPage", "pageError", "zoomIn",
                "zoomOut", "pageWidth", "pageFit", "document", "link",
                "goToPage", "externalLink", "download");
        Assertions.assertEquals(keys.size(), json.size());
        keys.forEach(
                key -> Assertions.assertEquals(key, json.get(key).asString()));
    }

    @Test
    void setI18n_unsetPropertiesNotSent() {
        viewer.setI18n(new PdfViewerI18n().setPreviousPage("Edellinen sivu"));

        var json = (ObjectNode) viewer.getElement().getPropertyRaw("i18n");
        Assertions.assertEquals(1, json.size());
        Assertions.assertEquals("Edellinen sivu",
                json.get("previousPage").asString());
    }

    @Test
    void setI18n_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> viewer.setI18n(null));
    }

    @Test
    void implementsHasSize() {
        Assertions.assertTrue(HasSize.class.isAssignableFrom(PdfViewer.class));
    }

    private List<String> dumpCalledFunctionNames() {
        return ui.dumpPendingJavaScriptInvocations().stream().filter(
                pending -> pending.getOwner() == viewer.getElement().getNode())
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(JsFunctionCallUtil::getFunctionName)
                .filter(Objects::nonNull).toList();
    }

    private Set<String> getSyncExpressions(String eventName) {
        return viewer.getElement().getNode()
                .getFeature(ElementListenerMap.class).getExpressions(eventName);
    }

    private void fireDocumentLoad(int pageCount) {
        ComponentUtil.fireEvent(viewer,
                new DocumentLoadEvent(viewer, true, pageCount, ""));
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
