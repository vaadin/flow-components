/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.icon;

import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;

/**
 * Component for displaying an icon from a SVG file.
 *
 * @author Vaadin Ltd
 */
public class SvgIcon extends AbstractIcon<SvgIcon> {
    private static final String STYLE_FILL = "fill";

    /**
     * Default constructor. Creates an empty SVG icon.
     */
    public SvgIcon() {
    }

    /**
     * Creates an SVG icon with the given source
     *
     * @param src
     *            the SVG file path
     * @see #setSrc(String)
     */
    public SvgIcon(String src) {
        setSrc(src);
    }

    /**
     * Creates an SVG icon with the given source and symbol
     *
     * @param src
     *            the SVG file path
     * @param symbol
     *            the symbol reference of the icon
     * @see #setSrc(String)
     * @see #setSymbol(String)
     */
    public SvgIcon(String src, String symbol) {
        this(src);
        setSymbol(symbol);
    }

    /**
     * Creates an SVG icon with the given resource
     *
     * @param src
     *            the resource value
     * @see #setSrc(AbstractStreamResource)
     */
    public SvgIcon(AbstractStreamResource src) {
        setSrc(src);
    }

    /**
     * Creates an SVG icon with the given resource
     *
     * @param src
     *            the resource value
     * @param symbol
     *            the symbol reference of the icon
     * @see #setSrc(AbstractStreamResource)
     * @see #setSymbol(String)
     */
    public SvgIcon(AbstractStreamResource src, String symbol) {
        this(src);
        setSymbol(symbol);
    }

    /**
     * Sets the URL of the SVG file to be used as the icon. The value can be:
     * <ul>
     *
     * <li>A path to a standalone SVG file</li>
     * <li>
     * <p>
     * A path in the format `"path/to/file.svg#symbol-id"` to an SVG file, where
     * "symbol-id" refers to an id of an element (generally a
     * `<symbol></symbol>` element) to be rendered in the icon component.
     * </p>
     * <p>
     * Note that the sprite file needs to follow the same-origin policy
     * </p>
     * </li>
     * <li>Alternatively, the source can be defined as a string in the format
     * `"data:image/svg+xml,<svg>...</svg>`</li>
     * </ul>
     *
     * @param src
     *            the source file of the icon
     */
    public void setSrc(String src) {
        getElement().setAttribute("src", src);
    }

    /**
     * Defines the src and the symbol to be used in the icon.
     *
     * @param src
     *            the path of the icon sprite file
     * @param symbol
     *            the symbol reference of the icon
     * @see #setSrc(String)
     * @see #setSymbol(String)
     */
    public void setSrc(String src, String symbol) {
        setSrc(src);
        setSymbol(symbol);
    }

    /**
     * Defines the source of the icon from the given {@link StreamResource} The
     * resource must contain a valid SVG element.
     *
     * @param src
     *            the source value, not null
     */
    public void setSrc(AbstractStreamResource src) {
        getElement().setAttribute("src", src);
    }

    /**
     * Defines the src and the symbol to be used in the icon.
     *
     * @param src
     *            the source of the icon sprite file, not null
     * @param symbol
     *            the symbol reference of the icon
     * @see #setSrc(AbstractStreamResource)
     * @see #setSymbol(String)
     */
    public void setSrc(AbstractStreamResource src, String symbol) {
        setSrc(src);
        setSymbol(symbol);
    }

    /**
     * Gets the source defined in the icon.
     *
     * @return the source defined or {@code null}
     */
    public String getSrc() {
        return getElement().getAttribute("src");
    }

    /**
     * <p>
     * Defines the symbol identifier that references an ID of an element
     * contained in the SVG element assigned to the {@link #setSrc(String)}
     * property.
     * </p>
     * <p>
     * If there's an identifier in the path defined in {@link #setSrc(String)}
     * in the moment this method is called, the value passed to
     * {@link #setSymbol(String)} will be used.
     * </p>
     *
     * @param symbol
     *            the symbol identifier of the icon to be shown
     */
    public void setSymbol(String symbol) {
        getElement().setProperty("symbol", symbol);
    }

    /**
     * Gets the symbol defined in the icon.
     *
     * @return the symbol defined or {@code null}
     */
    public String getSymbol() {
        return getElement().getProperty("symbol");
    }

    @Override
    public void setColor(String color) {
        getStyle().set(STYLE_FILL, color);
    }

    @Override
    public String getColor() {
        return getStyle().get(STYLE_FILL);
    }
}
