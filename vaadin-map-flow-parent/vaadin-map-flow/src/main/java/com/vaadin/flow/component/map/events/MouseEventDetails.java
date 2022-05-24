package com.vaadin.flow.component.map.events;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;

/**
 * Helper class to store and transfer mouse event details.
 */
public class MouseEventDetails implements Serializable {

    private MouseButton button;
    private int absoluteX;
    private int absoluteY;
    private boolean altKey;
    private boolean ctrlKey;
    private boolean metaKey;
    private boolean shiftKey;

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
                + ", shiftKey=" + shiftKey + '}';
    }
}