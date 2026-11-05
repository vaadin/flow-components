/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The i18n object of {@link PdfViewer}, which translates the texts of the
 * viewer. Texts it does not set keep their defaults.
 *
 * @see PdfViewer#setI18n(PdfViewerI18n)
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdfViewerI18n implements Serializable {
    private String previousPage;
    private String nextPage;
    private String pageError;
    private String zoomIn;
    private String zoomOut;
    private String pageWidth;
    private String pageFit;
    private String document;
    private String link;
    private String goToPage;
    private String externalLink;
    private String download;

    /**
     * Gets the tooltip of the previous page button, also used as the accessible
     * name of a link to the previous page.
     *
     * @return the tooltip of the previous page button
     */
    public String getPreviousPage() {
        return previousPage;
    }

    /**
     * Sets the tooltip of the previous page button, also used as the accessible
     * name of a link to the previous page.
     *
     * @param previousPage
     *            the tooltip of the previous page button
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
        return this;
    }

    /**
     * Gets the tooltip of the next page button, also used as the accessible
     * name of a link to the next page.
     *
     * @return the tooltip of the next page button
     */
    public String getNextPage() {
        return nextPage;
    }

    /**
     * Sets the tooltip of the next page button, also used as the accessible
     * name of a link to the next page.
     *
     * @param nextPage
     *            the tooltip of the next page button
     * @return this instance for method chaining
     */
    public PdfViewerI18n setNextPage(String nextPage) {
        this.nextPage = nextPage;
        return this;
    }

    /**
     * Gets the error message shown when the user enters a page that does not
     * exist.
     *
     * @return the error message shown when the user enters a page that does not
     *         exist
     */
    public String getPageError() {
        return pageError;
    }

    /**
     * Sets the error message shown when the user enters a page that does not
     * exist. The {@code {pageCount}} placeholder is replaced with the number of
     * pages in the document, for example {@code "Enter 1–{pageCount}"}.
     *
     * @param pageError
     *            the error message shown when the user enters a page that does
     *            not exist
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageError(String pageError) {
        this.pageError = pageError;
        return this;
    }

    /**
     * Gets the tooltip of the zoom in button.
     *
     * @return the tooltip of the zoom in button
     */
    public String getZoomIn() {
        return zoomIn;
    }

    /**
     * Sets the tooltip of the zoom in button.
     *
     * @param zoomIn
     *            the tooltip of the zoom in button
     * @return this instance for method chaining
     */
    public PdfViewerI18n setZoomIn(String zoomIn) {
        this.zoomIn = zoomIn;
        return this;
    }

    /**
     * Gets the tooltip of the zoom out button.
     *
     * @return the tooltip of the zoom out button
     */
    public String getZoomOut() {
        return zoomOut;
    }

    /**
     * Sets the tooltip of the zoom out button.
     *
     * @param zoomOut
     *            the tooltip of the zoom out button
     * @return this instance for method chaining
     */
    public PdfViewerI18n setZoomOut(String zoomOut) {
        this.zoomOut = zoomOut;
        return this;
    }

    /**
     * Gets the text of the zoom option that fits the page width to the viewer.
     *
     * @return the text of the zoom option that fits the page width to the
     *         viewer
     */
    public String getPageWidth() {
        return pageWidth;
    }

    /**
     * Sets the text of the zoom option that fits the page width to the viewer.
     *
     * @param pageWidth
     *            the text of the zoom option that fits the page width to the
     *            viewer
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageWidth(String pageWidth) {
        this.pageWidth = pageWidth;
        return this;
    }

    /**
     * Gets the text of the zoom option that fits the whole page into the
     * viewer.
     *
     * @return the text of the zoom option that fits the whole page into the
     *         viewer
     */
    public String getPageFit() {
        return pageFit;
    }

    /**
     * Sets the text of the zoom option that fits the whole page into the
     * viewer.
     *
     * @param pageFit
     *            the text of the zoom option that fits the whole page into the
     *            viewer
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageFit(String pageFit) {
        this.pageFit = pageFit;
        return this;
    }

    /**
     * Gets the header title for a document without a file name.
     *
     * @return the header title for a document without a file name
     */
    public String getDocument() {
        return document;
    }

    /**
     * Sets the header title for a document without a file name.
     *
     * @param document
     *            the header title for a document without a file name
     * @return this instance for method chaining
     */
    public PdfViewerI18n setDocument(String document) {
        this.document = document;
        return this;
    }

    /**
     * Gets the accessible name of a link without text whose destination is
     * unknown.
     *
     * @return the accessible name of a link without text whose destination is
     *         unknown
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the accessible name of a link without text whose destination is
     * unknown.
     *
     * @param link
     *            the accessible name of a link without text whose destination
     *            is unknown
     * @return this instance for method chaining
     */
    public PdfViewerI18n setLink(String link) {
        this.link = link;
        return this;
    }

    /**
     * Gets the accessible name of a link without text to another page.
     *
     * @return the accessible name of a link without text to another page
     */
    public String getGoToPage() {
        return goToPage;
    }

    /**
     * Sets the accessible name of a link without text to another page. The
     * {@code {page}} placeholder is replaced with the page number, for example
     * {@code "Go to page {page}"}.
     *
     * @param goToPage
     *            the accessible name of a link without text to another page
     * @return this instance for method chaining
     */
    public PdfViewerI18n setGoToPage(String goToPage) {
        this.goToPage = goToPage;
        return this;
    }

    /**
     * Gets the accessible name of a link that opens in a new tab.
     *
     * @return the accessible name of a link that opens in a new tab
     */
    public String getExternalLink() {
        return externalLink;
    }

    /**
     * Sets the accessible name of a link that opens in a new tab. The
     * {@code {text}} placeholder is replaced with the text of the link, or its
     * URL, for example {@code "{text} (opens in a new tab)"}.
     *
     * @param externalLink
     *            the accessible name of a link that opens in a new tab
     * @return this instance for method chaining
     */
    public PdfViewerI18n setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    /**
     * Gets the tooltip of the download button.
     *
     * @return the tooltip of the download button
     */
    public String getDownload() {
        return download;
    }

    /**
     * Sets the tooltip of the download button.
     *
     * @param download
     *            the tooltip of the download button
     * @return this instance for method chaining
     */
    public PdfViewerI18n setDownload(String download) {
        this.download = download;
        return this;
    }
}
