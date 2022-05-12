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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;

import java.io.Serializable;

/**
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-combo-box>} is a combo box element combining a dropdown list
 * with an input field for filtering the list of items. If you want to replace
 * the default input field with a custom implementation, you should use the
 * <a href=
 * "#/elements/vaadin-combo-box-light">{@code <vaadin-combo-box-light>}</a>
 * element.
 * </p>
 * <p>
 * Items in the dropdown list must be provided as a list of {@code String}
 * values. Defining the items is done using the {@code items} property, which
 * can be assigned with data-binding, using an attribute or directly with the
 * JavaScript property.
 * </p>
 * <p>
 * &lt;vaadin-combo-box label=&quot;Fruit&quot; items=&quot;[[data]]&quot;&gt;
 * &lt;/vaadin-combo-box&gt;
 * </p>
 * <p>
 * {@code combobox.items = ['apple', 'orange', 'banana'];}
 * </p>
 * <p>
 * When the selected {@code value} is changed, a {@code value-changed} event is
 * triggered.
 * </p>
 * <p>
 * This element can be used within an {@code iron-form}.
 * </p>
 * <h3>Item rendering</h3>
 * <p>
 * {@code <vaadin-combo-box>} supports using custom renderer callback function
 * for defining the content of {@code <vaadin-combo-box-item>}.
 * </p>
 * <p>
 * The renderer function provides {@code root}, {@code comboBox}, {@code model}
 * arguments when applicable. Generate DOM content by using {@code model} object
 * properties if needed, append it to the {@code root} element and control the
 * state of the host element by accessing {@code comboBox}. Before generating
 * new content, users are able to check if there is already content in
 * {@code root} for reusing it.
 * </p>
 * <p>
 * &lt;vaadin-combo-box id=&quot;combo-box&quot;&gt;&lt;/vaadin-combo-box&gt;
 * {@code const comboBox = document.querySelector('#combo-box');comboBox.items =
 * [ 'label': 'Hydrogen', 'value': 'H'}]; comboBox.renderer = function(root,
 * comboBox, model) { root.innerHTML = model.index + ': ' + model.item.label + '
 * ' + '<b>' + model.item.value + '</b>'; };}
 * </p>
 * <p>
 * Renderer is called on the opening of the combo-box and each time the related
 * model is updated. DOM generated during the renderer call can be reused in the
 * next renderer call and will be provided with the {@code root} argument. On
 * first call it will be empty.
 * </p>
 * <h3>Item Template</h3>
 * <p>
 * Alternatively, the content of the {@code <vaadin-combo-box-item>} can be
 * populated by using custom item template provided in the light DOM:
 * </p>
 * <p>
 * &lt;vaadin-combo-box items='[{&quot;label&quot;: &quot;Hydrogen&quot;,
 * &quot;value&quot;: &quot;H&quot;}]'&gt; &lt;template&gt; [[index]]:
 * [[item.label]] &lt;b&gt;[[item.value]&lt;/b&gt; &lt;/template&gt;
 * &lt;/vaadin-combo-box&gt;
 * </p>
 * <p>
 * The following properties are available for item template bindings:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Property name</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code index}</td>
 * <td>Number</td>
 * <td>Index of the item in the {@code items} array</td>
 * </tr>
 * <tr>
 * <td>{@code item}</td>
 * <td>String or Object</td>
 * <td>The item reference</td>
 * </tr>
 * <tr>
 * <td>{@code selected}</td>
 * <td>Boolean</td>
 * <td>True when item is selected</td>
 * </tr>
 * <tr>
 * <td>{@code focused}</td>
 * <td>Boolean</td>
 * <td>True when item is focused</td>
 * </tr>
 * </tbody>
 * </table>
 * <h3>Lazy Loading with Function Data Provider</h3>
 * <p>
 * In addition to assigning an array to the items property, you can
 * alternatively provide the {@code <vaadin-combo-box>} data through the
 * <a href=
 * "#/elements/vaadin-combo-box#property-dataProvider">{@code dataProvider} </a>
 * function property. The {@code <vaadin-combo-box>} calls this function lazily,
 * only when it needs more data to be displayed.
 * </p>
 * <p>
 * See the <a href="#/elements/vaadin-combo-box#property-dataProvider">
 * {@code dataProvider}</a> in the API reference below for the detailed data
 * provider arguments description, and the “Lazy Loading“ example on “Basics”
 * page in the demos.
 * </p>
 * <p>
 * <strong>Note that when using function data providers, the total number of
 * items needs to be set manually. The total number of items can be returned in
 * the second argument of the data provider callback:</strong>
 * </p>
 * <p>
 * {@code javascript comboBox.dataProvider = function(params, callback) var url
 * = 'https://api.example/data' + '?page=' + params.page + // the requested page
 * index '&amp;per_page=' + params.pageSize; // number of items on the page var
 * xhr = new XMLHttpRequest(); xhr.onload = function() { var response =
 * JSON.parse(xhr.responseText); callback( response.employees, // requested page
 * of items response.totalSize // total number of items ); }; xhr.open('GET',
 * url, true); xhr.send(); };}
 * </p>
 * <h3>Styling</h3>
 * <p>
 * The following custom properties are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Custom property</th>
 * <th>Description</th>
 * <th>Default</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code --vaadin-combo-box-overlay-max-height}</td>
 * <td>Property that determines the max height of overlay</td>
 * <td>{@code 65vh}</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * The following shadow DOM parts are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Part name</th>
 * <th>Description</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code text-field}</td>
 * <td>The text field</td>
 * </tr>
 * <tr>
 * <td>{@code toggle-button}</td>
 * <td>The toggle button</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * See <a href=
 * "https://github.com/vaadin/vaadin-overlay/blob/master/src/vaadin-overlay.html"
 * >{@code <vaadin-overlay>} documentation</a> for
 * {@code <vaadin-combo-box-overlay>} parts.
 * </p>
 * <p>
 * See <a href=
 * "https://vaadin.com/components/vaadin-text-field/html-api/elements/Vaadin.TextFieldElement"
 * >{@code <vaadin-text-field>} documentation</a> for the text field parts.
 * </p>
 * <p>
 * The following state attributes are available for styling:
 * </p>
 * <table>
 * <thead>
 * <tr>
 * <th>Attribute</th>
 * <th>Description</th>
 * <th>Part name</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code opened}</td>
 * <td>Set when the combo box dropdown is open</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code disabled}</td>
 * <td>Set to a disabled combo box</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code readonly}</td>
 * <td>Set to a read only combo box</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code has-value}</td>
 * <td>Set when the element has a value</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code invalid}</td>
 * <td>Set when the element is invalid</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code focused}</td>
 * <td>Set when the element is focused</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code focus-ring}</td>
 * <td>Set when the element is keyboard focused</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code loading}</td>
 * <td>Set when new items are expected</td>
 * <td>:host</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * In addition to {@code <vaadin-combo-box>} itself, the following internal
 * components are themable:
 * </p>
 * <ul>
 * <li>{@code <vaadin-text-field>}</li>
 * <li>{@code <vaadin-combo-box-overlay>}</li>
 * <li>{@code <vaadin-combo-box-item>}</li>
 * </ul>
 * <p>
 * Note: the {@code theme} attribute value set on {@code <vaadin-combo-box>} is
 * propagated to the internal themable components listed above.
 * </p>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 */
public abstract class GeneratedVaadinComboBox implements Serializable {

    /**
     * @deprecated Use {@link com.vaadin.flow.component.combobox.events.CustomValueSetEvent} instead. This will be removed in a future major version.
     * @param <TComponent>
     */
    public static class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends com.vaadin.flow.component.combobox.events.CustomValueSetEvent<TComponent> {
        public CustomValueSetEvent(TComponent source, boolean fromClient, String detail) {
            super(source, fromClient, detail);
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a future major version.
     * @param <TComponent>
     */
    public static class SelectedItemChangeEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final JsonObject selectedItem;

        public SelectedItemChangeEvent(TComponent source, boolean fromClient) {
            super(source, fromClient);
            this.selectedItem = (JsonObject) source.getElement().getPropertyRaw("selectedItem");
        }

        public JsonObject getSelectedItem() {
            return selectedItem;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a future major version.
     * @param <TComponent>
     */
    public static class OpenedChangeEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final boolean opened;

        public OpenedChangeEvent(TComponent source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a future major version.
     * @param <R>
     */
    public static class FilterChangeEvent<R extends ComboBoxBase<R, ?, ?>>
            extends ComponentEvent<R> {
        private final String filter;

        public FilterChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.filter = source.getFilter();
        }

        public String getFilter() {
            return filter;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a future major version.
     * @param <R>
     */
    public static class InvalidChangeEvent<R extends ComboBoxBase<R, ?, ?>>
            extends ComponentEvent<R> {
        private final boolean invalid;

        public InvalidChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.invalid = source.isInvalid();
        }

        public boolean isInvalid() {
            return invalid;
        }
    }
}
