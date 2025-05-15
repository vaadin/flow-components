/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.icon;

import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.DownloadHandler;

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
     * @deprecated Use {@link #SvgIcon(DownloadHandler)} instead
     */
    @Deprecated(since = "24.8", forRemoval = true)
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
     * @deprecated Use {@link #SvgIcon(DownloadHandler, String)} instead
     */
    @Deprecated(since = "24.8", forRemoval = true)
    public SvgIcon(AbstractStreamResource src, String symbol) {
        this(src);
        setSymbol(symbol);
    }

    /**
     * Creates an SVG icon with the given download handler resource
     *
     * @param src
     *            the download handler resource
     * @see #setSrc(AbstractStreamResource)
     */
    public SvgIcon(DownloadHandler src) {
        setSrc(src);
    }

    /**
     * Creates an SVG icon with the given download handler resource
     *
     * @param src
     *            the download handler resource
     * @param symbol
     *            the symbol reference of the icon
     * @see #setSrc(AbstractStreamResource)
     * @see #setSymbol(String)
     */
    public SvgIcon(DownloadHandler src, String symbol) {
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
     * @deprecated Use {@link #setSrc(DownloadHandler)} instead
     */
    @Deprecated(since = "24.8", forRemoval = true)
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
     * @deprecated Use {@link #setSrc(DownloadHandler, String)} instead
     */
    @Deprecated(since = "24.8", forRemoval = true)
    public void setSrc(AbstractStreamResource src, String symbol) {
        setSrc(src);
        setSymbol(symbol);
    }

    /**
     * Defines the source of the icon from the given {@link DownloadHandler} The
     * resource must contain a valid SVG element.
     *
     * @param src
     *            the source value, not null
     */
    public void setSrc(DownloadHandler src) {
        getElement().setAttribute("src",
                new StreamResourceRegistry.ElementStreamResource(src,
                        getElement()));
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
    public void setSrc(DownloadHandler src, String symbol) {
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
