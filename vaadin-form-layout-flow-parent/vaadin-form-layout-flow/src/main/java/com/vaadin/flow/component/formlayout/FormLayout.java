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
package com.vaadin.flow.component.formlayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.JsonSerializable;

import com.vaadin.flow.component.html.Label;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Form Layout allows you to build responsive forms with multiple columns and to
 * position input labels on top or to the side of the input. Form Layout has two
 * columns by default meaning it displays two input fields per line. When the
 * layout width is smaller it adjusts to a single column layout.
 * <p>
 * You can define how many columns Form Layout should use based on the screen
 * width. A single column layout is preferable to a multi column one. A multi
 * column layout is more prone to cause confusion and to be misinterpreted by
 * the user. However, closely related fields can be placed in line without
 * issue. For example, first and last name, address fields such as postal code
 * and city, as well as ranged input for dates, time, currency, etc.
 * <p>
 * Best Practices:<br>
 * Longer forms should be split into smaller, more manageable and user-friendly
 * sections using subheadings, Tabs, Details or separate views when possible.
 * Each section should consist of related content and/or fields.
 * <p>
 * Also, use the following guidelines for Button placement in forms:<br>
 * <ul>
 * <li>Buttons should be placed below the form they’re associated with.</li>
 * <li>Buttons should be aligned left.</li>
 * <li>Primary action first, followed by other actions, in order of
 * importance.</li>
 * </ul>
 *
 * @author Vaadin Ltd
 */
public class FormLayout extends GeneratedVaadinFormLayout<FormLayout>
        implements HasSize, HasComponents, ClickNotifier<FormLayout> {

    /**
     * A class used in describing the responsive layouting behavior of a
     * {@link FormLayout}.
     *
     * @author Vaadin Ltd
     */
    public static class ResponsiveStep implements JsonSerializable {

        /**
         * Enum for describing the position of label components in a
         * {@link FormItem}.
         */
        public enum LabelsPosition {

            /**
             * Labels are displayed on the left hand side of the wrapped
             * component.
             */
            ASIDE,

            /**
             * Labels are displayed atop the wrapped component.
             */
            TOP;

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ENGLISH);
            }
        }

        private static final String MIN_WIDTH_JSON_KEY = "minWidth";
        private static final String COLUMNS_JSON_KEY = "columns";
        private static final String LABELS_POSITION_JSON_KEY = "labelsPosition";

        private String minWidth;
        private int columns;
        private LabelsPosition labelsPosition;

        /**
         * Constructs a ResponsiveStep with the given minimum width and number
         * of columns.
         *
         * @param minWidth
         *            the minimum width as a CSS string value after which this
         *            responsive step is to be applied
         * @param columns
         *            the number of columns the layout should have
         */
        public ResponsiveStep(String minWidth, int columns) {
            this.minWidth = minWidth;
            this.columns = columns;
        }

        /**
         * Constructs a ResponsiveStep with the given minimum width, number of
         * columns and label position.
         *
         * @see LabelsPosition
         * @see FormItem
         *
         * @param minWidth
         *            the minimum width as a CSS string value after which this
         *            responsive step is to be applied
         * @param columns
         *            the number of columns the layout should have
         * @param labelsPosition
         *            the position where label components are to be displayed in
         *            {@link FormItem}s
         */
        public ResponsiveStep(String minWidth, int columns,
                LabelsPosition labelsPosition) {
            this.minWidth = minWidth;
            this.columns = columns;
            this.labelsPosition = labelsPosition;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = Json.createObject();
            if (minWidth != null
                    && !minWidth.chars().allMatch(Character::isWhitespace)) {
                json.put(MIN_WIDTH_JSON_KEY, minWidth);
            }
            json.put(COLUMNS_JSON_KEY, columns);
            if (labelsPosition != null) {
                json.put(LABELS_POSITION_JSON_KEY, labelsPosition.toString());
            }
            return json;
        }

        @Override
        public ResponsiveStep readJson(JsonObject value) {
            minWidth = value.getString(MIN_WIDTH_JSON_KEY);
            columns = (int) value.getNumber(COLUMNS_JSON_KEY);
            String labelsPositionString = value
                    .getString(LABELS_POSITION_JSON_KEY);
            if ("aside".equals(labelsPositionString)) {
                labelsPosition = LabelsPosition.ASIDE;
            } else if ("top".equals(labelsPositionString)) {
                labelsPosition = LabelsPosition.TOP;
            } else {
                labelsPosition = null;
            }
            return this;
        }
    }

    /**
     * Server-side component for the {@code <vaadin-form-item>} element. Used to
     * wrap components for display in a {@link FormLayout}.
     *
     * @author Vaadin Ltd
     */
    public static class FormItem extends GeneratedVaadinFormItem<FormItem>
            implements HasComponents {

        /**
         * Constructs an empty FormItem. Components to wrap can be added after
         * construction with {@link #add(Component...)}.
         *
         * @see HasComponents#add(Component...)
         */
        public FormItem() {
        }

        /**
         * Constructs a FormItem with the given initial components to wrap.
         * Additional components can be added after construction with
         * {@link #add(Component...)}.
         *
         * @param components
         *            the initial components to wrap as a form item.
         * @see HasComponents#add(Component...)
         */
        public FormItem(Component... components) {
            add(components);
        }

        @Override
        public void removeAll() {
            super.removeAll();
        }

        @Override
        public void remove(Component... components) {
            super.remove(components);
        }
    }

    /**
     * Constructs an empty layout. Components can be added with
     * {@link #add(Component...)}.
     */
    public FormLayout() {
    }

    /**
     * Constructs a FormLayout with the given initial components. Additional
     * components can be added after construction with
     * {@link #add(Component...)}.
     *
     * @param components
     *            the components to add
     * @see HasComponents#add(Component...)
     */
    public FormLayout(Component... components) {
        add(components);
    }

    /**
     * Sets the colspan of the given component's element. Will default to 1 if
     * an integer lower than 1 is supplied. You can directly add components with
     * the wanted colspan with {@link #add(Component, int)}.
     *
     * @param component
     *            the component to set the colspan for, not {@code null}
     *
     * @param colspan
     *            the desired colspan for the component
     *
     */
    public void setColspan(Component component, int colspan) {
        Objects.requireNonNull(component, "component cannot be null");
        String strColspan = "";
        if (colspan < 1) {
            strColspan = "1";
        } else {
            strColspan = String.valueOf(colspan);
        }
        component.getElement().setAttribute("colspan", strColspan);
    }

    /**
     * Adds a component with the desired colspan. This method is a shorthand for
     * calling {@link #add(Component)} and {@link #setColspan(Component, int)}
     *
     * @param component
     *            the component to add
     *
     * @param colspan
     *            the desired colspan for the component
     *
     */
    public void add(Component component, int colspan) {
        add(component);
        setColspan(component, colspan);

    }

    /**
     * Gets the colspan of the given component. If none is set, returns 1.
     *
     * @param component
     *            the component whose colspan is retrieved
     *
     *
     */
    public int getColspan(Component component) {
        String strColspan = component.getElement().getAttribute("colspan");
        if (strColspan == null) {
            return 1;
            // need this in case the colspan is modified outside the API to an
            // incorrect format somehow.
        } else if (strColspan.matches("\\d+")) {
            return Integer.parseInt(strColspan);
        } else {
            return 1;
        }
    }

    /**
     * Get the list of {@link ResponsiveStep}s used to configure this layout.
     *
     * @see ResponsiveStep
     *
     * @return the list of {@link ResponsiveStep}s used to configure this layout
     */
    public List<ResponsiveStep> getResponsiveSteps() {
        JsonArray stepsJsonArray = (JsonArray) getElement()
                .getPropertyRaw("responsiveSteps");
        if (stepsJsonArray == null) {
            return Collections.emptyList();
        }
        List<ResponsiveStep> steps = new ArrayList<>();
        for (int i = 0; i < stepsJsonArray.length(); i++) {
            steps.add(stepsJsonArray.get(i));
        }
        return steps;
    }

    /**
     * Configure the responsive steps used in this layout.
     *
     * @see ResponsiveStep
     *
     * @param steps
     *            list of {@link ResponsiveStep}s to set
     */
    public void setResponsiveSteps(List<ResponsiveStep> steps) {
        AtomicInteger index = new AtomicInteger();
        getElement().setPropertyJson("responsiveSteps",
                steps.stream().map(ResponsiveStep::toJson).collect(
                        Json::createArray,
                        (arr, value) -> arr.set(index.getAndIncrement(), value),
                        (arr, arrOther) -> {
                            int startIndex = arr.length();
                            for (int i = 0; i < arrOther.length(); i++) {
                                JsonValue value = arrOther.get(i);
                                arr.set(startIndex + i, value);
                            }
                        }));
    }

    /**
     * Configure the responsive steps used in this layout.
     *
     * @see ResponsiveStep
     *
     * @param steps
     *            the {@link ResponsiveStep}s to set
     */
    public void setResponsiveSteps(ResponsiveStep... steps) {
        setResponsiveSteps(Arrays.asList(steps));
    }

    /**
     * Convenience method for creating and adding a new FormItem to this layout
     * that wraps the given field with a label. Shorthand for
     * {@code addFormItem(field, new Label(label))}.
     *
     * @see #addFormItem(Component, Component)
     *
     * @param field
     *            the field component to wrap
     * @param label
     *            the label text to set
     * @return the created form item
     */
    public FormItem addFormItem(Component field, String label) {
        return addFormItem(field, new Label(label));
    }

    /**
     * Convenience method for creating and adding a new FormItem to this layout
     * that wraps the given field with a component as its label.
     *
     * @param field
     *            the field component to wrap
     * @param label
     *            the label component to set
     * @return the created form item
     */
    public FormItem addFormItem(Component field, Component label) {
        FormItem formItem = new FormItem(field);
        formItem.addToLabel(label);
        add(formItem);
        return formItem;
    }
}
