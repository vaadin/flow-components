/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

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
@Tag("vaadin-button")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha5")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/button", version = "24.5.0-alpha5")
@JsModule("@vaadin/button/src/vaadin-button.js")
@JsModule("./buttonFunctions.js")
public class Button extends Component
        implements ClickNotifier<Button>, Focusable<Button>, HasAriaLabel,
        HasEnabled, HasPrefix, HasSize, HasStyle, HasSuffix, HasText,
        HasThemeVariant<ButtonVariant>, HasTooltip {

    private Component iconComponent;
    private boolean iconAfterText;
    private boolean disableOnClick = false;
    private PendingJavaScriptResult initDisableOnClick;

    // Register immediately as first listener
    private final Registration disableListener = addClickListener(
            buttonClickEvent -> {
                if (disableOnClick) {
                    setEnabled(false);
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
     * Simulates a click on this button on the server side if it is enabled.
     * Calling this method executes all registered click listeners on the server
     * side, but does not execute possible client side registered listeners.
     *
     * @see #clickInClient()
     */
    public void click() {
        if (isEnabled()) {
            fireEvent(new ClickEvent<>(this, false, 0, 0, 0, 0, 0, 0, false,
                    false, false, false));
        }
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
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
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
        return getElement().getProperty("autofocus", false);
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
            initDisableOnClick();
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
        if (initDisableOnClick == null) {
            initDisableOnClick = getElement().executeJs(
                    "window.Vaadin.Flow.button.initDisableOnClick($0)");
            getElement().getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(this,
                            executionContext -> this.initDisableOnClick = null));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        Focusable.super.setEnabled(enabled);
        // Force updating the disabled state on the client
        // When using disable on click, the client side will immediately
        // run JS to disable the button. If the button is then disabled and
        // re-enabled during the same round trip, Flow will not detect any
        // changes and the client side button would not be enabled again.
        getElement().executeJs("this.disabled = $0", !enabled);
    }

    private void updateIconSlot() {
        iconComponent.getElement().setAttribute("slot",
                iconAfterText ? "suffix" : "prefix");
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            The components to remove.
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component.
     */
    protected void remove(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                component.getElement().removeAttribute("slot");
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
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
        long childCount = getElement().getChildren().filter(
                el -> el.isTextNode() || !"vaadin-tooltip".equals(el.getTag()))
                .count();

        if (childCount == 1 && iconComponent != null) {
            getThemeNames().add("icon");
        } else {
            getThemeNames().remove("icon");
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (isDisableOnClick()) {
            initDisableOnClick();
        }
    }
}
