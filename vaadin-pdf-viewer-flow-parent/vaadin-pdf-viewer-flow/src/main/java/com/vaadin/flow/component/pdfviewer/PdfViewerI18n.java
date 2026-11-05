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
 * Translatable texts of {@link PdfViewer}.
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
     * Gets the tooltip of the previous page button.
     *
     * @return the tooltip
     */
    public String getPreviousPage() {
        return previousPage;
    }

    /**
     * Sets the tooltip of the previous page button. The viewer also uses it as
     * the accessible name of links in the document that go to the previous
     * page.
     *
     * @param previousPage
     *            the tooltip
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
        return this;
    }

    /**
     * Gets the tooltip of the next page button.
     *
     * @return the tooltip
     */
    public String getNextPage() {
        return nextPage;
    }

    /**
     * Sets the tooltip of the next page button. The viewer also uses it as the
     * accessible name of links in the document that go to the next page.
     *
     * @param nextPage
     *            the tooltip
     * @return this instance for method chaining
     */
    public PdfViewerI18n setNextPage(String nextPage) {
        this.nextPage = nextPage;
        return this;
    }

    /**
     * Gets the error message for a page that does not exist.
     *
     * @return the error message
     */
    public String getPageError() {
        return pageError;
    }

    /**
     * Sets the error message for a page that does not exist.
     * {@code {pageCount}} is replaced with the page count, for example
     * {@code "Enter 1–{pageCount}"}.
     *
     * @param pageError
     *            the error message
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageError(String pageError) {
        this.pageError = pageError;
        return this;
    }

    /**
     * Gets the tooltip of the zoom in button.
     *
     * @return the tooltip
     */
    public String getZoomIn() {
        return zoomIn;
    }

    /**
     * Sets the tooltip of the zoom in button.
     *
     * @param zoomIn
     *            the tooltip
     * @return this instance for method chaining
     */
    public PdfViewerI18n setZoomIn(String zoomIn) {
        this.zoomIn = zoomIn;
        return this;
    }

    /**
     * Gets the tooltip of the zoom out button.
     *
     * @return the tooltip
     */
    public String getZoomOut() {
        return zoomOut;
    }

    /**
     * Sets the tooltip of the zoom out button.
     *
     * @param zoomOut
     *            the tooltip
     * @return this instance for method chaining
     */
    public PdfViewerI18n setZoomOut(String zoomOut) {
        this.zoomOut = zoomOut;
        return this;
    }

    /**
     * Gets the text of the zoom option that fits the page width.
     *
     * @return the text
     */
    public String getPageWidth() {
        return pageWidth;
    }

    /**
     * Sets the text of the zoom option that fits the page width.
     *
     * @param pageWidth
     *            the text
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageWidth(String pageWidth) {
        this.pageWidth = pageWidth;
        return this;
    }

    /**
     * Gets the text of the zoom option that fits the whole page.
     *
     * @return the text
     */
    public String getPageFit() {
        return pageFit;
    }

    /**
     * Sets the text of the zoom option that fits the whole page.
     *
     * @param pageFit
     *            the text
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageFit(String pageFit) {
        this.pageFit = pageFit;
        return this;
    }

    /**
     * Gets the header title of a document without a file name.
     *
     * @return the title
     */
    public String getDocument() {
        return document;
    }

    /**
     * Sets the header title of a document without a file name.
     *
     * @param document
     *            the title
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
     * @return the accessible name
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the accessible name of a link without text whose destination is
     * unknown.
     *
     * @param link
     *            the accessible name
     * @return this instance for method chaining
     */
    public PdfViewerI18n setLink(String link) {
        this.link = link;
        return this;
    }

    /**
     * Gets the accessible name of a link without text to another page.
     *
     * @return the accessible name
     */
    public String getGoToPage() {
        return goToPage;
    }

    /**
     * Sets the accessible name of a link without text to another page.
     * {@code {page}} is replaced with the page number, for example
     * {@code "Go to page {page}"}.
     *
     * @param goToPage
     *            the accessible name
     * @return this instance for method chaining
     */
    public PdfViewerI18n setGoToPage(String goToPage) {
        this.goToPage = goToPage;
        return this;
    }

    /**
     * Gets the accessible name of a link that opens in a new tab.
     *
     * @return the accessible name
     */
    public String getExternalLink() {
        return externalLink;
    }

    /**
     * Sets the accessible name of a link that opens in a new tab.
     * {@code {text}} is replaced with the link text or URL, for example
     * {@code "{text} (opens in a new tab)"}.
     *
     * @param externalLink
     *            the accessible name
     * @return this instance for method chaining
     */
    public PdfViewerI18n setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    /**
     * Gets the tooltip of the download button.
     *
     * @return the tooltip
     */
    public String getDownload() {
        return download;
    }

    /**
     * Sets the tooltip of the download button.
     *
     * @param download
     *            the tooltip
     * @return this instance for method chaining
     */
    public PdfViewerI18n setDownload(String download) {
        this.download = download;
        return this;
    }
}
