/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-pdf-viewer&gt;</code>
 * element.
 */
@Element("vaadin-pdf-viewer")
public class PdfViewerElement extends TestBenchElement {

    /**
     * Gets the URL of the document.
     *
     * @return the URL of the document, or {@code null} if no document is set
     */
    public String getSrc() {
        return getPropertyString("src");
    }

    /**
     * Gets the number of pages in the loaded document.
     *
     * @return the number of pages, or {@code 0} if no document is loaded
     */
    public int getPageCount() {
        return getPropertyInteger("pageCount");
    }

    /**
     * Gets the current page.
     *
     * @return the current page, starting from {@code 1}
     */
    public int getPage() {
        return getPropertyInteger("page");
    }

    /**
     * Gets the previous page button in the toolbar.
     *
     * @return the previous page button, or {@code null} if there is none
     */
    public TestBenchElement getPreviousPageButton() {
        return getToolbarButton("previous-page");
    }

    /**
     * Gets the next page button in the toolbar.
     *
     * @return the next page button, or {@code null} if there is none
     */
    public TestBenchElement getNextPageButton() {
        return getToolbarButton("next-page");
    }

    /**
     * Checks whether a document is being loaded.
     *
     * @return {@code true} if a document is being loaded, {@code false}
     *         otherwise
     */
    public boolean isLoading() {
        return hasAttribute("loading");
    }

    /**
     * Checks whether the document could not be loaded.
     *
     * @return {@code true} if the document could not be loaded, {@code false}
     *         otherwise
     */
    public boolean hasError() {
        return hasAttribute("has-error");
    }

    private TestBenchElement getToolbarButton(String icon) {
        return $("vaadin-pdf-viewer-button").withAttribute("icon", icon).all()
                .stream().findFirst().orElse(null);
    }
}
