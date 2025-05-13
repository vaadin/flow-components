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
package com.vaadin.flow.component.shared;

import java.io.Serializable;
import java.util.Arrays;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * A handle that can be used to configure and control tooltips.
 *
 * @author Vaadin Ltd
 */
@NpmPackage(value = "@vaadin/tooltip", version = "24.8.0-alpha18")
@JsModule("@vaadin/tooltip/src/vaadin-tooltip.js")
public class Tooltip implements Serializable {

    private static final String TOOLTIP_DATA_KEY = "tooltip";

    /**
     * The {@code <vaadin-tooltip>} element controlled by this tooltip instance.
     */
    private final Element tooltipElement = new Element("vaadin-tooltip");

    private Tooltip() {
        super();
    }

    /**
     * Tooltip position in relation to the target element.
     */
    public enum TooltipPosition {
        TOP_START("top-start"),
        TOP("top"),
        TOP_END("top-end"),
        BOTTOM_START("bottom-start"),
        BOTTOM("bottom"),
        BOTTOM_END("bottom-end"),
        START_TOP("start-top"),
        START("start"),
        START_BOTTOM("start-bottom"),
        END_TOP("end-top"),
        END("end"),
        END_BOTTOM("end-bottom");

        private final String position;

        TooltipPosition(String position) {
            this.position = position;
        }

        public String getPosition() {
            return position;
        }

        /**
         * Gets the {@link TooltipPosition} for the given position string.
         * Returns {@link TooltipPosition#BOTTOM} if no match is found.
         *
         * @param position
         *            the position string
         * @return the {@link TooltipPosition}
         */
        public static TooltipPosition fromPosition(String position) {
            return Arrays.stream(TooltipPosition.values())
                    .filter(p -> p.getPosition().equals(position)).findFirst()
                    .orElse(BOTTOM);
        }
    }

    /**
     * Creates a tooltip for the given element.
     *
     * @param element
     *            the element to attach the tooltip to
     * @return the tooltip handle
     */
    private static Tooltip forElement(Element element) {
        // Create a new Tooltip handle instance
        var tooltip = new Tooltip();

        // Handle target attach
        SerializableRunnable onTargetAttach = () -> {
            // Remove the tooltip from its current state tree
            tooltip.tooltipElement.removeFromTree(false);

            // The host under which the <vaadin-tooltip> element is
            // auto-attached
            var tooltipHost = UI.getCurrent().getElement();
            tooltipHost.appendChild(tooltip.tooltipElement);
            tooltip.tooltipElement.executeJs("this.target = $0;", element);
        };
        element.addAttachListener(e -> onTargetAttach.run());
        if (element.getNode().isAttached()) {
            onTargetAttach.run();
        }

        // Handle target detach
        element.addDetachListener(
                e -> tooltip.tooltipElement.removeFromParent());

        return tooltip;
    }

    /**
     * Creates a tooltip to the given {@code Component} if one hasn't already
     * been created.
     *
     * @param component
     *            the component to attach the tooltip to
     * @return the tooltip handle
     */
    public static Tooltip forComponent(Component component) {
        var tooltip = getForElement(component.getElement());
        if (tooltip == null) {
            tooltip = forElement(component.getElement());
            ComponentUtil.setData(component, TOOLTIP_DATA_KEY, tooltip);
        }
        return tooltip;
    }

    /**
     * Gets the tooltip handle for the given element.
     *
     * @param element
     *            the element to get the tooltip handle for
     * @return the tooltip handle
     */
    static Tooltip getForElement(Element element) {
        var component = ComponentUtil.getInnermostComponent(element);
        return (Tooltip) ComponentUtil.getData(component, TOOLTIP_DATA_KEY);
    }

    /**
     * Creates a tooltip to the given {@link HasTooltip} component and adds the
     * tooltip element to the component's tooltip slot.
     *
     * @param hasTooltip
     *            the component to attach the tooltip to
     * @return the tooltip handle
     */
    static Tooltip forHasTooltip(HasTooltip hasTooltip) {
        var tooltip = new Tooltip();
        SlotUtils.setSlot(hasTooltip, "tooltip", tooltip.tooltipElement);
        var component = ComponentUtil
                .getInnermostComponent(hasTooltip.getElement());
        ComponentUtil.setData(component, TOOLTIP_DATA_KEY, tooltip);
        return tooltip;
    }

    /**
     * String used as a tooltip content.
     *
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        tooltipElement.setProperty("text", text);
    }

    /**
     * String used as a tooltip content.
     *
     * @return the text
     */
    public String getText() {
        return tooltipElement.getProperty("text");
    }

    /**
     * String used as a tooltip content.
     *
     * @param text
     *            the text to set
     */
    public Tooltip withText(String text) {
        setText(text);
        return this;
    }

    /**
     * The delay in milliseconds before the tooltip is opened on keyboard focus,
     * when not in manual mode.
     *
     * @param focusDelay
     *            the delay in milliseconds
     */
    public void setFocusDelay(int focusDelay) {
        tooltipElement.setProperty("focusDelay", focusDelay);
    }

    /**
     * The delay in milliseconds before the tooltip is opened on keyboard focus,
     * when not in manual mode.
     *
     * @return the delay in milliseconds
     */
    public int getFocusDelay() {
        return tooltipElement.getProperty("focusDelay", 0);
    }

    /**
     * The delay in milliseconds before the tooltip is opened on keyboard focus,
     * when not in manual mode.
     *
     * @param focusDelay
     *            the delay in milliseconds
     */
    public Tooltip withFocusDelay(int focusDelay) {
        setFocusDelay(focusDelay);
        return this;
    }

    /**
     * The delay in milliseconds before the tooltip is closed on losing hover,
     * when not in manual mode. On blur, the tooltip is closed immediately.
     *
     * @param hideDelay
     *            the delay in milliseconds
     */
    public void setHideDelay(int hideDelay) {
        tooltipElement.setProperty("hideDelay", hideDelay);
    }

    /**
     * The delay in milliseconds before the tooltip is closed on losing hover,
     * when not in manual mode. On blur, the tooltip is closed immediately.
     *
     * @return the delay in milliseconds
     */
    public int getHideDelay() {
        return tooltipElement.getProperty("hideDelay", 0);
    }

    /**
     * The delay in milliseconds before the tooltip is closed on losing hover,
     * when not in manual mode. On blur, the tooltip is closed immediately.
     *
     * @param hideDelay
     *            the delay in milliseconds
     */
    public Tooltip withHideDelay(int hideDelay) {
        setHideDelay(hideDelay);
        return this;
    }

    /**
     * The delay in milliseconds before the tooltip is opened on hover, when not
     * in manual mode.
     *
     * @param hoverDelay
     *            the delay in milliseconds
     */
    public void setHoverDelay(int hoverDelay) {
        tooltipElement.setProperty("hoverDelay", hoverDelay);
    }

    /**
     * The delay in milliseconds before the tooltip is opened on hover, when not
     * in manual mode.
     *
     * @return the delay in milliseconds
     */
    public int getHoverDelay() {
        return tooltipElement.getProperty("hoverDelay", 0);
    }

    /**
     * The delay in milliseconds before the tooltip is opened on hover, when not
     * in manual mode.
     *
     * @param hoverDelay
     *            the delay in milliseconds
     */
    public Tooltip withHoverDelay(int hoverDelay) {
        setHoverDelay(hoverDelay);
        return this;
    }

    /**
     * Position of the tooltip with respect to its target.
     *
     * @param position
     *            the position to set
     */
    public void setPosition(TooltipPosition position) {
        tooltipElement.setProperty("position", position.getPosition());
    }

    /**
     * Position of the tooltip with respect to its target.
     *
     * @return the position
     */
    public TooltipPosition getPosition() {
        var positionString = tooltipElement.getProperty("position");
        return Arrays.stream(TooltipPosition.values())
                .filter(p -> p.getPosition().equals(positionString)).findFirst()
                .orElse(null);
    }

    /**
     * Position of the tooltip with respect to its target.
     *
     * @param position
     *            the position to set
     */
    public Tooltip withPosition(TooltipPosition position) {
        setPosition(position);
        return this;
    }

    /**
     * When true, the tooltip is controlled programmatically instead of reacting
     * to focus and mouse events.
     *
     * @param manual
     *            true to enable manual mode
     */
    public void setManual(boolean manual) {
        tooltipElement.setProperty("manual", manual);
    }

    /**
     * When true, the tooltip is controlled programmatically instead of reacting
     * to focus and mouse events.
     *
     * @return true if manual mode is enabled
     */
    public boolean isManual() {
        return tooltipElement.getProperty("manual", false);
    }

    /**
     * When true, the tooltip is controlled programmatically instead of reacting
     * to focus and mouse events.
     *
     * @param manual
     *            true to enable manual mode
     */
    public Tooltip withManual(boolean manual) {
        setManual(manual);
        return this;
    }

    /**
     * When true, the tooltip is opened programmatically. Only works if `manual`
     * is set to `true`.
     *
     * @param opened
     *            true to open the tooltip
     */
    public void setOpened(boolean opened) {
        tooltipElement.setProperty("opened", opened);
    }

    /**
     * When true, the tooltip is opened programmatically. Only works if `manual`
     * is set to `true`.
     *
     * @return true if the tooltip is opened
     */
    public boolean isOpened() {
        return tooltipElement.getProperty("opened", false);
    }
}
