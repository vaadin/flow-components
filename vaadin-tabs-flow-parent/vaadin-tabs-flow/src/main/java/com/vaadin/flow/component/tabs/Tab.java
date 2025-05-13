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
package com.vaadin.flow.component.tabs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;

/**
 * This component provides an accessible and customizable tab to be used inside
 * {@link Tabs} component.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-tab")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/tabs/src/vaadin-tab.js")
@NpmPackage(value = "@vaadin/tabs", version = "24.8.0-alpha18")
public class Tab extends Component implements HasAriaLabel, HasComponents,
        HasLabel, HasStyle, HasThemeVariant<TabVariant>, HasTooltip {

    private static final String FLEX_GROW_CSS_PROPERTY = "flexGrow";

    /**
     * Constructs a new object in its default state.
     */
    public Tab() {
    }

    /**
     * Constructs a new object with the given label.
     *
     * @param label
     *            the label to display
     */
    public Tab(String label) {
        setLabel(label);
    }

    /**
     * Constructs a new object with child components.
     *
     * @param components
     *            the child components
     */
    public Tab(Component... components) {
        add(components);
    }

    /**
     * Gets the label of this tab.
     *
     * @return the label
     */
    @Override
    public final String getLabel() {
        return getElement().getText();
    }

    /**
     * Sets the label of this tab.
     *
     * @param label
     *            the label to display
     */
    @Override
    public final void setLabel(String label) {
        getElement().setText(label);
    }

    /**
     * Sets the flex grow property of this tab. The flex grow property specifies
     * what amount of the available space inside the layout the component should
     * take up, proportionally to the other components.
     * <p>
     * For example, if all components have a flex grow property value set to 1,
     * the remaining space in the layout will be distributed equally to all
     * components inside the layout. If you set a flex grow property of one
     * component to 2, that component will take twice the available space as the
     * other components, and so on.
     * <p>
     * Setting to flex grow property value 0 disables the expansion of the
     * component. Negative values are not allowed.
     *
     * @param flexGrow
     *            the proportion of the available space the tab should take up
     */
    public void setFlexGrow(double flexGrow) {
        if (flexGrow < 0) {
            throw new IllegalArgumentException(
                    "Flex grow property cannot be negative");
        }
        if (flexGrow == 0) {
            getElement().getStyle().remove(FLEX_GROW_CSS_PROPERTY);
        } else {
            getElement().getStyle().set(FLEX_GROW_CSS_PROPERTY,
                    String.valueOf(flexGrow));
        }
    }

    /**
     * Gets the flex grow property of this tab.
     *
     * @return the flex grow property, or 0 if none was set
     */
    public double getFlexGrow() {
        String ratio = getElement().getStyle().get(FLEX_GROW_CSS_PROPERTY);
        if (ratio == null || ratio.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(ratio);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "The flex grow property of the component is not parseable to double: "
                            + ratio,
                    e);
        }
    }

    /**
     * If true, the item is in selected state.
     *
     * @param selected
     *            the boolean value to set
     */
    public void setSelected(boolean selected) {
        getElement().setProperty("selected", selected);
    }

    /**
     * If true, the item is in selected state.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     * If a {@link Tab} instance is used inside {@link Tabs} component then
     * selected state is updated based on currently selected tab. So the value
     * is the same as {@link Tabs#getSelectedTab()}.
     *
     * @see Tabs#getSelectedTab()
     *
     * @return the {@code selected} property from the webcomponent
     */
    public boolean isSelected() {
        return getElement().getProperty("selected", false);
    }

    @Override
    public String toString() {
        return "Tab{" + getLabel() + "}";
    }
}
