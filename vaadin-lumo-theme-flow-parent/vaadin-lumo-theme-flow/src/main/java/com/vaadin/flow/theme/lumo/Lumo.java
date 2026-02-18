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
package com.vaadin.flow.theme.lumo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.AbstractTheme;

/**
 * Lumo component theme class implementation.
 */
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = "25.1.0-alpha8")
@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "25.1.0-alpha8")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class Lumo implements AbstractTheme {

    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    /**
     * The path to the Lumo stylesheet. Can be used as argument to a
     * {@link StyleSheet} on an {@link AppShellConfigurator} class to apply the
     * Lumo theme to an application.
     */
    public static final String STYLESHEET = "lumo/lumo.css";

    /**
     * The path to the stylesheet that contains the Lumo utility classes. Can be
     * used as argument to a {@link StyleSheet} on an
     * {@link AppShellConfigurator} class to apply the utility classes to an
     * application.
     */
    public static final String UTILITY_STYLESHEET = "lumo/utility.css";

    /**
     * The path to the stylesheet that contains the Lumo compact preset. Can be
     * used as argument to a {@link StyleSheet} on an
     * {@link AppShellConfigurator} class to apply the compact preset to an
     * application.
     * <p>
     * The compact preset needs to be loaded in addition to the main Lumo
     * stylesheet referenced by {@link #STYLESHEET}, not instead of it. Make
     * sure to load the compact preset after the main Lumo stylesheet so that it
     * can override the relevant CSS custom properties.
     */
    public static final String COMPACT_STYLESHEET = "lumo/presets/compact.css";

    @Override
    public String getBaseUrl() {
        return "src/";
    }

    @Override
    public String getThemeUrl() {
        return "src/";
    }

    @Override
    public Map<String, String> getHtmlAttributes(String variant) {
        if (variant.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> attributes = new HashMap<>(1);
        switch (variant) {
        case LIGHT:
            attributes.put("theme", LIGHT);
            break;
        case DARK:
            attributes.put("theme", DARK);
            break;
        default:
            LoggerFactory.getLogger(Lumo.class.getName()).warn(
                    "Lumo theme variant not recognized: '{}'. Using no variant.",
                    variant);
        }
        return attributes;
    }

}
