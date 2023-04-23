/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.I18NProvider;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a common single interface for input fields {@link Component
 * components} based on an {@link HasElement element} that supports
 * {@link HasLabel label}, {@link HasStyle styles}, {@link HasTooltip tooltip},
 * {@link HasEnabled enabled status} and {@link HasSize size} definition.
 *
 * @author Vaadin Ltd
 * @since 24.1
 */
public interface InputField extends HasEnabled, HasLabel, HasSize,
        HasStyle, HasTooltip {

    /**
     * Traverses the component tree up and returns the first ancestor component
     * that matches the given type.
     *
     * @param componentType
     *            the class of the ancestor component to search for
     * @return The first ancestor that can be assigned to the given class. Null
     *         if no ancestor with the correct type could be found.
     * @param <T>
     *            the type of the ancestor component to return
     */
    public <T> T findAncestor(Class<T> componentType);

    /**
     * Gets the child components of this component.
     * <p>
     * The default implementation finds child components by traversing each
     * child {@link Element} tree.
     * <p>
     * If the component is injected to a PolymerTemplate using the
     * <code>@Id</code> annotation the getChildren method will only return
     * children added from the server side and will not return any children
     * declared in the template file.
     *
     * @see Id
     *
     * @return the child components of this component
     */
    public Stream<Component> getChildren();

    /**
     * Gets the id of the root element of this component.
     *
     * @see #setId(String)
     *
     * @return the id, or and empty optional if no id has been set
     */
    public Optional<String> getId();

    /**
     * Gets the parent component of this component.
     * <p>
     * A component can only have one parent.
     *
     * @return an optional parent component, or an empty optional if the
     *         component is not attached to a parent
     */
    public Optional<Component> getParent();

    /**
     * Get the translation for key with given locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     * @param locale
     *            locale to use
     * @param key
     *            translation key
     * @param params
     *            parameters used in translation string
     * @return translation for key if found
     */
    public String getTranslation(Locale locale, Object key, Object... params);

    /**
     * Get the translation for key with given locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     * @param locale
     *            locale to use
     * @param key
     *            translation key
     * @param params
     *            parameters used in translation string
     * @return translation for key if found
     */
    public String getTranslation(Locale locale, String key, Object... params);

    /**
     * Get the translation for key with given locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     * @param key
     *            translation key
     * @param locale
     *            locale to use
     * @param params
     *            parameters used in translation string
     * @return translation for key if found
     * @deprecated Use {@link #getTranslation(Locale, String, Object...)}
     *             instead
     */
    @Deprecated
    public String getTranslation(Object key, Locale locale, Object... params);

    /**
     * Get the translation for the component locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     *
     * @param key
     *            translation key
     * @param params
     *            parameters used in translation string
     * @return translation for key if found (implementation should not return
     *         null)
     */
    public String getTranslation(Object key, Object... params);

    /**
     * Get the translation for key with given locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     * @param key
     *            translation key
     * @param locale
     *            locale to use
     * @param params
     *            parameters used in translation string
     * @return translation for key if found
     * @deprecated Use {@link #getTranslation(Locale, String, Object...)}
     *             instead
     */
    @Deprecated
    public String getTranslation(String key, Locale locale, Object... params);

    /**
     * Get the translation for the component locale.
     * <p>
     * The method never returns a null. If there is no {@link I18NProvider}
     * available or no translation for the {@code key} it returns an exception
     * string e.g. '!{key}!'.
     *
     *
     * @param key
     *            translation key
     * @param params
     *            parameters used in translation string
     * @return translation for key if found (implementation should not return
     *         null)
     */
    public String getTranslation(String key, Object... params);

    /**
     * Gets the UI this component is attached to.
     *
     * @return an optional UI component, or an empty optional if this component
     *         is not attached to a UI
     */
    public Optional<UI> getUI();

    /**
     * Checks whether this component is currently attached to a UI.
     * <p>
     * When {@link UI#close()} is called, the UI and the components are not
     * detached immediately; the UI cleanup is performed at the end of the
     * current request which also detaches the UI and its components.
     *
     * @return true if the component is attached to an active UI.
     */
    public boolean isAttached();

    /**
     * Gets the component visibility value.
     *
     * @return {@code true} if the component is visible, {@code false} otherwise
     */
    public boolean isVisible();

    /**
     * Handle component enable state when the enabled state changes.
     * <p>
     * By default this sets or removes the 'disabled' attribute from the
     * element. This can be overridden to have custom handling.
     *
     * @param enabled
     *            the new enabled state of the component
     */
    public void onEnabledStateChanged(boolean enabled);

    /**
     * Removes the component from its parent.
     */
    public void removeFromParent();

    /**
     * Scrolls the current component into the visible area of the browser
     * window.
     */
    public void scrollIntoView();

    /**
     * Scrolls the current component into the visible area of the browser
     * window.
     *
     * @param scrollOptions
     *            options to define the scrolling behavior
     */
    public void scrollIntoView(ScrollOptions scrollOptions);

    /**
     * Sets the id of the root element of this component. The id is used with
     * various APIs to identify the element, and it should be unique on the
     * page.
     *
     * @param id
     *            the id to set, or <code>""</code> to remove any previously set
     *            id
     */
    public void setId(String id);

    /**
     * Gets the component visibility value.
     *
     * @return {@code true} if the component is visible, {@code false} otherwise
     */
    public void setVisible(boolean visible);

}
