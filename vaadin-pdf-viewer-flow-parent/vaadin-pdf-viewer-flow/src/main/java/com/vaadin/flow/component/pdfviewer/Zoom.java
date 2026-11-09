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
import java.util.Objects;

/**
 * The zoom level of the pages of {@link PdfViewer}: {@link #PAGE_WIDTH},
 * {@link #PAGE_FIT}, or a factor of the actual size of the pages, see
 * {@link #of(double)}.
 *
 * @author Vaadin Ltd
 * @since 25.4
 * @see PdfViewer#setZoom(Zoom)
 */
public final class Zoom implements Serializable {

    /**
     * Fits the width of the current page to the viewer. The default zoom.
     */
    public static final Zoom PAGE_WIDTH = new Zoom("page-width", 0);

    /**
     * Fits the whole current page into the viewer.
     */
    public static final Zoom PAGE_FIT = new Zoom("page-fit", 0);

    private final String mode;
    private final double factor;

    private Zoom(String mode, double factor) {
        this.mode = mode;
        this.factor = factor;
    }

    /**
     * Creates a zoom that scales the pages relative to their actual size, for
     * example {@code 1} shows them at 100% and {@code 1.5} at 150%.
     *
     * @param factor
     *            the factor of the actual size of the pages, greater than
     *            {@code 0}
     * @return the zoom
     * @throws IllegalArgumentException
     *             if the factor is not greater than {@code 0}, or not finite
     */
    public static Zoom of(double factor) {
        if (!isValidFactor(factor)) {
            throw new IllegalArgumentException(
                    "The zoom factor should be greater than 0, was " + factor);
        }
        return new Zoom(null, factor);
    }

    /**
     * Gets the factor of the actual size of the pages, see {@link #of(double)}.
     * {@link #PAGE_WIDTH} and {@link #PAGE_FIT} do not have a factor, so
     * compare the zoom with them before calling this method.
     *
     * @return the factor, greater than {@code 0}
     * @throws IllegalStateException
     *             if this zoom is {@link #PAGE_WIDTH} or {@link #PAGE_FIT}
     */
    public double getFactor() {
        if (mode != null) {
            throw new IllegalStateException(
                    "The zoom " + mode + " does not have a factor");
        }
        return factor;
    }

    /**
     * Gets the mode that the client takes for this zoom.
     *
     * @return {@code page-width} or {@code page-fit}, or {@code null} for a
     *         factor
     */
    String getMode() {
        return mode;
    }

    /**
     * Gets the zoom that the client reports with the given value.
     *
     * @param clientValue
     *            the value reported by the client: {@code page-width},
     *            {@code page-fit}, or a factor as a number
     * @return the zoom, or {@link #PAGE_WIDTH} if the value is not a zoom,
     *         which is what the client falls back to
     */
    static Zoom fromClientValue(Serializable clientValue) {
        if (clientValue instanceof Number number) {
            return ofFactorOrDefault(number.doubleValue());
        }
        if (PAGE_FIT.mode.equals(clientValue)) {
            return PAGE_FIT;
        }
        return PAGE_WIDTH;
    }

    private static Zoom ofFactorOrDefault(double factor) {
        return isValidFactor(factor) ? new Zoom(null, factor) : PAGE_WIDTH;
    }

    private static boolean isValidFactor(double factor) {
        return Double.isFinite(factor) && factor > 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zoom other && Objects.equals(mode, other.mode)
                && factor == other.factor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, factor);
    }
}
