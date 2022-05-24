package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.time.Instant;
import java.util.Date;

/**
 * DataSeriesItem that can hold also title and text values. Used in flags
 * charts.
 */
public class FlagItem extends DataSeriesItem {

    private String title;
    private String text;

    /**
     * Constructs an item with X and Title values
     *
     * @param x
     * @param title
     */
    public FlagItem(Number x, String title) {
        setX(x);
        setTitle(title);
    }

    /**
     * Constructs an item with X and Title values
     *
     * @param instant
     * @param title
     */
    public FlagItem(Instant instant, String title) {
        setX(instant);
        setTitle(title);
    }

    /**
     * @deprecated as of 4.0. Use {@link #FlagItem(Instant, String)}
     */
    @Deprecated
    public FlagItem(Date date, String title) {
        setX(date);
        setTitle(title);
    }

    /**
     * Constructs an item with X, Title and Text values
     *
     * @param x
     * @param title
     */
    public FlagItem(Number x, String title, String text) {
        setX(x);
        setTitle(title);
        setText(text);
    }

    /**
     * Constructs an item with X, Title and Text values
     *
     * @param instant
     * @param title
     * @param text
     */
    public FlagItem(Instant instant, String title, String text) {
        setX(instant);
        setTitle(title);
        setText(text);
    }

    /**
     * @deprecated as of 4.0. Use {@link #FlagItem(Instant, String, String)}
     */
    @Deprecated
    public FlagItem(Date date, String title, String text) {
        setX(date);
        setTitle(title);
        setText(text);
    }

    /**
     * Sets the title of the flag
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
        makeCustomized();
    }

    /**
     * @return the title of the flag
     */
    public String getTitle() {
        return title;
    }

    /**
     * @see #setText(String)
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be displayed when the flag is highlighted
     */
    public void setText(String text) {
        this.text = text;
    }

}
