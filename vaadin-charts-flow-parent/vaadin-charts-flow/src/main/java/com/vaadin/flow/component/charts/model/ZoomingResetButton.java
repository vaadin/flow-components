/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.ButtonTheme;

/**
 * The button that appears after a selection zoom, allowing the user to reset
 * zoom.
 *
 * @apiNote This button is only shown after a selection zoom, not after a mouse
 *          wheel zoom.
 */
public class ZoomingResetButton extends AbstractConfigurationObject {

    private Boolean enabled;
    private Position position;
    private ButtonRelativeTo relativeTo;
    private ButtonTheme theme;

    public ZoomingResetButton() {
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Defines whether the reset button is visible. When undefined, it is shown
     * automatically if zooming is enabled and hidden if zooming is disabled.
     * 
     * @param enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
     * 
     * <p>
     * Adjusting position values might cause overlap with chart elements. Ensure
     * coordinates do not obstruct other components or data visibility.
     * </p>
     * 
     * @param position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @see #setRelativeTo(ButtonRelativeTo)
     */
    public ButtonRelativeTo getRelativeTo() {
        return relativeTo;
    }

    /**
     * What frame the button placement should be related to.
     *
     * @param relativeTo
     */
    public void setRelativeTo(ButtonRelativeTo relativeTo) {
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
     * A collection of attributes for the button theme.
     * 
     * @param theme
     */
    public void setTheme(ButtonTheme theme) {
        this.theme = theme;
    }
}
