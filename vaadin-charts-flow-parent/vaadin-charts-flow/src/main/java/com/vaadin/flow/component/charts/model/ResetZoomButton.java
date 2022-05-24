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

import com.vaadin.flow.component.charts.model.style.ButtonTheme;

/**
 * The button that appears after a selection zoom, allowing the user to reset
 * zoom.
 */
public class ResetZoomButton extends AbstractConfigurationObject {

    private Position position;
    private ResetZoomButtonRelativeTo relativeTo;
    private ButtonTheme theme;

    public ResetZoomButton() {
    }

    /**
     * @see #setPosition(Position)
     */
    public Position getPosition() {
        if (position == null) {
            position = new Position();
        }
        return position;
    }

    /**
     * The position of the button.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @see #setRelativeTo(ResetZoomButtonRelativeTo)
     */
    public ResetZoomButtonRelativeTo getRelativeTo() {
        return relativeTo;
    }

    /**
     * What frame the button should be placed related to. Can be either "plot"
     * or "chart".
     * <p>
     * Defaults to: plot
     */
    public void setRelativeTo(ResetZoomButtonRelativeTo relativeTo) {
        this.relativeTo = relativeTo;
    }

    /**
     * @see #setTheme(ButtonTheme)
     */
    public ButtonTheme getTheme() {
        if (theme == null) {
            theme = new ButtonTheme();
        }
        return theme;
    }

    /**
     * A collection of attributes for the button. The object takes SVG
     * attributes like <code>fill</code>, <code>stroke</code>,
     * <code>stroke-width</code> or <code>r</code>, the border radius. The theme
     * also supports <code>style</code>, a collection of CSS properties for the
     * text. Equivalent attributes for the hover state are given in
     * <code>theme.states.hover</code>.
     */
    public void setTheme(ButtonTheme theme) {
        this.theme = theme;
    }
}
