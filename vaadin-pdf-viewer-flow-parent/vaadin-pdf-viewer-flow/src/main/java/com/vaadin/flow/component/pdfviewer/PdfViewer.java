/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer;

import java.util.Objects;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * PDF Viewer is a component for showing PDF documents. Give the document as a
 * URL with {@link #setSrc(String)}, or serve it from the application with
 * {@link #setSrc(DownloadHandler)}:
 *
 * <pre>
 * PdfViewer viewer = new PdfViewer(
 *         DownloadHandler.forClassResource(getClass(), "report.pdf"));
 * viewer.setHeight("600px");
 * </pre>
 * <p>
 * You can add your own controls to the toolbar with
 * {@link #getToolbarActions()}.
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.pdfViewerComponent} feature flag, either in
 * the experimental features tab of Copilot or in
 * {@code src/main/resources/vaadin-featureflags.properties}:
 *
 * <pre>
 * com.vaadin.experimental.pdfViewerComponent = true
 * </pre>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
@Tag("vaadin-pdf-viewer")
@NpmPackage(value = "@vaadin/pdf-viewer", version = "25.4.0-alpha5")
@JsModule("@vaadin/pdf-viewer/src/vaadin-pdf-viewer.js")
public class PdfViewer extends Component implements HasSize {

    private final ValueSignal<Integer> pageSignal = new ValueSignal<>(1);
    private final ValueSignal<Integer> pageCountSignal = new ValueSignal<>(0);

    private PdfViewerToolbarActions toolbarActions;
    private PdfViewerI18n i18n;

    /**
     * Creates a PDF viewer without a document.
     */
    public PdfViewer() {
        getElement().setProperty("page", 1);
        getElement().setProperty("zoom", Zoom.PAGE_WIDTH.getMode());
        getElement().setProperty("toolbarCollapsed", false);
        getElement().addPropertyChangeListener("page", event -> {
            pageSignal.set(getPage());
            fireEvent(new PageChangeEvent(this, event.isUserOriginated(),
                    getPage()));
        });
        getElement().addPropertyChangeListener("zoom",
                event -> fireEvent(new ZoomChangeEvent(this,
                        event.isUserOriginated(), getZoom())));
        getElement().addPropertyChangeListener("toolbarCollapsed",
                event -> fireEvent(new ToolbarCollapsedChangeEvent(this,
                        event.isUserOriginated(), isToolbarCollapsed())));
        addDocumentLoadListener(
                event -> pageCountSignal.set(event.getPageCount()));
        addDocumentErrorListener(event -> resetDocumentState());
    }

    /**
     * Creates a PDF viewer that shows the document at the given URL.
     *
     * @param src
     *            the URL of the document, or {@code null} to show no document
     * @see #setSrc(String)
     */
    public PdfViewer(String src) {
        this();
        setSrc(src);
    }

    /**
     * Creates a PDF viewer that shows the document served by the given download
     * handler.
     *
     * @param downloadHandler
     *            the download handler that serves the document, not
     *            {@code null}
     * @see #setSrc(DownloadHandler)
     */
    public PdfViewer(DownloadHandler downloadHandler) {
        this();
        setSrc(downloadHandler);
    }

    /**
     * Gets the URL of the document.
     *
     * @return the URL of the document, or {@code null} if no document is set
     */
    public String getSrc() {
        return getElement().getAttribute("src");
    }

    /**
     * Sets the URL of the document to show. The current document is replaced,
     * and the new one opens on the first page. {@link DocumentLoadEvent} fires
     * when the document has loaded, {@link DocumentErrorEvent} if it could not
     * be loaded.
     * <p>
     * The browser fetches the document, so the URL must be on the same origin
     * as the application, or the server must allow it with CORS headers. To
     * serve a document from the application, use
     * {@link #setSrc(DownloadHandler)}.
     *
     * @param src
     *            the URL of the document, or {@code null} to show no document
     */
    public void setSrc(String src) {
        if (src == null) {
            getElement().removeAttribute("src");
        } else {
            getElement().setAttribute("src", src);
        }
        resetDocumentState();
    }

    /**
     * Sets a download handler that serves the document to show, for example one
     * created with the factory methods of {@link DownloadHandler}. The
     * framework generates a URL for the handler, which {@link #getSrc()}
     * returns, and the viewer loads it like a URL set with
     * {@link #setSrc(String)}.
     *
     * @param downloadHandler
     *            the download handler that serves the document, not
     *            {@code null}
     */
    public void setSrc(DownloadHandler downloadHandler) {
        Objects.requireNonNull(downloadHandler,
                "The download handler should not be null");
        getElement().setAttribute("src", downloadHandler);
        resetDocumentState();
    }

    private void resetDocumentState() {
        if (getPage() != 1) {
            getElement().setProperty("page", 1);
        }
        pageCountSignal.set(0);
    }

    /**
     * Gets the current page, which is the page that takes up most of the
     * visible area. The default value is {@code 1}.
     * <p>
     * The value comes from the browser and must not be trusted for security
     * decisions.
     *
     * @return the current page, starting from {@code 1}
     */
    @Synchronize(property = "page", value = "page-changed")
    public int getPage() {
        return getElement().getProperty("page", 1);
    }

    /**
     * Gets a read-only signal with the current page, which is the page that
     * takes up most of the visible area.
     * <p>
     * The value comes from the browser and must not be trusted for security
     * decisions.
     *
     * @return a read-only signal with the current page, starting from {@code 1}
     */
    public Signal<Integer> pageSignal() {
        return pageSignal.asReadonly();
    }

    /**
     * Gets the number of pages in the document.
     * <p>
     * The value comes from the browser and must not be trusted for security
     * decisions.
     *
     * @return the number of pages, or {@code 0} if no document is loaded
     */
    public int getPageCount() {
        return pageCountSignal.peek();
    }

    /**
     * Gets a read-only signal with the number of pages in the document, or
     * {@code 0} if no document is loaded.
     * <p>
     * The value comes from the browser and must not be trusted for security
     * decisions.
     *
     * @return a read-only signal with the number of pages
     */
    public Signal<Integer> pageCountSignal() {
        return pageCountSignal.asReadonly();
    }

    /**
     * Gets whether the viewer shows the header above the toolbar. The default
     * value is {@code true}.
     *
     * @return {@code true} if the header is visible, {@code false} otherwise
     */
    public boolean isHeaderVisible() {
        return !getElement().getProperty("headerHidden", false);
    }

    /**
     * Sets whether the viewer shows the header above the toolbar. The header
     * shows the title, see {@link #setHeaderTitle(String)}, and collapses and
     * expands the toolbar when the user clicks it.
     * <p>
     * While the header is hidden, the toolbar is always shown, also when it is
     * collapsed with {@link #setToolbarCollapsed(boolean)}.
     *
     * @param headerVisible
     *            {@code true} to show the header, {@code false} to hide it
     */
    public void setHeaderVisible(boolean headerVisible) {
        getElement().setProperty("headerHidden", !headerVisible);
    }

    /**
     * Gets the title shown in the header.
     *
     * @return the title, or {@code null} if the header shows the file name of
     *         the document
     */
    public String getHeaderTitle() {
        return getElement().getProperty("headerTitle");
    }

    /**
     * Sets the title shown in the header. By default, no title is set, and the
     * header shows the file name of the document, or the text from
     * {@link PdfViewerI18n#setDocument(String)} if the document has no file
     * name.
     *
     * @param headerTitle
     *            the title, or {@code null} to show the file name of the
     *            document
     * @see #setHeaderVisible(boolean)
     */
    public void setHeaderTitle(String headerTitle) {
        if (headerTitle == null) {
            getElement().removeProperty("headerTitle");
        } else {
            getElement().setProperty("headerTitle", headerTitle);
        }
    }

    /**
     * Gets whether the toolbar is collapsed. The default value is
     * {@code false}.
     *
     * @return {@code true} if the toolbar is collapsed, {@code false} otherwise
     */
    @Synchronize(property = "toolbarCollapsed", value = "toolbar-collapsed-changed")
    public boolean isToolbarCollapsed() {
        return getElement().getProperty("toolbarCollapsed", false);
    }

    /**
     * Sets whether the toolbar is collapsed. A collapsed toolbar is hidden, and
     * the user can expand it again by clicking the header.
     * <p>
     * The toolbar collapses only while the header is visible, see
     * {@link #setHeaderVisible(boolean)}.
     *
     * @param toolbarCollapsed
     *            {@code true} to collapse the toolbar, {@code false} to expand
     *            it
     */
    public void setToolbarCollapsed(boolean toolbarCollapsed) {
        getElement().setProperty("toolbarCollapsed", toolbarCollapsed);
    }

    /**
     * Gets the container for your own controls, such as buttons, in the
     * toolbar. The controls are placed after the built-in page and zoom
     * controls, before the download button. Empty by default.
     *
     * @return the toolbar actions container
     */
    public PdfViewerToolbarActions getToolbarActions() {
        if (toolbarActions == null) {
            toolbarActions = new PdfViewerToolbarActions();
            getElement().appendChild(toolbarActions.getElement());
        }
        return toolbarActions;
    }

    /**
     * Gets the i18n object.
     *
     * @return the i18n object, or {@code null} if not set
     */
    public PdfViewerI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the i18n object that translates the texts of the viewer. Changes to
     * the object take effect only when you set it again.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(PdfViewerI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n object should not be null");
        getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
    }

    /**
     * Gets the zoom level of the pages. The default value is
     * {@link Zoom#PAGE_WIDTH}.
     *
     * @return the zoom level, not {@code null}
     */
    @Synchronize(property = "zoom", value = "zoom-changed")
    public Zoom getZoom() {
        return Zoom.fromClientValue(getElement().getPropertyRaw("zoom"));
    }

    /**
     * Sets the zoom level of the pages:
     * <ul>
     * <li>{@link Zoom#PAGE_WIDTH} fits the width of the current page to the
     * viewer</li>
     * <li>{@link Zoom#PAGE_FIT} fits the whole current page into the
     * viewer</li>
     * <li>{@link Zoom#of(double)} scales the pages relative to their actual
     * size</li>
     * </ul>
     * The fit modes update the scale when the viewer resizes. Scrolling to a
     * page of another size does not change the scale.
     * <p>
     * The zoom stays when another document is set.
     *
     * @param zoom
     *            the zoom level, not {@code null}
     */
    public void setZoom(Zoom zoom) {
        Objects.requireNonNull(zoom, "The zoom should not be null");
        String mode = zoom.getMode();
        if (mode != null) {
            getElement().setProperty("zoom", mode);
        } else {
            getElement().setProperty("zoom", zoom.getFactor());
        }
    }

    /**
     * Adds a listener that is called when the current page changes.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addPageChangeListener(
            ComponentEventListener<PageChangeEvent> listener) {
        return addListener(PageChangeEvent.class, listener);
    }

    /**
     * Adds a listener that is called when the zoom level changes.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addZoomChangeListener(
            ComponentEventListener<ZoomChangeEvent> listener) {
        return addListener(ZoomChangeEvent.class, listener);
    }

    /**
     * Adds a listener that is called when the toolbar is collapsed or expanded,
     * either by the user clicking the header or with
     * {@link #setToolbarCollapsed(boolean)}.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addToolbarCollapsedChangeListener(
            ComponentEventListener<ToolbarCollapsedChangeEvent> listener) {
        return addListener(ToolbarCollapsedChangeEvent.class, listener);
    }

    /**
     * Adds a listener that is called when a document has been loaded.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addDocumentLoadListener(
            ComponentEventListener<DocumentLoadEvent> listener) {
        return addListener(DocumentLoadEvent.class, listener);
    }

    /**
     * Adds a listener that is called when a document could not be loaded.
     *
     * @param listener
     *            the listener to add, not {@code null}
     * @return a registration for removing the listener
     */
    public Registration addDocumentErrorListener(
            ComponentEventListener<DocumentErrorEvent> listener) {
        return addListener(DocumentErrorEvent.class, listener);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        if (!featureFlags
                .isEnabled(PdfViewerFeatureFlagProvider.PDF_VIEWER_COMPONENT)) {
            throw new PdfViewerExperimentalFeatureException();
        }
    }

    /**
     * Container for your own controls in the {@link PdfViewer} toolbar.
     *
     * @see PdfViewer#getToolbarActions()
     */
    public static final class PdfViewerToolbarActions implements HasComponents {
        private final Element root;

        private PdfViewerToolbarActions() {
            root = new Element("div");
            root.setAttribute("slot", "toolbar-actions");
            root.getStyle().set("display", "contents");
        }

        @Override
        public Element getElement() {
            return root;
        }
    }

    /**
     * Event fired when the current page changes.
     */
    public static class PageChangeEvent extends ComponentEvent<PdfViewer> {
        private final int page;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         * @param page
         *            the new current page, starting from {@code 1}
         */
        public PageChangeEvent(PdfViewer source, boolean fromClient, int page) {
            super(source, fromClient);
            this.page = page;
        }

        /**
         * Gets the new current page.
         *
         * @return the new current page, starting from {@code 1}
         */
        public int getPage() {
            return page;
        }
    }

    /**
     * Event fired when the zoom level changes.
     */
    public static class ZoomChangeEvent extends ComponentEvent<PdfViewer> {
        private final Zoom zoom;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         * @param zoom
         *            the new zoom level
         */
        public ZoomChangeEvent(PdfViewer source, boolean fromClient,
                Zoom zoom) {
            super(source, fromClient);
            this.zoom = zoom;
        }

        /**
         * Gets the new zoom level.
         *
         * @return the new zoom level
         */
        public Zoom getZoom() {
            return zoom;
        }
    }

    /**
     * Event fired when the toolbar is collapsed or expanded.
     */
    public static class ToolbarCollapsedChangeEvent
            extends ComponentEvent<PdfViewer> {
        private final boolean toolbarCollapsed;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         * @param toolbarCollapsed
         *            {@code true} if the toolbar is now collapsed,
         *            {@code false} if it is now expanded
         */
        public ToolbarCollapsedChangeEvent(PdfViewer source, boolean fromClient,
                boolean toolbarCollapsed) {
            super(source, fromClient);
            this.toolbarCollapsed = toolbarCollapsed;
        }

        /**
         * Gets whether the toolbar is now collapsed.
         *
         * @return {@code true} if the toolbar is collapsed, {@code false} if it
         *         is expanded
         */
        public boolean isToolbarCollapsed() {
            return toolbarCollapsed;
        }
    }

    /**
     * Event fired when a document has been loaded, before its pages are shown.
     */
    @DomEvent("document-load")
    public static class DocumentLoadEvent extends ComponentEvent<PdfViewer> {
        private final int pageCount;
        private final String title;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         * @param pageCount
         *            the number of pages in the document
         * @param title
         *            the title of the document, or an empty string if it has
         *            none
         */
        public DocumentLoadEvent(PdfViewer source, boolean fromClient,
                @EventData("event.detail.pageCount") int pageCount,
                @EventData("event.detail.title") String title) {
            super(source, fromClient);
            this.pageCount = pageCount;
            this.title = title;
        }

        /**
         * Gets the number of pages in the document.
         *
         * @return the number of pages
         */
        public int getPageCount() {
            return pageCount;
        }

        /**
         * Gets the title of the document from its metadata.
         *
         * @return the title, or an empty string if the document has no title
         */
        public String getTitle() {
            return title;
        }
    }

    /**
     * Event fired when a document could not be loaded.
     */
    @DomEvent("document-error")
    public static class DocumentErrorEvent extends ComponentEvent<PdfViewer> {
        private final DocumentErrorReason reason;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         * @param reason
         *            the reason reported by the client, see
         *            {@link DocumentErrorReason}
         */
        public DocumentErrorEvent(PdfViewer source, boolean fromClient,
                @EventData("event.detail.reason") String reason) {
            super(source, fromClient);
            this.reason = DocumentErrorReason.fromClientValue(reason);
        }

        /**
         * Gets the reason why the document could not be loaded.
         *
         * @return the reason, or {@code null} if this version does not know the
         *         reported reason
         */
        public DocumentErrorReason getReason() {
            return reason;
        }
    }
}
