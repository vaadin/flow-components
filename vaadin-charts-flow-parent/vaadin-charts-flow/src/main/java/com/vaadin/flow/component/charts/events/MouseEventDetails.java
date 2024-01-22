
package com.vaadin.flow.component.charts.events;

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

import java.io.Serializable;

/**
 * Helper class to store and transfer mouse event details.
 */
public class MouseEventDetails implements Serializable {

    private MouseButton button;
    private int x;
    private int y;
    private int absoluteX;
    private int absoluteY;
    private boolean altKey;
    private boolean ctrlKey;
    private boolean metaKey;
    private boolean shiftKey;
    private double xValue;
    private double yValue;

    public MouseEventDetails() {
    }

    public MouseButton getButton() {
        return button;
    }

    public int getAbsoluteX() {
        return absoluteX;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }

    public boolean isAltKey() {
        return altKey;
    }

    public boolean isCtrlKey() {
        return ctrlKey;
    }

    public boolean isMetaKey() {
        return metaKey;
    }

    public boolean isShiftKey() {
        return shiftKey;
    }

    public double getxValue() {
        return xValue;
    }

    public double getyValue() {
        return yValue;
    }

    public void setButton(MouseButton button) {
        this.button = button;
    }

    public void setAbsoluteX(int absoluteX) {
        this.absoluteX = absoluteX;
    }

    public void setAbsoluteY(int absoluteY) {
        this.absoluteY = absoluteY;
    }

    public void setAltKey(boolean altKey) {
        this.altKey = altKey;
    }

    public void setCtrlKey(boolean ctrlKey) {
        this.ctrlKey = ctrlKey;
    }

    public void setMetaKey(boolean metaKey) {
        this.metaKey = metaKey;
    }

    public void setShiftKey(boolean shiftKey) {
        this.shiftKey = shiftKey;
    }

    public void setxValue(double xValue) {
        this.xValue = xValue;
    }

    public void setyValue(double yValue) {
        this.yValue = yValue;
    }

    public String getButtonName() {
        return button == null ? "" : button.getName();
    }

    /**
     * Constants for mouse buttons.
     */
    public enum MouseButton {
        LEFT("left"), RIGHT("right"), MIDDLE("middle");

        private String name;

        private MouseButton(String name) {
            this.name = name;
        }

        /**
         * Returns a human readable text representing the button
         *
         * @return name
         */
        public String getName() {
            return name;
        }

        public static MouseButton of(int button) {
            switch (button) {
            case 0:
                return LEFT;
            case 1:
                return MIDDLE;
            default:
                return RIGHT;
            }
        }
    }

    @Override
    public String toString() {
        return "MouseEventDetails{" + "button=" + button + ", absoluteX="
                + absoluteX + ", absoluteY=" + absoluteY + ", altKey=" + altKey
                + ", ctrlKey=" + ctrlKey + ", metaKey=" + metaKey
                + ", shiftKey=" + shiftKey + ", xValue=" + xValue + ", yValue="
                + yValue + '}';
    }
}
