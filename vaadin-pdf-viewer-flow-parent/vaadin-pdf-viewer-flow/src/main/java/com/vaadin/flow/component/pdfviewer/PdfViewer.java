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
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * PDF Viewer is a component for showing PDF documents. The document is given as
 * a URL with {@link #setSrc(String)}, or served by the application with
 * {@link #setSrc(DownloadHandler)}:
 *
 * <pre>
 * PdfViewer viewer = new PdfViewer(
 *         DownloadHandler.forClassResource(getClass(), "report.pdf"));
 * viewer.setHeight("600px");
 * </pre>
 *
 * The viewer has a default height of 400px. Set its height to fit your layout.
 * <p>
 * The toolbar above the pages has built-in controls for moving between pages.
 * You can add your own controls, such as a button, at the end of the toolbar
 * with {@link #getToolbarActions()}.
 * <p>
 * The document is rendered in the browser with pdf.js, which requires Safari 18
 * or newer. Other browsers are supported as for all Vaadin components.
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.pdfViewerComponent} feature flag. The
 * component can be enabled using Copilot, in the experimental features tab, or
 * by adding a {@code src/main/resources/vaadin-featureflags.properties} file
 * with the following content:
 *
 * <pre>
 * com.vaadin.experimental.pdfViewerComponent = true
 * </pre>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
@Tag("vaadin-pdf-viewer")
@NpmPackage(value = "@vaadin/pdf-viewer", version = "25.4.0-alpha4")
@JsModule("@vaadin/pdf-viewer/src/vaadin-pdf-viewer.js")
public class PdfViewer extends Component implements HasAriaLabel, HasSize {

    private PdfViewerI18n i18n;

    private ToolbarActions toolbarActions;

    /**
     * Creates an empty PDF viewer that shows no document.
     */
    public PdfViewer() {
        getElement().addPropertyChangeListener("page", event -> fireEvent(
                new PageChangeEvent(this, event.isUserOriginated())));
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
     * Creates a PDF viewer that shows the document provided by the given
     * download handler.
     *
     * @param downloadHandler
     *            the download handler that provides the document, not
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
     * Sets the URL of the PDF document to show. Setting a URL unloads the
     * current document and loads the new one. The viewer fires
     * {@link DocumentLoadEvent} once the document has loaded, or
     * {@link DocumentErrorEvent} if it could not be loaded. The new document
     * opens on the first page, unless you also call {@link #setPage(int)} in
     * the same request.
     * <p>
     * The document is fetched by the browser, so it must be served from the
     * same origin as the application, or with CORS headers that allow the
     * application to read it. To serve a document from the application, use
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
     * Sets a download handler that provides the PDF document to show, for
     * example one created with the factory methods of {@link DownloadHandler}.
     * The document is served through a URL generated by the framework, which
     * {@link #getSrc()} returns.
     * <p>
     * Handlers that extend {@link AbstractDownloadHandler} are set to serve the
     * document inline instead of as an attachment.
     *
     * @param downloadHandler
     *            the download handler that provides the document, not
     *            {@code null}
     */
    public void setSrc(DownloadHandler downloadHandler) {
        Objects.requireNonNull(downloadHandler,
                "The download handler should not be null");
        if (downloadHandler instanceof AbstractDownloadHandler<?> handler) {
            handler.inline();
        }
        getElement().setAttribute("src", downloadHandler.allowDisabled());
    }

    /**
     * Gets the current page. The current page changes when the user scrolls or
     * navigates between pages, and is the page that takes up most of the
     * visible area. The default value is {@code 1}.
     *
     * @return the current page, starting from {@code 1}
     */
    @Synchronize(property = "page", value = "page-changed")
    public int getPage() {
        return getElement().getProperty("page", 1);
    }

    /**
     * Sets the current page and scrolls to the start of that page. A page that
     * does not exist in the document is ignored and the viewer does not scroll.
     * You can call this before the document has loaded, in which case the
     * viewer opens the document on that page.
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
     * Gets the number of pages in the loaded document. The value is updated
     * when a document loads or is unloaded, and is {@code 0} while no document
     * is loaded. The default value is {@code 0}.
     * <p>
     * The value is reported by the browser and is not verified by the server. A
     * modified client can report any value, so don't rely on it for access
     * control or other security decisions.
     *
     * @return the number of pages, or {@code 0} if no document is loaded
     */
    @Synchronize(property = "pageCount", value = "page-count-changed")
    public int getPageCount() {
        return getElement().getProperty("pageCount", 0);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(PdfViewerI18n)}.
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public PdfViewerI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component. It enables you
     * to customize and translate the texts used in the viewer. Properties that
     * are not set use the default texts.
     * <p>
     * Note: updating the object properties after setting the i18n will not
     * update the component. To make the changes effective, you need to set the
     * updated object again.
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
     * Adds a listener that is called when the current page changes, either
     * because the user scrolled or navigated to another page, or because
     * {@link #setPage(int)} was called.
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
         * @return the reason, or {@code null} if the client reported a reason
         *         that this version does not know
         */
        public DocumentErrorReason getReason() {
            return reason;
        }
    }
}
