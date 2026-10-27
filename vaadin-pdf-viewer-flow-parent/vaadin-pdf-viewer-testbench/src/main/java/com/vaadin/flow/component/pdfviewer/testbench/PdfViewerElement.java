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
}
