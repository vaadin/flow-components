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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;

/**
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-button>} is a Web Component providing an accessible and
 * customizable button.
 * </p>
 * <p>
 * &lt;vaadin-button&gt; &lt;/vaadin-button&gt;
 * </p>
 * <p>
 * {@code document.querySelector('vaadin-button').addEventListener('click', () =&gt; alert('Hello World!'));}
 * </p>
 * <h3>Styling</h3>
 * <p>
 * The following shadow DOM parts are exposed for styling:
 * </p>
 * <table>
 * <caption>Shadow DOM parts exposed for styling</caption> <thead>
 * <tr>
 * <th>Part name</th>
 * <th>Description</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code label}</td>
 * <td>The label (text) inside the button</td>
 * </tr>
 * <tr>
 * <td>{@code prefix}</td>
 * <td>A slot for e.g. an icon before the label</td>
 * </tr>
 * <tr>
 * <td>{@code suffix}</td>
 * <td>A slot for e.g. an icon after the label</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * The following attributes are exposed for styling:
 * </p>
 * <table>
 * <caption>Attributes exposed for styling</caption> <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Description</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code active}</td>
 * <td>Set when the button is pressed down, either with mouse, touch or the
 * keyboard.</td>
 * </tr>
 * <tr>
 * <td>{@code disabled}</td>
 * <td>Set when the button is disabled.</td>
 * </tr>
 * <tr>
 * <td>{@code focus-ring}</td>
 * <td>Set when the button is focused using the keyboard.</td>
 * </tr>
 * <tr>
 * <td>{@code focused}</td>
 * <td>Set when the button is focused.</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 *
 * @deprecated since v23.3, generated classes will be removed in v24.
 */
@Deprecated
@Tag("vaadin-button")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.3.11")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/button", version = "23.3.11")
@NpmPackage(value = "@vaadin/vaadin-button", version = "23.3.11")
@JsModule("@vaadin/button/src/vaadin-button.js")
public abstract class GeneratedVaadinButton<R extends GeneratedVaadinButton<R>>
        extends Component implements HasStyle, ClickNotifier<R>, HasText,
        Focusable<R>, HasThemeVariant<ButtonVariant> {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that this control should have input focus when the page loads.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autofocus} property from the webcomponent
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected boolean isAutofocusBoolean() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that this control should have input focus when the page loads.
     * </p>
     *
     * @param autofocus
     *            the boolean value to set
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * If true, the user cannot interact with this element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code disabled} property from the webcomponent
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected boolean isDisabledBoolean() {
        return getElement().getProperty("disabled", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * If true, the user cannot interact with this element.
     * </p>
     *
     * @param disabled
     *            the boolean value to set
     *
     * @deprecated Since 3.0, this API is deprecated in favor of
     *             {@link Button#setEnabled(boolean)}
     */
    @Deprecated
    protected void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * Adds the given components as children of this component at the slot
     * 'prefix'.
     *
     * @param components
     *            The components to add.
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
     *      page about slots</a>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
     *      website about slots</a>
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void addToPrefix(Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", "prefix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Adds the given components as children of this component at the slot
     * 'suffix'.
     *
     * @param components
     *            The components to add.
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
     *      page about slots</a>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
     *      website about slots</a>
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void addToSuffix(Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", "suffix");
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            The components to remove.
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component.
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
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
     * Removes all contents from this component, this includes child components,
     * text content as well as child elements that have been added directly to
     * this component using the {@link Element} API.
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void removeAll() {
        getElement().getChildren()
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, will be removed with the method in v24
    @Override
    @Deprecated
    public void addThemeVariants(ButtonVariant... variants) {
        HasThemeVariant.super.addThemeVariants(variants);
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, will be removed with the method in v24
    @Override
    @Deprecated
    public void removeThemeVariants(ButtonVariant... variants) {
        HasThemeVariant.super.removeThemeVariants(variants);
    }

    /**
     * Sets the given string as the content of this component.
     *
     * @param text
     *            the text content to set
     * @see HasText#setText(String)
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public GeneratedVaadinButton(String text) {
        setText(text);
    }

    /**
     * Default constructor.
     *
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    public GeneratedVaadinButton() {
    }
}
