/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.button;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.nodefeature.ElementAttributeMap;
import com.vaadin.flow.internal.nodefeature.NodeFeature;
import com.vaadin.flow.shared.Registration;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Button component allows users to perform actions. It comes in several
 * different style variants, and supports icons in addition to text labels.
 *
 * @author Vaadin Ltd
 */
public class Button extends GeneratedVaadinButton<Button>
        implements HasSize, HasEnabled {

    private Component iconComponent;
    private boolean iconAfterText;
    private boolean disableOnClick = false;

    // Register immediately as first listener
    private Registration disableListener = addClickListener(
            buttonClickEvent -> {
                if (disableOnClick) {
                    doDisableOnClick();
                }
            });

    /**
     * Default constructor. Creates an empty button.
     */
    public Button() {
    }

    /**
     * Creates a button with a text inside.
     *
     * @param text
     *            the text inside the button
     * @see #setText(String)
     */
    public Button(String text) {
        setText(text);
    }

    /**
     * Creates a button with an icon inside.
     *
     * @param icon
     *            the icon inside the button
     * @see #setIcon(Component)
     */
    public Button(Component icon) {
        setIcon(icon);
    }

    /**
     * Creates a button with a text and an icon inside.
     * <p>
     * Use {@link #setIconAfterText(boolean)} to change the order of the text
     * and the icon.
     *
     * @param text
     *            the text inside the button
     * @param icon
     *            the icon inside the button
     * @see #setText(String)
     * @see #setIcon(Component)
     */
    public Button(String text, Component icon) {
        setIcon(icon);
        setText(text);
    }

    /**
     * Creates a button with a text and a listener for click events.
     *
     * @param text
     *            the text inside the button
     * @param clickListener
     *            the event listener for click events
     * @see #setText(String)
     * @see #addClickListener(ComponentEventListener)
     */
    public Button(String text,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        setText(text);
        addClickListener(clickListener);
    }

    /**
     * Creates a button with an icon and a listener for click events.
     *
     * @param icon
     *            the icon inside the button
     * @param clickListener
     *            the event listener for click events
     * @see #setIcon(Component)
     * @see #addClickListener(ComponentEventListener)
     */
    public Button(Component icon,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        setIcon(icon);
        addClickListener(clickListener);
    }

    /**
     * Create a button with a text, an icon and a listener for click events.
     *
     * @param text
     *            the text inside the button
     * @param icon
     *            the icon inside the button
     * @param clickListener
     *            the event listener for click events
     * @see #setText(String)
     * @see #setIcon(Component)
     * @see #addClickListener(ComponentEventListener)
     */
    public Button(String text, Component icon,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        setIcon(icon);
        setText(text);
        addClickListener(clickListener);
    }

    /**
     * Sets the given string as the text content of this component.
     * <p>
     * This method removes any existing text-content and replaces it with the
     * given text.
     * <p>
     * This method also sets or removes this button's <code>theme=icon</code>
     * attribute based on whether this button contains only an icon after this
     * operation or not.
     *
     * @param text
     *            the text content to set, may be <code>null</code> to only
     *            remove existing text
     */
    @Override
    public void setText(String text) {
        removeAll(getNonTextNodes());
        if (text != null && !text.isEmpty()) {
            getElement().appendChild(Element.createText(text));
        }
        updateThemeAttribute();
    }

    /**
     * Sets the given component as the icon of this button.
     * <p>
     * Even though you can use almost any component as an icon, some good
     * options are {@code Icon} and {@link Image}.
     * <p>
     * Use {@link #setIconAfterText(boolean)} to change the icon's position
     * relative to the button's text content.
     * <p>
     * This method also sets or removes this button's <code>theme=icon</code>
     * attribute based on whether this button contains only an icon after this
     * operation or not.
     *
     * @param icon
     *            component to be used as an icon, may be <code>null</code> to
     *            only remove the current icon, can't be a text-node
     */
    public void setIcon(Component icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            remove(iconComponent);
        }
        iconComponent = icon;
        if (icon != null) {
            add(icon);
            updateIconSlot();
        }
        updateThemeAttribute();
    }

    /**
     * Gets the component that is defined as the icon of this button.
     *
     * @return the icon of this button, or <code>null</code> if the icon is not
     *         set
     */
    public Component getIcon() {
        return iconComponent;
    }

    /**
     * Sets whether this button's icon should be positioned after it's text
     * content or the other way around.
     * <p>
     * At the element-level, this method determines whether to set
     * {@code slot="prefix"} or {@code slot="suffix"} attribute to the icon.
     *
     * @param iconAfterText
     *            whether the icon should be positioned after the text content
     *            or not
     */
    public void setIconAfterText(boolean iconAfterText) {
        this.iconAfterText = iconAfterText;
        if (iconComponent != null) {
            updateIconSlot();
        }
    }

    /**
     * Gets whether this button's icon is positioned after it's text content or
     * the other way around.
     *
     * @return <code>true</code> if this button positions it's icon after it's
     *         text content, <code>false</code> otherwise
     */
    public boolean isIconAfterText() {
        return iconAfterText;
    }

    /**
     * Simulates a click on this button on the server side. Calling this method
     * executes all registered click listeners on the server side, but does not
     * execute possible client side registered listeners.
     *
     * @see #clickInClient()
     */
    public void click() {
        fireEvent(new ClickEvent<>(this, false, 0, 0, 0, 0, 0, 0, false, false,
                false, false));
    }

    /**
     * Executes a click on this button at the client-side. Calling this method
     * behaves the same as if the user would have clicked on the button.
     */
    public void clickInClient() {
        getElement().callJsFunction("click");
    }

    /**
     * Adds the given components as children of this component.
     * <p>
     * Note that using this method together with convenience methods, such as
     * {@link #setText(String)} and {@link #setIcon(Component)}, may have
     * unexpected results, e.g. in the order of child elements and the result of
     * {@link #getText()}.
     *
     * @param components
     *            the components to add
     */
    private void add(Component... components) {
        assert components != null;
        for (Component component : components) {
            assert component != null;
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Set the button to be input focused when the page loads.
     *
     * @param autofocus
     *            the boolean value to set
     */
    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Get the state for the auto-focus property of the button.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the button
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    /**
     * Set the button so that it is disabled on click.
     * <p>
     * Enabling the button needs to happen from the server.
     *
     * @param disableOnClick
     *            true to disable button immediately when clicked
     */
    public void setDisableOnClick(boolean disableOnClick) {
        this.disableOnClick = disableOnClick;
        if (disableOnClick) {
            getElement().setAttribute("disableOnClick", "true");
        } else {
            getElement().removeAttribute("disableOnClick");
        }
    }

    /**
     * Get if button is set to be disabled on click.
     *
     * @return {@code true} if button gets disabled on click, else {@code false}
     */
    public boolean isDisableOnClick() {
        return disableOnClick;
    }

    /**
     * Initialize client side disabling so disabled if immediate on click even
     * if server-side handling takes some time.
     */
    private void initDisableOnClick() {
        getElement().executeJs("var disableEvent = function () {"
                + "if($0.getAttribute('disableOnClick')){"
                + " $0.setAttribute('disabled', 'true');" + "}" + "};"
                + "$0.addEventListener('click', disableEvent)");
    }

    private void updateIconSlot() {
        iconComponent.getElement().setAttribute("slot",
                iconAfterText ? "suffix" : "prefix");
    }

    /**
     * Removes all contents from this component except elements in
     * {@code exclusion} array. This includes child components, text content as
     * well as child elements that have been added directly to this component
     * using the {@link Element} API.
     *
     * @see Button#removeAll()
     */
    private void removeAll(Element... exclusion) {
        Set<Element> toExclude = Stream.of(exclusion)
                .collect(Collectors.toSet());
        Predicate<Element> filter = toExclude::contains;
        getElement().getChildren().filter(filter.negate())
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
        getElement().appendChild(exclusion);
    }

    private Element[] getNonTextNodes() {
        return getElement().getChildren()
                .filter(element -> !element.isTextNode())
                .toArray(Element[]::new);
    }

    private void updateThemeAttribute() {
        // Add theme attribute "icon" when the button contains only an icon to
        // fully support themes like Lumo. This doesn't override explicitly set
        // theme attribute.

        if (getElement().getChildCount() == 1 && iconComponent != null) {
            getThemeNames().add("icon");
        } else {
            getThemeNames().remove("icon");
        }
    }

    /*
     * https://github.com/vaadin/vaadin-button-flow/issues/115 because of the
     * latency compensation, we need to hack the "diffstate" for the server side
     * state, so that the disabled value can be reverted during the same
     * roundtrip.
     */
    private void doDisableOnClick() {
        ElementAttributeMap elementAttributeMap = getElement().getNode()
                .getFeature(ElementAttributeMap.class);
        elementAttributeMap.set("disabled", "true");
        Map<NodeFeature, Serializable> changes = getElement().getNode()
                .getChangeTracker(elementAttributeMap, () -> null);
        // Remove the change, if it was applied. It should have been
        // applied unless something else has done the exact same thing already
        // (which is almost impossible, but ...)
        if (changes != null) {
            changes.remove("disabled");
            setEnabled(false);
            getUI().ifPresent(
                    ui -> ui.beforeClientResponse(this, executionContext -> {
                        // in case the disabled status was reverted,
                        // the client might not update the value in
                        // case it was that already
                        if (isEnabled()) {
                            executionContext.getUI().getPage().executeJs(
                                    "$0.disabled = false;", getElement());
                        }
                    }));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        initDisableOnClick();
    }

}
