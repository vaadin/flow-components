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
package com.vaadin.flow.component.formlayout;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableFunction;

/**
 * AutoForm automatically generates a form layout with fields based on a bean
 * class. It uses reflection to discover bean properties and creates appropriate
 * input components for each property type.
 * <p>
 * When the Vaadin component libraries are available on the classpath, AutoForm
 * creates the following field types automatically:
 * <ul>
 * <li>String - TextField</li>
 * <li>Integer/int, Long/long - IntegerField</li>
 * <li>Double/double, Float/float - NumberField</li>
 * <li>BigDecimal, BigInteger - BigDecimalField</li>
 * <li>Boolean/boolean - Checkbox</li>
 * <li>LocalDate - DatePicker</li>
 * <li>LocalTime - TimePicker</li>
 * <li>LocalDateTime - DateTimePicker</li>
 * </ul>
 * <p>
 * Example usage:
 *
 * <pre>
 * AutoForm&lt;Person&gt; form = new AutoForm&lt;&gt;(Person.class);
 * form.setValue(new Person("John", "Doe"));
 *
 * // Add save handler
 * form.setOnSave(person -&gt; personService.save(person));
 * </pre>
 *
 * <h2>Customization</h2>
 * <p>
 * You can customize field generation by:
 * <ul>
 * <li>Excluding properties with {@link #setExcludedProperties(String...)}</li>
 * <li>Setting visible properties with
 * {@link #setVisibleProperties(String...)}</li>
 * <li>Providing custom field factories with
 * {@link #setFieldFactory(String, SerializableFunction)}</li>
 * <li>Configuring fields after creation with
 * {@link #setFieldCustomizer(String, SerializableBiConsumer)}</li>
 * </ul>
 *
 * <h2>Data Binding and Validation</h2>
 * <p>
 * AutoForm uses {@link BeanValidationBinder} for data binding. This means it
 * automatically applies Bean Validation constraints (JSR-380) like
 * {@code @NotNull}, {@code @Size}, {@code @Email}, etc.
 *
 * @param <T>
 *            the bean type
 * @author Vaadin Ltd
 */
public class AutoForm<T> extends Composite<FormLayout> implements Serializable {

    private static final Map<Class<?>, String> TYPE_TO_FIELD_CLASS = new HashMap<>();

    static {
        TYPE_TO_FIELD_CLASS.put(String.class,
                "com.vaadin.flow.component.textfield.TextField");
        TYPE_TO_FIELD_CLASS.put(Integer.class,
                "com.vaadin.flow.component.textfield.IntegerField");
        TYPE_TO_FIELD_CLASS.put(int.class,
                "com.vaadin.flow.component.textfield.IntegerField");
        TYPE_TO_FIELD_CLASS.put(Long.class,
                "com.vaadin.flow.component.textfield.IntegerField");
        TYPE_TO_FIELD_CLASS.put(long.class,
                "com.vaadin.flow.component.textfield.IntegerField");
        TYPE_TO_FIELD_CLASS.put(Double.class,
                "com.vaadin.flow.component.textfield.NumberField");
        TYPE_TO_FIELD_CLASS.put(double.class,
                "com.vaadin.flow.component.textfield.NumberField");
        TYPE_TO_FIELD_CLASS.put(Float.class,
                "com.vaadin.flow.component.textfield.NumberField");
        TYPE_TO_FIELD_CLASS.put(float.class,
                "com.vaadin.flow.component.textfield.NumberField");
        TYPE_TO_FIELD_CLASS.put(BigDecimal.class,
                "com.vaadin.flow.component.textfield.BigDecimalField");
        TYPE_TO_FIELD_CLASS.put(BigInteger.class,
                "com.vaadin.flow.component.textfield.BigDecimalField");
        TYPE_TO_FIELD_CLASS.put(Boolean.class,
                "com.vaadin.flow.component.checkbox.Checkbox");
        TYPE_TO_FIELD_CLASS.put(boolean.class,
                "com.vaadin.flow.component.checkbox.Checkbox");
        TYPE_TO_FIELD_CLASS.put(LocalDate.class,
                "com.vaadin.flow.component.datepicker.DatePicker");
        TYPE_TO_FIELD_CLASS.put(LocalTime.class,
                "com.vaadin.flow.component.timepicker.TimePicker");
        TYPE_TO_FIELD_CLASS.put(LocalDateTime.class,
                "com.vaadin.flow.component.datetimepicker.DateTimePicker");
    }

    private final Class<T> beanType;
    private final BeanValidationBinder<T> binder;

    private final Map<String, PropertyDescriptor> propertyDescriptors = new LinkedHashMap<>();
    private final Map<String, HasValue<?, ?>> fields = new LinkedHashMap<>();
    private final Map<String, SerializableFunction<PropertyDescriptor, ? extends HasValue<?, ?>>> fieldFactories = new HashMap<>();
    private final Map<String, SerializableBiConsumer<PropertyDescriptor, HasValue<?, ?>>> fieldCustomizers = new HashMap<>();

    private Set<String> excludedProperties = new HashSet<>();
    private List<String> visibleProperties = null;

    private Consumer<T> onSave;
    private Consumer<T> onDelete;
    private Runnable onCancel;

    private NativeButton saveButton;
    private NativeButton deleteButton;
    private NativeButton cancelButton;
    private FormLayout.FormRow buttonRow;

    private boolean buttonsVisible = true;
    private boolean deleteButtonVisible = false;

    private T currentValue;
    private boolean built = false;

    /**
     * Creates a new AutoForm for the given bean type.
     *
     * @param beanType
     *            the bean class to generate the form for
     */
    public AutoForm(Class<T> beanType) {
        this.beanType = Objects.requireNonNull(beanType,
                "Bean type cannot be null");
        this.binder = new BeanValidationBinder<>(beanType);

        discoverProperties();
    }

    /**
     * Returns the binder used for data binding.
     *
     * @return the binder
     */
    public Binder<T> getBinder() {
        return binder;
    }

    /**
     * Returns the bean type.
     *
     * @return the bean class
     */
    public Class<T> getBeanType() {
        return beanType;
    }

    /**
     * Sets the bean value to edit in the form.
     *
     * @param value
     *            the bean instance to edit
     */
    public void setValue(T value) {
        this.currentValue = value;
        ensureBuilt();
        binder.setBean(value);
    }

    /**
     * Returns the current bean value. Note that this returns the bean being
     * edited, which may have uncommitted changes.
     *
     * @return the current bean value
     */
    public T getValue() {
        return currentValue;
    }

    /**
     * Validates the form and writes the values to the bean if valid.
     *
     * @return true if validation passed and values were written
     */
    public boolean validate() {
        return binder.validate().isOk();
    }

    /**
     * Sets the properties to exclude from the form.
     *
     * @param properties
     *            the property names to exclude
     * @return this form for method chaining
     */
    public AutoForm<T> setExcludedProperties(String... properties) {
        if (built) {
            throw new IllegalStateException(
                    "Cannot modify configuration after the form has been built");
        }
        this.excludedProperties = new HashSet<>(Arrays.asList(properties));
        return this;
    }

    /**
     * Sets the properties to show in the form, in the specified order. If not
     * set, all discovered properties will be shown.
     *
     * @param properties
     *            the property names to show
     * @return this form for method chaining
     */
    public AutoForm<T> setVisibleProperties(String... properties) {
        if (built) {
            throw new IllegalStateException(
                    "Cannot modify configuration after the form has been built");
        }
        this.visibleProperties = Arrays.asList(properties);
        return this;
    }

    /**
     * Sets a custom field factory for a specific property.
     *
     * @param propertyName
     *            the property name
     * @param factory
     *            the factory function that creates the field component
     * @return this form for method chaining
     */
    public AutoForm<T> setFieldFactory(String propertyName,
            SerializableFunction<PropertyDescriptor, ? extends HasValue<?, ?>> factory) {
        if (built) {
            throw new IllegalStateException(
                    "Cannot modify configuration after the form has been built");
        }
        fieldFactories.put(propertyName, factory);
        return this;
    }

    /**
     * Sets a customizer to configure a field after it is created.
     *
     * @param propertyName
     *            the property name
     * @param customizer
     *            the customizer function
     * @return this form for method chaining
     */
    public AutoForm<T> setFieldCustomizer(String propertyName,
            SerializableBiConsumer<PropertyDescriptor, HasValue<?, ?>> customizer) {
        if (built) {
            throw new IllegalStateException(
                    "Cannot modify configuration after the form has been built");
        }
        fieldCustomizers.put(propertyName, customizer);
        return this;
    }

    /**
     * Sets the handler to call when the save button is clicked. The handler
     * receives the bean if validation passes.
     *
     * @param onSave
     *            the save handler
     * @return this form for method chaining
     */
    public AutoForm<T> setOnSave(Consumer<T> onSave) {
        this.onSave = onSave;
        return this;
    }

    /**
     * Sets the handler to call when the delete button is clicked.
     *
     * @param onDelete
     *            the delete handler
     * @return this form for method chaining
     */
    public AutoForm<T> setOnDelete(Consumer<T> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    /**
     * Sets the handler to call when the cancel button is clicked.
     *
     * @param onCancel
     *            the cancel handler
     * @return this form for method chaining
     */
    public AutoForm<T> setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    /**
     * Sets whether the action buttons (save, delete, cancel) are visible.
     *
     * @param visible
     *            true to show buttons, false to hide them
     * @return this form for method chaining
     */
    public AutoForm<T> setButtonsVisible(boolean visible) {
        this.buttonsVisible = visible;
        if (buttonRow != null) {
            buttonRow.setVisible(visible);
        }
        return this;
    }

    /**
     * Sets whether the delete button is visible.
     *
     * @param visible
     *            true to show the delete button
     * @return this form for method chaining
     */
    public AutoForm<T> setDeleteButtonVisible(boolean visible) {
        this.deleteButtonVisible = visible;
        if (deleteButton != null) {
            deleteButton.setVisible(visible);
        }
        return this;
    }

    /**
     * Returns the save button, creating it if necessary.
     *
     * @return the save button
     */
    public NativeButton getSaveButton() {
        ensureBuilt();
        return saveButton;
    }

    /**
     * Returns the delete button, creating it if necessary.
     *
     * @return the delete button
     */
    public NativeButton getDeleteButton() {
        ensureBuilt();
        return deleteButton;
    }

    /**
     * Returns the cancel button, creating it if necessary.
     *
     * @return the cancel button
     */
    public NativeButton getCancelButton() {
        ensureBuilt();
        return cancelButton;
    }

    /**
     * Returns the field component for the given property name.
     *
     * @param propertyName
     *            the property name
     * @return the field component, or null if not found
     */
    public HasValue<?, ?> getField(String propertyName) {
        ensureBuilt();
        return fields.get(propertyName);
    }

    /**
     * Returns an unmodifiable map of all field components.
     *
     * @return the fields map
     */
    public Map<String, HasValue<?, ?>> getFields() {
        ensureBuilt();
        return Collections.unmodifiableMap(fields);
    }

    @Override
    protected FormLayout initContent() {
        FormLayout layout = new FormLayout();
        layout.setAutoResponsive(true);
        return layout;
    }

    /**
     * Ensures the form is built. This is called automatically when accessing
     * the form content.
     */
    private void ensureBuilt() {
        if (!built) {
            build();
        }
    }

    /**
     * Builds the form by creating fields for all visible properties.
     */
    private void build() {
        built = true;
        FormLayout layout = getContent();

        List<String> propertiesToShow = getPropertiesToShow();

        for (String propertyName : propertiesToShow) {
            PropertyDescriptor descriptor = propertyDescriptors
                    .get(propertyName);
            if (descriptor == null) {
                continue;
            }

            HasValue<?, ?> field = createField(descriptor);
            if (field != null) {
                fields.put(propertyName, field);
                bindField(descriptor, field);

                SerializableBiConsumer<PropertyDescriptor, HasValue<?, ?>> customizer = fieldCustomizers
                        .get(propertyName);
                if (customizer != null) {
                    customizer.accept(descriptor, field);
                }

                if (field instanceof Component component) {
                    layout.add(component);
                }
            }
        }

        createButtons(layout);
    }

    /**
     * Returns the list of properties to show in the form.
     */
    private List<String> getPropertiesToShow() {
        if (visibleProperties != null) {
            return visibleProperties.stream()
                    .filter(p -> !excludedProperties.contains(p)).toList();
        }
        return propertyDescriptors.keySet().stream()
                .filter(p -> !excludedProperties.contains(p)).toList();
    }

    /**
     * Discovers bean properties using introspection.
     */
    private void discoverProperties() {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanType,
                    Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                    propertyDescriptors.put(pd.getName(), pd);
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(
                    "Failed to introspect bean type: " + beanType.getName(), e);
        }
    }

    /**
     * Creates a field component for the given property using reflection to load
     * component classes dynamically.
     */
    private HasValue<?, ?> createField(PropertyDescriptor descriptor) {
        String propertyName = descriptor.getName();

        SerializableFunction<PropertyDescriptor, ? extends HasValue<?, ?>> factory = fieldFactories
                .get(propertyName);
        if (factory != null) {
            return factory.apply(descriptor);
        }

        Class<?> propertyType = descriptor.getPropertyType();
        String label = generateLabel(propertyName);

        String fieldClassName = TYPE_TO_FIELD_CLASS.get(propertyType);
        if (fieldClassName == null) {
            return null;
        }

        return createFieldByClassName(fieldClassName, label);
    }

    /**
     * Creates a field instance by class name using reflection.
     */
    @SuppressWarnings("unchecked")
    private HasValue<?, ?> createFieldByClassName(String className,
            String label) {
        try {
            Class<?> fieldClass = Class.forName(className);
            try {
                Constructor<?> labelConstructor = fieldClass
                        .getConstructor(String.class);
                return (HasValue<?, ?>) labelConstructor.newInstance(label);
            } catch (NoSuchMethodException e) {
                HasValue<?, ?> field = (HasValue<?, ?>) fieldClass
                        .getConstructor().newInstance();
                if (field instanceof Component component) {
                    try {
                        component.getClass().getMethod("setLabel", String.class)
                                .invoke(component, label);
                    } catch (NoSuchMethodException ignored) {
                        // Field doesn't support labels
                    }
                }
                return field;
            }
        } catch (ClassNotFoundException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to create field of type " + className, e);
        }
    }

    /**
     * Generates a human-readable label from a property name.
     */
    private String generateLabel(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(propertyName.charAt(0)));

        for (int i = 1; i < propertyName.length(); i++) {
            char c = propertyName.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(c);
        }

        return result.toString();
    }

    /**
     * Binds a field to the binder for the given property.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void bindField(PropertyDescriptor descriptor,
            HasValue<?, ?> field) {
        String propertyName = descriptor.getName();
        Class<?> propertyType = descriptor.getPropertyType();

        if (propertyType == Long.class || propertyType == long.class) {
            binder.forField((HasValue) field).withConverter(
                    intVal -> intVal != null ? Long.valueOf(intVal) : null,
                    longVal -> longVal != null ? longVal.intValue() : null)
                    .bind(propertyName);
        } else if (propertyType == BigInteger.class) {
            binder.forField((HasValue) field)
                    .withConverter(
                            bd -> bd != null ? bd.toBigInteger() : null,
                            bi -> bi != null ? new BigDecimal(bi) : null)
                    .bind(propertyName);
        } else if (propertyType == Float.class || propertyType == float.class) {
            binder.forField((HasValue) field)
                    .withConverter(
                            d -> d != null ? d.floatValue() : null,
                            f -> f != null ? f.doubleValue() : null)
                    .bind(propertyName);
        } else {
            binder.forField((HasValue) field).bind(propertyName);
        }
    }

    /**
     * Creates action buttons.
     */
    private void createButtons(FormLayout layout) {
        saveButton = new NativeButton("Save", e -> handleSave());

        cancelButton = new NativeButton("Cancel", e -> handleCancel());

        deleteButton = new NativeButton("Delete", e -> handleDelete());
        deleteButton.setVisible(deleteButtonVisible);

        buttonRow = new FormLayout.FormRow();
        buttonRow.add(saveButton, cancelButton, deleteButton);
        buttonRow.setVisible(buttonsVisible);

        layout.add(buttonRow);
    }

    private void handleSave() {
        if (binder.validate().isOk() && onSave != null) {
            onSave.accept(currentValue);
        }
    }

    private void handleCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void handleDelete() {
        if (onDelete != null) {
            onDelete.accept(currentValue);
        }
    }
}
