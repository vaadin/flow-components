/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.theme.lumo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.theme.AbstractTheme;

/**
 * Lumo component theme class implementation.
 */
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = "24.4.20")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.4.20")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "24.4.20")
@JsModule("@vaadin/vaadin-lumo-styles/color-global.js")
@JsModule("@vaadin/vaadin-lumo-styles/typography-global.js")
@JsModule("@vaadin/vaadin-lumo-styles/sizing.js")
@JsModule("@vaadin/vaadin-lumo-styles/spacing.js")
@JsModule("@vaadin/vaadin-lumo-styles/style.js")
@JsModule("@vaadin/vaadin-lumo-styles/vaadin-iconset.js")
public class Lumo implements AbstractTheme {

    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    @Override
    public String getBaseUrl() {
        return "src/";
    }

    @Override
    public String getThemeUrl() {
        return "theme/lumo/";
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
