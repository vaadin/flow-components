/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Category {

    private String name;
    private String caption;
    private List<Demo> demos;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
        caption = toCaption(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<Demo> getDemos() {
        return demos;
    }

    public void setDemos(List<Demo> demos) {
        this.demos = demos;
    }

    private static String toCaption(String categoryName) {
        String camelCased = categoryName
                .replace("and", "And")
                .replace("bar", "Bar")
                .replace("scatter", "Scatter");

        return splitCamelCase(camelCased);
    }

    private static String splitCamelCase(String s) {
        String replaced = s.replaceAll(String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");

        replaced = replaced
                .replaceAll("And", "and")
                .replaceAll("With", "with")
                .replaceAll("To", "to")
                .replaceAll("Of", "of")
                .replaceAll("Api", "API")
                .replaceAll("3 D", "3D");

        return StringUtils.capitalize(replaced);
    }

    public static class Demo {

        private String component;
        private String caption;

        public Demo() {
        }

        public Demo(String component) {
            this.component = component;
            caption = splitCamelCase(component);
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }
}
