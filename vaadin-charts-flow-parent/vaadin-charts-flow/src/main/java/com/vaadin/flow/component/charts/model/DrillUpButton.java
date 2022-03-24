package com.vaadin.flow.component.charts.model;

/*
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

/**
 * Options for the drill up button that appears when drilling down on a series.
 * The text for the button is defined in {@link Lang#setDrillUpText(String)}.
 */
public class DrillUpButton extends AbstractConfigurationObject {

    private ButtonPosition position;
    private DrillUpButtonRelativeTo relativeTo;
    private DrillUpButtonTheme theme;

    /**
     * @see #setPosition(ButtonPosition)
     * @return position
     */
    public ButtonPosition getPosition() {
        return position;
    }

    /**
     * Positioning options for the button within the
     * {@link #setRelativeTo(DrillUpButtonRelativeTo)} box.
     *
     * @param position
     */
    public void setPosition(ButtonPosition position) {
        this.position = position;
    }

    /**
     * @see #setRelativeTo(DrillUpButtonRelativeTo)
     * @return relativeTo
     */
    public DrillUpButtonRelativeTo getRelativeTo() {
        return relativeTo;
    }

    /**
     * What box to align the button to. Can be either
     * {@link DrillUpButtonRelativeTo#PLOTBOX} or
     * {@link DrillUpButtonRelativeTo#SPACINGBOX}. Defaults to
     * {@link DrillUpButtonRelativeTo#PLOTBOX}
     *
     * @param relativeTo
     */
    public void setRelativeTo(DrillUpButtonRelativeTo relativeTo) {
        this.relativeTo = relativeTo;
    }

    /**
     * @see #setTheme(DrillUpButtonTheme)
     * @return theme
     */
    public DrillUpButtonTheme getTheme() {
        return theme;
    }

    /**
     * A collection of attributes for the button.
     *
     * @param theme
     */
    public void setTheme(DrillUpButtonTheme theme) {
        this.theme = theme;
    }

}
