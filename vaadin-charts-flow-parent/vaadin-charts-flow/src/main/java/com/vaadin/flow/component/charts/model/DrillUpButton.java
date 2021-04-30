package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.ButtonTheme;


/**
 * Options for the drill up button that appears when drilling down on a series.
 * The text for the button is defined in {@link Lang#setDrillUpText(String)}.
 */
public class DrillUpButton extends AbstractConfigurationObject {

    private ButtonPosition position;
    private DrillUpButtonRelativeTo relativeTo;
    private ButtonTheme theme;

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
     * @see #setTheme(ButtonTheme)
     * @return theme
     */
    public ButtonTheme getTheme() {
        return theme;
    }

    /**
     * A collection of attributes for the button.
     * 
     * @param theme
     */
    public void setTheme(ButtonTheme theme) {
        this.theme = theme;
    }

}
