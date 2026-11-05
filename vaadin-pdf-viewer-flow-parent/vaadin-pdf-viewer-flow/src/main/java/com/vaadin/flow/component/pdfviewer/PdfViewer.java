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
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
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
 * The toolbar above the pages has built-in controls for moving between pages.
 * You can add your own controls, such as a button, at the end of the toolbar
 * with {@link #getToolbarActions()}.
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

    private ToolbarActions toolbarActions;

    /**
     * Creates a PDF viewer without a document.
     */
    public PdfViewer() {
        getElement().addPropertyChangeListener("page", event -> {
            pageSignal.set(getPage());
            fireEvent(new PageChangeEvent(this, event.isUserOriginated(),
                    getPage()));
        });
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
        getElement().setAttribute("src", downloadHandler.allowDisabled());
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
    @Synchronize(property = "page", value = "page-changed", allowUpdates = DisabledUpdateMode.ALWAYS)
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
     * Gets the area at the end of the toolbar where you can add your own
     * controls, such as a button. It is empty by default.
     *
     * <pre>
     * viewer.getToolbarActions().add(button);
     * </pre>
     *
     * @return the toolbar actions
     */
    public ToolbarActions getToolbarActions() {
        if (toolbarActions == null) {
            toolbarActions = new ToolbarActions();
            getElement().appendChild(toolbarActions.getElement());
        }
        return toolbarActions;
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
     * The area at the end of the toolbar of a {@link PdfViewer} that holds your
     * own controls, such as a button.
     *
     * @see PdfViewer#getToolbarActions()
     */
    public static final class ToolbarActions implements HasComponents {
        private final Element root;

        private ToolbarActions() {
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
     * Event fired when a document has been loaded, before its pages are shown.
     */
    @DomEvent(value = "document-load", allowUpdates = DisabledUpdateMode.ALWAYS)
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
    @DomEvent(value = "document-error", allowUpdates = DisabledUpdateMode.ALWAYS)
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
