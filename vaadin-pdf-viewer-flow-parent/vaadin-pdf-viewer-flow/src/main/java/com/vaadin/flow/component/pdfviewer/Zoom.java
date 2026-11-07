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
import java.util.OptionalDouble;

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
     *
     * @return the factor, or empty for {@link #PAGE_WIDTH} and
     *         {@link #PAGE_FIT}
     */
    public OptionalDouble getFactor() {
        return mode == null ? OptionalDouble.of(factor)
                : OptionalDouble.empty();
    }

    /**
     * Gets the zoom that the client reports with the given value.
     *
     * @param clientValue
     *            the value reported by the client: {@code page-width},
     *            {@code page-fit}, or a factor as a number or a string
     * @return the zoom, or {@link #PAGE_WIDTH} if the value is not a zoom,
     *         which is what the client falls back to
     */
    static Zoom fromClientValue(Serializable clientValue) {
        if (clientValue instanceof Number number) {
            return ofFactorOrDefault(number.doubleValue());
        }
        if (clientValue instanceof String string) {
            if (PAGE_FIT.mode.equals(string)) {
                return PAGE_FIT;
            }
            if (PAGE_WIDTH.mode.equals(string)) {
                return PAGE_WIDTH;
            }
            try {
                return ofFactorOrDefault(Double.parseDouble(string));
            } catch (NumberFormatException e) {
                return PAGE_WIDTH;
            }
        }
        return PAGE_WIDTH;
    }

    /**
     * Gets the value that the client takes for the given zoom.
     *
     * @param zoom
     *            the zoom, or {@code null} for {@link #PAGE_WIDTH}
     * @return {@code page-width}, {@code page-fit}, or the factor as a number
     */
    static Serializable toClientValue(Zoom zoom) {
        if (zoom == null) {
            return PAGE_WIDTH.mode;
        }
        if (zoom.mode != null) {
            return zoom.mode;
        }
        return zoom.factor;
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

    /**
     * Returns {@code page-width}, {@code page-fit}, or the factor.
     */
    @Override
    public String toString() {
        return mode != null ? mode : String.valueOf(factor);
    }
}
