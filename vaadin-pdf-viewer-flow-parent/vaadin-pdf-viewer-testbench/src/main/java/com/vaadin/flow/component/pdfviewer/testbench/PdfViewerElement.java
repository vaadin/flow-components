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
     * Gets the title shown in the header of the viewer.
     *
     * @return the title shown in the header
     */
    public String getHeaderTitle() {
        return $("span").withAttribute("part", "title").single().getText();
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
     * Gets the zoom level of the pages.
     *
     * @return {@code page-width}, {@code page-fit}, or the factor of the actual
     *         size of the pages
     */
    public String getZoom() {
        return String.valueOf(getProperty("zoom"));
    }

    /**
     * Sets the zoom to a mode that fits the pages to the viewer, like selecting
     * it in the zoom select of the toolbar.
     *
     * @param mode
     *            the zoom mode
     */
    public void setZoom(ZoomMode mode) {
        setProperty("zoom", mode.value);
    }

    /**
     * Sets the zoom to a factor of the actual size of the pages, for example
     * {@code 1} for 100% and {@code 1.5} for 150%. The factor does not need to
     * be one of the levels of the zoom select of the toolbar.
     *
     * @param factor
     *            the factor of the actual size of the pages, greater than
     *            {@code 0}
     */
    public void setZoom(double factor) {
        setProperty("zoom", factor);
    }

    /**
     * Zooms in to the next zoom level with the zoom in button of the toolbar.
     */
    public void zoomIn() {
        getToolbarButton("zoom-in").click();
    }

    /**
     * Zooms out to the previous zoom level with the zoom out button of the
     * toolbar.
     */
    public void zoomOut() {
        getToolbarButton("zoom-out").click();
    }

    private TestBenchElement getToolbarButton(String icon) {
        return $("vaadin-pdf-viewer-button").withAttribute("icon", icon)
                .single();
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

    /**
     * The zoom modes that fit the pages to the viewer, see
     * {@link #setZoom(ZoomMode)}.
     */
    public enum ZoomMode {
        /**
         * Fits the width of the current page to the viewer. The default zoom.
         */
        PAGE_WIDTH("page-width"),

        /**
         * Fits the whole current page into the viewer.
         */
        PAGE_FIT("page-fit");

        private final String value;

        ZoomMode(String value) {
            this.value = value;
        }
    }
}
