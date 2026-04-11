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
package com.vaadin.flow.component.geolocation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Geo Location is a button component that asks the browser for the user's
 * current position and exposes the result as a strongly-typed Java value.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-geo-location")
@NpmPackage(value = "@vaadin/geo-location", version = "25.2.0-alpha6")
@JsModule("@vaadin/geo-location/src/vaadin-geo-location.js")
public class GeoLocation extends Component implements HasSize {

    /**
     * Creates a new {@link GeoLocation}.
     */
    public GeoLocation() {
    }
}
