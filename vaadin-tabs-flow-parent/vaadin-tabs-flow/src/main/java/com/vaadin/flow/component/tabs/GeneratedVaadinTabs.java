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
package com.vaadin.flow.component.tabs;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-tabs>} is a Web Component for easy switching between different
 * views.
 * </p>
 * <p>
 * {@code
<vaadin-tabs selected="4">
<vaadin-tab>Page 1</vaadin-tab>
<vaadin-tab>Page 2</vaadin-tab>
<vaadin-tab>Page 3</vaadin-tab>
<vaadin-tab>Page 4</vaadin-tab>
</vaadin-tabs>}
 * </p>
 * <h3>Styling</h3>
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
 * <td>{@code back-button}</td>
 * <td>Button for moving the scroll back</td>
 * </tr>
 * <tr>
 * <td>{@code tabs}</td>
 * <td>The tabs container</td>
 * </tr>
 * <tr>
 * <td>{@code forward-button}</td>
 * <td>Button for moving the scroll forward</td>
 * </tr>
 * </tbody>
 * </table>
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
 * <td>{@code orientation}</td>
 * <td>Tabs disposition, valid values are {@code horizontal} and
 * {@code vertical}.</td>
 * <td>:host</td>
 * </tr>
 * <tr>
 * <td>{@code overflow}</td>
 * <td>It's set to {@code start}, {@code end}, none or both.</td>
 * <td>:host</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 */
@Tag("vaadin-tabs")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/tabs/src/vaadin-tabs.js")
@NpmPackage(value = "@vaadin/tabs", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-tabs", version = "23.1.0-beta1")
public abstract class GeneratedVaadinTabs<R extends GeneratedVaadinTabs<R>>
        extends Component implements HasStyle, HasTheme {

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(TabsVariant... variants) {
        getThemeNames().addAll(Stream.of(variants)
                .map(TabsVariant::getVariantName).collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(TabsVariant... variants) {
        getThemeNames().removeAll(Stream.of(variants)
                .map(TabsVariant::getVariantName).collect(Collectors.toList()));
    }

    protected void focus() {
        getElement().callJsFunction("focus");
    }
}
