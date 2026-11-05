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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableConsumer;
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

    private final ValueSignal<Integer> pageCountSignal = new ValueSignal<>(0);

    /**
     * Creates a PDF viewer without a document.
     */
    public PdfViewer() {
        getElement().addPropertyChangeListener("page", event -> fireEvent(
                new PageChangeEvent(this, event.isUserOriginated())));
        getElement().addPropertyChangeListener("pageCount",
                event -> pageCountSignal.set(getPageCount()));
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
     * and the new one opens on the first page, unless {@link #setPage(int)} is
     * called in the same request. {@link DocumentLoadEvent} fires when the
     * document has loaded, {@link DocumentErrorEvent} if it could not be
     * loaded.
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
    }

    /**
     * Gets the current page, which is the page that takes up most of the
     * visible area. The default value is {@code 1}.
     *
     * @return the current page, starting from {@code 1}
     */
    @Synchronize(property = "page", value = "page-changed")
    public int getPage() {
        return getElement().getProperty("page", 1);
    }

    /**
     * Sets the current page and scrolls the viewer to it. Pages beyond the end
     * of the document are ignored. Can be called before the document has
     * loaded.
     *
     * @param page
     *            the page to show, starting from {@code 1}
     * @throws IllegalArgumentException
     *             if the page is less than {@code 1}
     */
    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException(
                    "The page should be greater than or equal to 1");
        }
        getElement().setProperty("page", page);
    }

    /**
     * Binds the current page to the given signal. The binding is two-way:
     * signal changes scroll the viewer to the page, and page changes by the
     * user or by {@link #setPage(int)} invoke the write callback.
     *
     * @param pageSignal
     *            the signal to bind, not {@code null}
     * @param writeCallback
     *            the callback to propagate page changes back, or {@code null}
     *            for a one-way binding
     * @return a {@link SignalBinding} that can be used to register
     *         {@link SignalBinding#onChange(SerializableConsumer) onChange}
     *         callbacks
     * @throws com.vaadin.flow.signals.BindingActiveException
     *             if a binding is already active
     * @see #setPage(int)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SignalBinding<Integer> bindPage(Signal<Integer> pageSignal,
            SerializableConsumer<Integer> writeCallback) {
        Objects.requireNonNull(pageSignal, "Signal cannot be null");
        SerializableConsumer<Object> callback = writeCallback == null ? null
                : value -> writeCallback.accept(((Number) value).intValue());
        return getElement().bindProperty("page", pageSignal,
                (SerializableConsumer) callback);
    }

    /**
     * Gets the number of pages in the document.
     *
     * @return the number of pages, or {@code 0} if no document is loaded
     */
    @Synchronize(property = "pageCount", value = "page-count-changed")
    public int getPageCount() {
        return getElement().getProperty("pageCount", 0);
    }

    /**
     * Gets a read-only signal with the number of pages in the document, or
     * {@code 0} if no document is loaded.
     *
     * @return a read-only signal with the number of pages
     */
    public Signal<Integer> pageCountSignal() {
        return pageCountSignal.asReadonly();
    }

    /**
     * Adds a listener that is called when the current page changes, either by
     * the user or by {@link #setPage(int)}.
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
     * Event fired when the current page changes.
     */
    public static class PageChangeEvent extends ComponentEvent<PdfViewer> {

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            {@code true} if the event originated from the client
         */
        public PageChangeEvent(PdfViewer source, boolean fromClient) {
            super(source, fromClient);
        }

        /**
         * Gets the current page.
         *
         * @return the current page, starting from {@code 1}
         */
        public int getPage() {
            return getSource().getPage();
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
