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
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.JsonSerializable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;

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
 * <h2>Auto Responsive Mode</h2>
 * <p>
 * To avoid manually dealing with responsive breakpoints, Form Layout provides
 * an auto-responsive mode that automatically creates and adjusts fixed-width
 * columns based on the container's available space.
 * <p>
 * To control the number of columns and their widths, you can use the following
 * properties:
 * <ul>
 * <li>{@link #setColumnWidth(String) columnWidth} - controls the column width
 * (13em by default).</li>
 * <li>{@link #setMaxColumns(int) maxColumns} - controls the maximum number of
 * columns that the layout can create (10 by default).</li>
 * <li>{@link #setMinColumns(int) minColumns} - controls the minimum number of
 * columns that the layout will create (1 by default).</li>
 * </ul>
 * <p>
 * The auto-responsive mode is disabled by default. To enable it for an
 * individual instance, set the {@link #setAutoResponsive(boolean)
 * autoResponsive} property to {@code true}:
 *
 * <pre>
 * FormLayout formLayout = new FormLayout();
 * formLayout.setAutoResponsive(true);
 * formLayout.add(new TextField("First name"), new TextField("Last name"));
 * formLayout.add(new TextArea("Address"), 2); // colspan 2
 * </pre>
 *
 * <p>
 * You can also enable it for all instances by enabling the following feature
 * flag in {@code src/main/resources/vaadin-featureflags.properties}:
 *
 * <pre>
 * com.vaadin.experimental.defaultAutoResponsiveFormLayout = true
 * </pre>
 *
 * <h3>Organizing Fields into Rows</h3>
 * <p>
 * By default, each field is placed on a new row. To organize fields into rows,
 * you can either:
 * <ul>
 * <li>Manually wrap fields into {@link FormRow} elements.
 * <li>Enable the {@link #setAutoRows(boolean) autoRows} property to let Form
 * Layout automatically arrange fields in available columns, wrapping to a new
 * row when necessary. HTML {@link ElementFactory#createBr() br} elements can be
 * used to force a new row.
 * </ul>
 * <p>
 * Here is an example of using {@link FormRow}:
 *
 * <pre>
 * FormLayout formLayout = new FormLayout();
 * formLayout.setAutoResponsive(true);
 *
 * FormRow firstRow = new FormRow();
 * firstRow.add(new TextField("First name"), new TextField("Last name"));
 *
 * FormRow secondRow = new FormRow();
 * secondRow.add(new TextArea("Address"), 2); // colspan 2
 *
 * formLayout.add(firstRow, secondRow);
 * </pre>
 *
 * <h3>Expanding Columns and Fields</h3>
 * <p>
 * You can configure Form Layout to expand columns to evenly fill any remaining
 * space after all fixed-width columns have been created. To enable this, set
 * the {@link #setExpandColumns(boolean) expandColumns} property to
 * {@code true}.
 * <p>
 * Also, Form Layout can stretch fields to make them take up all available space
 * within columns. To enable this, set the {@link #setExpandFields(boolean)
 * expandFields} property to {@code true}.
 *
 * <h3>Customizing Label Position</h3>
 * <p>
 * By default, Form Layout displays labels above the fields. To position labels
 * beside fields, you need to wrap each field in a {@link FormItem} element and
 * define its labels on the wrapper. Then, you can enable the
 * {@link #setLabelsAside(boolean) labelsAside} property:
 *
 * <pre>
 * FormLayout formLayout = new FormLayout();
 * formLayout.setAutoResponsive(true);
 * formLayout.setLabelsAside(true);
 *
 * FormRow firstRow = new FormRow();
 * firstRow.addFormItem(new TextField(), "First Name");
 * firstRow.addFormItem(new TextField(), "Last Name");
 *
 * FormRow secondRow = new FormRow();
 * FormItem addressField = secondRow.addFormItem(new TextArea(), "Address");
 * secondRow.setColspan(addressField, 2);
 *
 * formLayout.add(firstRow, secondRow);
 * </pre>
 * <p>
 * With this, FormLayout will display labels beside fields, falling back to the
 * default position above the fields only when there isn't enough space.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-form-layout")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/form-layout", version = "24.8.0-alpha18")
@JsModule("@vaadin/form-layout/src/vaadin-form-layout.js")
public class FormLayout extends Component
        implements HasSize, HasStyle, HasComponents, ClickNotifier<FormLayout> {

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
            if (minWidth != null && !minWidth.trim().isEmpty()) {
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
            JsonValue minWidthValue = value.get(MIN_WIDTH_JSON_KEY);
            if (minWidthValue != null) {
                minWidth = minWidthValue.asString();
            } else {
                minWidth = null;
            }

            columns = (int) value.getNumber(COLUMNS_JSON_KEY);

            JsonValue labelsPositionValue = value.get(LABELS_POSITION_JSON_KEY);
            if (labelsPositionValue != null) {
                String labelsPositionString = labelsPositionValue.asString();
                if ("aside".equals(labelsPositionString)) {
                    labelsPosition = LabelsPosition.ASIDE;
                } else if ("top".equals(labelsPositionString)) {
                    labelsPosition = LabelsPosition.TOP;
                }
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
    @Tag("vaadin-form-item")
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
    @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
    @NpmPackage(value = "@vaadin/form-layout", version = "24.8.0-alpha18")
    @JsModule("@vaadin/form-layout/src/vaadin-form-item.js")
    public static class FormItem extends Component
            implements HasComponents, HasStyle, ClickNotifier<FormItem> {

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

        /**
         * Adds the given components as children of this component at the slot
         * 'label'.
         *
         * @param components
         *            The components to add.
         * @see <a href=
         *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/slot">MDN
         *      page about slots</a>
         * @see <a href=
         *      "https://html.spec.whatwg.org/multipage/scripting.html#the-slot-element">Spec
         *      website about slots</a>
         */
        protected void addToLabel(Component... components) {
            SlotUtils.addToSlot(this, "label", components);
        }

        /**
         * Removes all contents from this component, this includes child
         * components, text content as well as child elements that have been
         * added directly to this component using the {@link Element} API.
         */
        public void removeAll() {
            getElement().getChildren()
                    .forEach(child -> child.removeAttribute("slot"));
            getElement().removeAllChildren();
        }

        /**
         * Removes the given child components from this component.
         *
         * @param components
         *            The components to remove.
         * @throws IllegalArgumentException
         *             if any of the components is not a child of this
         *             component.
         */
        public void remove(Component... components) {
            for (Component component : components) {
                if (getElement().equals(component.getElement().getParent())) {
                    component.getElement().removeAttribute("slot");
                    getElement().removeChild(component.getElement());
                } else {
                    throw new IllegalArgumentException("The given component ("
                            + component + ") is not a child of this component");
                }
            }
        }
    }

    /**
     * Server-side component for the {@code <vaadin-form-row>} element. Used to
     * arrange fields into rows inside a {@link FormLayout} when
     * {@link FormLayout#setAutoResponsive(boolean) auto-responsive mode} is
     * enabled.
     * <p>
     * Each FormRow always starts on a new row. Fields that exceed the available
     * columns wrap to a new row, which then remains reserved exclusively for
     * the fields of that FormRow.
     * <p>
     * Example of creating a FormRow with two fields and a single field that
     * spans two columns:
     *
     * <pre>
     * FormLayout formLayout = new FormLayout();
     * formLayout.setAutoResponsive(true);
     *
     * FormRow firstRow = new FormRow();
     * firstRow.add(new TextField("First name"), new TextField("Last name"));
     *
     * FormRow secondRow = new FormRow();
     * secondRow.add(new TextArea("Address"), 2); // colspan 2
     *
     * formLayout.add(firstRow, secondRow);
     * </pre>
     *
     * @author Vaadin Ltd
     */
    @Tag("vaadin-form-row")
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
    @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
    @NpmPackage(value = "@vaadin/form-layout", version = "24.8.0-alpha18")
    @JsModule("@vaadin/form-layout/src/vaadin-form-row.js")
    public static class FormRow extends Component implements HasComponents {

        /**
         * Constructs an empty FormRow. Components to wrap can be added after
         * construction with {@link #add(Component...)}, or by using the
         * {@link #addFormItem(Component, String)} and
         * {@link #addFormItem(Component, Component)} methods.
         *
         * @see HasComponents#add(Component...)
         */
        public FormRow() {
        }

        /**
         * Adds a component with the desired colspan. This method is a shorthand
         * for calling {@link #add(Component...)} and
         * {@link #setColspan(Component, int)}
         *
         * @param component
         *            the component to add
         * @param colspan
         *            the desired colspan for the component
         */
        public void add(Component component, int colspan) {
            add(component);
            setColspan(component, colspan);
        }

        /**
         * Sets the colspan of the given component's element. Will default to 1
         * if an integer lower than 1 is supplied. You can directly add
         * components with the wanted colspan with {@link #add(Component, int)}.
         *
         * @param component
         *            the component to set the colspan for, not {@code null}
         * @param colspan
         *            the desired colspan for the component
         */
        public void setColspan(Component component, int colspan) {
            Objects.requireNonNull(component, "component cannot be null");
            component.getElement().setAttribute("colspan",
                    String.valueOf(Math.max(1, colspan)));
        }

        /**
         * Gets the colspan of the given component. If none is set, returns 1.
         *
         * @param component
         *            the component whose colspan is retrieved
         * @return the colspan of the given component or 1 if none is set
         */
        public int getColspan(Component component) {
            String colspan = component.getElement().getAttribute("colspan");
            if (colspan != null && colspan.matches("\\d+")) {
                return Integer.parseInt(colspan);
            } else {
                return 1;
            }
        }

        /**
         * Creates a new {@link FormItem} with the given component and the label
         * string, and adds it to the form row. The label is inserted into the
         * form item as a {@link NativeLabel}.
         *
         * @param field
         *            the field component to be wrapped in a form item
         * @param label
         *            the label text to be displayed
         *
         * @return the created form item
         */
        public FormItem addFormItem(Component field, String label) {
            return addFormItem(field, new NativeLabel(label));
        }

        /**
         * Creates a new {@link FormItem} with the given field and label
         * components and adds it to the form row.
         *
         * @param field
         *            the field component to be wrapped in a form item
         * @param label
         *            the label component to be displayed
         *
         * @return the created form item
         */
        public FormItem addFormItem(Component field, Component label) {
            FormItem formItem = new FormItem(field);
            formItem.addToLabel(label);
            add(formItem);
            return formItem;
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
        component.getElement().setAttribute("colspan",
                String.valueOf(Math.max(1, colspan)));
    }

    /**
     * Adds a component with the desired colspan. This method is a shorthand for
     * calling {@link #add(Component...)} and
     * {@link #setColspan(Component, int)}
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
     * @return the colspan of the given component or 1 if none is set
     */
    public int getColspan(Component component) {
        String colspan = component.getElement().getAttribute("colspan");
        if (colspan != null && colspan.matches("\\d+")) {
            return Integer.parseInt(colspan);
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
            steps.add(new ResponsiveStep(null, 0)
                    .readJson(stepsJsonArray.get(i)));
        }
        return steps;
    }

    /**
     * Configure the responsive steps used in this layout.
     * <p>
     * NOTE: Responsive steps are ignored in auto-responsive mode, which may be
     * enabled explicitly via {@link #setAutoResponsive(boolean)} or implicitly
     * if the following feature flag is set in
     * {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * com.vaadin.experimental.defaultAutoResponsiveFormLayout = true
     * </pre>
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
     * <p>
     * NOTE: Responsive steps are ignored in auto-responsive mode, which may be
     * enabled explicitly via {@link #setAutoResponsive(boolean)} or implicitly
     * if the following feature flag is set in
     * {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * com.vaadin.experimental.defaultAutoResponsiveFormLayout = true
     * </pre>
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
        return addFormItem(field, new NativeLabel(label));
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

    /**
     * Convenience method dor creating and adding a new {@link FormRow} to this
     * layout. The method accepts a list of components that will be added to the
     * row.
     *
     * @param components
     *            the components to add to the row
     * @return the created form row
     */
    public FormRow addFormRow(Component... components) {
        FormRow formRow = new FormRow();
        formRow.add(components);
        add(formRow);
        return formRow;
    }

    /**
     * Sets the width of side-positioned label.
     *
     * @param width
     *            the value and CSS unit as a string
     * @see <a href=
     *      "https://vaadin.com/docs/latest/components/form-layout#label-position">Label
     *      position</a>
     */
    public void setLabelWidth(String width) {
        getStyle().set("--vaadin-form-layout-label-width", width);
    }

    /**
     * Sets the width of side-positioned label.
     *
     * @param width
     *            the value of the width
     * @param unit
     *            the CSS unit of the width
     * @see #setLabelWidth(String)
     */
    public void setLabelWidth(float width, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        setLabelWidth(width + unit.toString());
    }

    /**
     * Gets the width of side-positioned label.
     *
     * @return the value and CSS unit as a string
     * @see <a href=
     *      "https://vaadin.com/docs/latest/components/form-layout#label-position">Label
     *      position</a>
     */
    public String getLabelWidth() {
        return getStyle().get("--vaadin-form-layout-label-width");
    }

    /**
     * Sets the gap between the label and the field which is used when labels
     * are positioned aside. The value must be provided in CSS length units,
     * e.g. {@code 1em}.
     *
     * @param labelSpacing
     *            the gap between the label and the field
     * @see #setLabelSpacing(float, Unit)
     */
    public void setLabelSpacing(String labelSpacing) {
        getStyle().set("--vaadin-form-layout-label-spacing", labelSpacing);
    }

    /**
     * Sets the gap between the label and the field which is used when labels
     * are positioned aside. The value must be provided with a {@link Unit},
     * e.g., {@code 1} and {@link Unit#EM}.
     *
     * @param labelSpacing
     *            the gap between the label and the field
     * @param unit
     *            the CSS unit of the gap
     * @see #setLabelSpacing(String)
     */
    public void setLabelSpacing(float labelSpacing, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        setLabelSpacing(labelSpacing + unit.toString());
    }

    /**
     * Gets the gap between the label and the field which is used when labels
     * are positioned aside.
     *
     * @return the value and CSS unit as a string
     * @see #setLabelSpacing(String)
     * @see #setLabelSpacing(float, Unit)
     */
    public String getLabelSpacing() {
        return getStyle().get("--vaadin-form-layout-label-spacing");
    }

    /**
     * Sets the gap between the columns. The value must be provided in CSS
     * length units, e.g. {@code 1em}.
     *
     * @param columnSpacing
     *            the gap between the columns
     * @see #setColumnSpacing(float, Unit)
     */
    public void setColumnSpacing(String columnSpacing) {
        getStyle().set("--vaadin-form-layout-column-spacing", columnSpacing);
    }

    /**
     * Sets the gap between the columns. The value must be provided with a
     * {@link Unit}, e.g. {@code 1} and {@link Unit#EM}.
     *
     * @param columnSpacing
     *            the gap between the columns
     * @param unit
     *            the CSS unit of the gap
     * @see #setColumnSpacing(String)
     */
    public void setColumnSpacing(float columnSpacing, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        setColumnSpacing(columnSpacing + unit.toString());
    }

    /**
     * Gets the gap between the columns.
     *
     * @return the value and CSS unit as a string
     * @see #setColumnSpacing(String)
     * @see #setColumnSpacing(float, Unit)
     */
    public String getColumnSpacing() {
        return getStyle().get("--vaadin-form-layout-column-spacing");
    }

    /**
     * Sets the gap between the rows. The value must be provided in CSS length
     * units, e.g. {@code 1em}.
     *
     * @param rowSpacing
     *            the gap between the rows
     * @see #setRowSpacing(float, Unit)
     */
    public void setRowSpacing(String rowSpacing) {
        getStyle().set("--vaadin-form-layout-row-spacing", rowSpacing);
    }

    /**
     * Sets the gap between the rows. The value must be provided with a
     * {@link Unit}, e.g., {@code 1} and {@link Unit#EM}.
     *
     * @param rowSpacing
     *            the gap between the rows
     * @param unit
     *            the CSS unit of the gap
     * @see #setRowSpacing(String)
     */
    public void setRowSpacing(float rowSpacing, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        setRowSpacing(rowSpacing + unit.toString());
    }

    /**
     * Gets the gap between the rows.
     *
     * @return the value and CSS unit as a string
     * @see #setRowSpacing(String)
     * @see #setRowSpacing(float, Unit)
     */
    public String getRowSpacing() {
        return getStyle().get("--vaadin-form-layout-row-spacing");
    }

    /**
     * When set to {@code true}, the component automatically creates and adjusts
     * columns based on the container's width. Columns have a fixed width
     * defined by {@link #setColumnWidth(String)}. The number of columns
     * increases up to the limit set by {@link #setMaxColumns(int)}. The minimum
     * number of columns will be created is set by {@link #setMinColumns(int)}.
     * The component dynamically adjusts the number of columns as the container
     * size changes. When this mode is enabled, {@link ResponsiveStep Responsive
     * steps} are ignored.
     * <p>
     * By default, each field is placed on a new row. To organize fields into
     * rows, there are two options:
     * <ol>
     * <li>Use {@link FormRow} to explicitly group fields into rows.
     * <li>Enable the {@link #setAutoRows(boolean)} property to automatically
     * arrange fields in available columns, wrapping to a new row when
     * necessary. {@link ElementFactory#createBr()} elements can be used to
     * force a new row.
     * </ol>
     * <p>
     * The auto-responsive mode is disabled by default. To enable it for an
     * individual instance, use this method. Alternatively, if you want it to be
     * enabled for all instances by default, enable the following feature flag
     * in {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * com.vaadin.experimental.defaultAutoResponsiveFormLayout = true
     * </pre>
     *
     * @param autoResponsive
     *            {@code true} to enable auto responsive mode, {@code false} to
     *            disable
     */
    public void setAutoResponsive(boolean autoResponsive) {
        getElement().setProperty("autoResponsive", autoResponsive);
    }

    /**
     * Sets whether the component should automatically distribute fields across
     * columns by placing each field in the next available column and wrapping
     * to the next row when the current row is full.
     * {@link ElementFactory#createBr()} elements can be used to force a new
     * row.
     * <p>
     * The default value is {@code false}.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param autoRows
     *            {@code true} to enable auto rows mode, {@code false} otherwise
     */
    public void setAutoRows(boolean autoRows) {
        getElement().setProperty("autoRows", autoRows);
    }

    /**
     * Gets whether the component is configured to automatically distribute
     * fields across columns when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @return {@code true} if auto rows mode is enabled, {@code false}
     *         otherwise
     * @see #setAutoRows(boolean)
     */
    public boolean isAutoRows() {
        return getElement().getProperty("autoRows", false);
    }

    /**
     * Sets the width of columns that the component should use when
     * {@link #setAutoResponsive(boolean)} is enabled. The value must be
     * provided in CSS length units, e.g. {@code 100px}.
     * <p>
     * When the column width is {@code null}, the web component defaults to
     * {@code 12em} or uses the value of {@code --vaadin-field-default-width} if
     * that CSS custom property is defined.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param columnWidth
     *            the width of columns or {@code null} to use the default
     */
    public void setColumnWidth(String columnWidth) {
        getElement().setProperty("columnWidth", columnWidth);
    }

    /**
     * Sets the width of columns that the component should use when
     * {@link #setAutoResponsive(boolean)} is enabled. The value must be
     * provided with a {@link Unit}, e.g. {@code 100} and {@link Unit#PIXELS}.
     * <p>
     * When the column width is {@code null}, the web component defaults to
     * {@code 12em} or uses the value of {@code --vaadin-field-default-width} if
     * that CSS custom property is defined.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param columnWidth
     *            the width of columns
     * @param unit
     *            the CSS unit of the width
     */
    public void setColumnWidth(float columnWidth, Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");
        setColumnWidth(columnWidth + unit.toString());
    }

    /**
     * Gets the width of columns that is used when
     * {@link #setAutoResponsive(boolean)} is enabled.
     * <p>
     * When the column width is {@code null}, the web component defaults to
     * {@code 12em} or uses the value of {@code --vaadin-field-default-width} if
     * that CSS custom property is defined.
     *
     * @return the value and CSS unit as a string, or {@code null} if not set,
     *         in which case the web component uses its default value
     * @see #setColumnWidth(String)
     * @see #setColumnWidth(float, Unit)
     */
    public String getColumnWidth() {
        return getElement().getProperty("columnWidth");
    }

    /**
     * Sets the maximum number of columns that the component can create. The
     * component will create columns up to this limit based on the available
     * container width.
     * <p>
     * By default, the web component uses a maximum of 10 columns.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param maxColumns
     *            the maximum number of columns
     */
    public void setMaxColumns(int maxColumns) {
        getElement().setProperty("maxColumns", maxColumns);
    }

    /**
     * Gets the maximum number of columns that the component can create when
     * {@code #setAutoResponsive(boolean)} is enabled.
     *
     * @return the maximum number of columns or 0 if not explicitly set
     * @see #setMaxColumns(int)
     */
    public int getMaxColumns() {
        return getElement().getProperty("maxColumns", 0);
    }

    /**
     * Sets the minimum number of columns that the component will create.
     * <p>
     * By default, the web component uses a minimum of 1 column.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param minColumns
     *            the minimum number of columns
     */
    public void setMinColumns(int minColumns) {
        getElement().setProperty("minColumns", minColumns);
    }

    /**
     * Gets the minimum number of columns that the component can create when
     * {@code #setAutoResponsive(boolean)} is enabled.
     *
     * @return the minimum number of columns or 0 if not explicitly set
     * @see #setMinColumns(int)
     */
    public int getMinColumns() {
        return getElement().getProperty("minColumns", 0);
    }

    /**
     * Sets whether the columns should evenly expand in width to fill any
     * remaining space after all columns have been created.
     * <p>
     * The default value is {@code false}.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param expandColumns
     *            {@code true} to expand columns, {@code false} otherwise
     */
    public void setExpandColumns(boolean expandColumns) {
        getElement().setProperty("expandColumns", expandColumns);
    }

    /**
     * Gets whether columns are configured to expand to fill remaining space
     * when {@link #setAutoResponsive(boolean)} is enabled.
     *
     * @return {@code true} if columns should expand, {@code false} otherwise
     * @see #setExpandColumns(boolean)
     */
    public boolean isExpandColumns() {
        return getElement().getProperty("expandColumns", false);
    }

    /**
     * Sets whether fields should stretch to take up all available space within
     * columns. Fields inside {@link FormItem} elements are also included.
     * <p>
     * The default value is {@code false}.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     *
     * @param expandFields
     *            {@code true} to expand fields, {@code false} otherwise
     */
    public void setExpandFields(boolean expandFields) {
        getElement().setProperty("expandFields", expandFields);
    }

    /**
     * Gets whether fields are configured to stretch to take up all available
     * space within columns when {@link #setAutoResponsive(boolean)} is enabled.
     *
     * @return {@code true} if fields should expand, {@code false} otherwise
     * @see #setExpandFields(boolean)
     */
    public boolean isExpandFields() {
        return getElement().getProperty("expandFields", false);
    }

    /**
     * Sets whether {@link FormItem} should prefer positioning labels beside the
     * fields. If the layout is too narrow to fit a single column with a side
     * label, labels will automatically switch to their default position above
     * the fields until the layout gets wide again.
     * <p>
     * This setting only applies when {@link #setAutoResponsive(boolean)} is
     * enabled.
     * <p>
     * To customize the label width and the gap between the label and the field,
     * use the following methods:
     * <ul>
     * <li>{@link #setLabelWidth(String)}</li>
     * <li>{@link #setLabelSpacing(String)}</li>
     * </ul>
     * <p>
     * Alternatively, you can use the following CSS custom properties:
     * <ul>
     * <li>{@code --vaadin-form-layout-label-width}</li>
     * <li>{@code --vaadin-form-layout-label-spacing}</li>
     * </ul>
     *
     * @param labelsAside
     *            {@code true} to position labels aside, {@code false} otherwise
     */
    public void setLabelsAside(boolean labelsAside) {
        getElement().setProperty("labelsAside", labelsAside);
    }

    /**
     * Gets whether {@link FormItem} is configured to prefer positioning labels
     * beside the fields when {@link #setAutoResponsive(boolean)} is enabled.
     *
     * @return {@code true} if labels are positioned aside, {@code false}
     *         otherwise
     * @see #setLabelsAside(boolean)
     */
    public boolean isLabelsAside() {
        return getElement().getProperty("labelsAside", false);
    }
}
