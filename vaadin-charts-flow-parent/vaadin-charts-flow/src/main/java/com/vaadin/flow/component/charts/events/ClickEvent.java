package com.vaadin.flow.component.charts.events;

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

import java.io.Serializable;

import com.vaadin.flow.component.charts.util.Util;

public interface ClickEvent extends Serializable {

    /**
     * Gets the mouse click details
     *
     * @return
     */
    MouseEventDetails getMouseDetails();

    /**
     * Gets the x axis value of the clicked point.
     * <p>
     * Note, that if the axis type is Date, the value is "unix timestamp" which
     * is shifted to UTF time zone that is used by the client side
     * implementation. If you have used Date object as value, you most likely
     * want to pass the value thru {@link Util#toServerDate(double)} method
     * before actually using the value.
     * <p>
     * Legend items are outside of the plot area and legend item click events
     * have no significant xAxisValue.
     *
     * @return the X coordinate of the click.
     */
    default double getxAxisValue() {
        return getMouseDetails().getxValue();
    }

    /**
     * Gets the y axis value of the clicked point.
     * <p>
     * Legend items are outside of the plot area and legend item click events
     * have no significant yAxisValue.
     *
     * @return the Y coordinate of the click
     */
    default double getyAxisValue() {
        return getMouseDetails().getyValue();
    }

    /**
     * @return the absolute x position of the clicked point in browser client
     *         area in pixels or -1 if chart type (like pie) don't have relevant
     *         point
     */
    default int getAbsoluteX() {
        return getMouseDetails().getAbsoluteX();
    }

    /**
     * @return the absolute x position of the clicked point in browser client
     *         area in pixels or -1 if chart type (like pie) don't have relevant
     *         point
     */
    default int getAbsoluteY() {
        return getMouseDetails().getAbsoluteY();
    }

    /**
     * Gets the {@link MouseEventDetails.MouseButton} of the click event.
     * <p>
     * Note that the charting library only allows MouseButton.LEFT clicks in
     * most cases.
     *
     * @return the {@link MouseEventDetails.MouseButton} of the click event.
     */
    default MouseEventDetails.MouseButton getButton() {
        return getMouseDetails().getButton();
    }

    /**
     * Checks if the Alt key was down when the mouse event took place.
     *
     * @return true if Alt was down when the event occurred, false otherwise
     */
    default boolean isAltKey() {
        return getMouseDetails().isAltKey();
    }

    /**
     * Checks if the Ctrl key was down when the mouse event took place.
     *
     * @return true if Ctrl was pressed when the event occurred, false otherwise
     */
    default boolean isCtrlKey() {
        return getMouseDetails().isCtrlKey();
    }

    /**
     * Checks if the Meta key was down when the mouse event took place.
     *
     * @return true if Meta was pressed when the event occurred, false otherwise
     */
    default boolean isMetaKey() {
        return getMouseDetails().isMetaKey();
    }

    /**
     * Checks if the Shift key was down when the mouse event took place.
     *
     * @return true if Shift was pressed when the event occurred, false
     *         otherwise
     */
    default boolean isShiftKey() {
        return getMouseDetails().isShiftKey();
    }
}
