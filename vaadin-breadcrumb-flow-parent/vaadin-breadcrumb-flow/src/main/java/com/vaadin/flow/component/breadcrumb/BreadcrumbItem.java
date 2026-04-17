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
package com.vaadin.flow.component.breadcrumb;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasTooltip;

@Tag("vaadin-breadcrumb-item")
@NpmPackage(value = "@vaadin/breadcrumb", version = "25.2.0-alpha7")
@JsModule("@vaadin/breadcrumb/src/vaadin-breadcrumb-item.js")
public class BreadcrumbItem extends Component
        implements HasPrefix, HasTooltip, HasEnabled {

    public BreadcrumbItem(String label) {
        // placeholder
    }
}
