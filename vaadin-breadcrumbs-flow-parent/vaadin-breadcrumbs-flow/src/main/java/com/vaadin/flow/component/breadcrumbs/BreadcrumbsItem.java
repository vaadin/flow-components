/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.breadcrumbs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;

/**
 * An item of the {@link Breadcrumbs} component, representing a single entry in
 * the navigation trail.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-breadcrumbs-item")
@NpmPackage(value = "@vaadin/breadcrumbs", version = "25.2.0-rc2")
@JsModule("@vaadin/breadcrumbs/src/vaadin-breadcrumbs-item.js")
public class BreadcrumbsItem extends Component
        implements HasText, HasEnabled, HasPrefix {

    /**
     * Creates a breadcrumbs item with the given text.
     *
     * @param text
     *            the text of the item
     */
    public BreadcrumbsItem(String text) {
        setText(text);
    }
}
