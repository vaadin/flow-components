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
package com.vaadin.flow.component.masterdetaillayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * MasterDetailLayout is a component for building UIs with a master (or primary)
 * area and a detail (or secondary) area that is displayed next to, or overlaid
 * on top of, the master area, depending on configuration and viewport size.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-master-detail-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha3")
@NpmPackage(value = "@vaadin/master-detail-layout", version = "24.8.0-alpha3")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/master-detail-layout/src/vaadin-master-detail-layout.js")
public class MasterDetailLayout extends Component {
}
