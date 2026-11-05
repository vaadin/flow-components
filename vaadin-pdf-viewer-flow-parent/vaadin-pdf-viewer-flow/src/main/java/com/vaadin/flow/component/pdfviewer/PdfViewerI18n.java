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

    /**
     * Gets the tooltip of the previous page button.
     *
     * @return the tooltip of the previous page button
     */
    public String getPreviousPage() {
        return previousPage;
    }

    /**
     * Sets the tooltip of the previous page button.
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
     * Gets the tooltip of the next page button.
     *
     * @return the tooltip of the next page button
     */
    public String getNextPage() {
        return nextPage;
    }

    /**
     * Sets the tooltip of the next page button.
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
     * @return the error message for a page that does not exist
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
     *            the error message for a page that does not exist
     * @return this instance for method chaining
     */
    public PdfViewerI18n setPageError(String pageError) {
        this.pageError = pageError;
        return this;
    }
}
