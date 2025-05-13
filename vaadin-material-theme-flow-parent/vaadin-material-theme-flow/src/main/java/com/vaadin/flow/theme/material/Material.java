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
package com.vaadin.flow.theme.material;

import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.theme.AbstractTheme;

/**
 * Material component theme class implementation.
 *
 * @deprecated Since 24.7, the Material theme is deprecated and will be removed
 *             in Vaadin 25.
 */
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = "24.8.0-alpha18")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@NpmPackage(value = "@vaadin/vaadin-material-styles", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/vaadin-material-styles/color-global.js")
@JsModule("@vaadin/vaadin-material-styles/typography-global.js")
@Deprecated
public class Material implements AbstractTheme {
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    @Override
    public String getBaseUrl() {
        return "src/";
    }

    @Override
    public String getThemeUrl() {
        return "theme/material/";
    }

    @Override
    public Map<String, String> getHtmlAttributes(String variant) {
        switch (variant) {
        case LIGHT:
            return Collections.singletonMap("theme", LIGHT);
        case DARK:
            return Collections.singletonMap("theme", DARK);
        default:
            if (!variant.isEmpty()) {
                LoggerFactory.getLogger(getClass().getName()).warn(
                        "Material theme variant not recognized: '{}'. Using no variant.",
                        variant);
            }
            return Collections.emptyMap();
        }
    }
}
