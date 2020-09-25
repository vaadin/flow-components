/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * A single select component using radio buttons as options.
 * <p>
 * This is a server side Java integration for the {@code vaadin-radio-group}
 * element.
 * <p>
 * Usage examples, see
 * <a href="https://vaadin.com/components/vaadin-radio-button/java-examples">the
 * demo in vaadin.com</a>.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "1.4.1")
public class RadioButtonGroup<T>
        extends GeneratedVaadinRadioGroup<RadioButtonGroup<T>, T> implements
        HasItemsAndComponents<T>, SingleSelect<RadioButtonGroup<T>, T>,
        HasDataProvider<T>, HasValidation {

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>();

    private boolean isReadOnly;

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private static <T> T presentationToModel(
            RadioButtonGroup<T> radioButtonGroup, String presentation) {
        if (!radioButtonGroup.keyMapper.containsKey(presentation)) {
            return null;
        }
        return radioButtonGroup.keyMapper.get(presentation);
    }

    private static <T> String modelToPresentation(
            RadioButtonGroup<T> radioButtonGroup, T model) {
        if (!radioButtonGroup.keyMapper.has(model)) {
            return null;
        }
        return radioButtonGroup.keyMapper.key(model);
    }

    public RadioButtonGroup() {
        super(null, null, String.class, RadioButtonGroup::presentationToModel,
                RadioButtonGroup::modelToPresentation);

        registerValidation();
    }

    @Override
    protected boolean hasValidValue() {
        String selectedKey = getElement().getProperty("value");
        return itemEnabledProvider.test(keyMapper.get(selectedKey));
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        reset();

        setupDataProviderListener(dataProvider);
    }

    private void setupDataProviderListener(DataProvider<T, ?> dataProvider) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(event -> {
                    if (event instanceof DataChangeEvent.DataRefreshEvent) {
                        resetRadioButton(
                            ((DataChangeEvent.DataRefreshEvent<T>) event).getItem());
                    } else {
                        reset();
                    }
                });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null && dataProviderListenerRegistration == null) {
            setupDataProviderListener(getDataProvider());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderListenerRegistration != null) {
        	dataProviderListenerRegistration.remove();
        	dataProviderListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    /**
     * Sets the item enabled predicate for this radio button group. The
     * predicate is applied to each item to determine whether the item should be
     * enabled ({@code true}) or disabled ({@code false}). Disabled items are
     * displayed as grayed out and the user cannot select them. The default
     * predicate always returns true (all the items are enabled).
     *
     * @param itemEnabledProvider
     *            the item enable predicate, not {@code null}
     */
    public void setItemEnabledProvider(
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        refreshButtons();
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setRenderer(ComponentRenderer)
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this radio button group. The renderer is
     * applied to each item to create a component which represents the item.
     *
     * @param renderer
     *            the item renderer, not {@code null}
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> renderer) {
        this.itemRenderer = Objects.requireNonNull(renderer);
        refreshButtons();
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        if (isReadOnly()) {
            setDisabled(true);
        } else {
            setDisabled(!enabled);
        }
        refreshButtons();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        if (isEnabled()) {
            setDisabled(readOnly);
            refreshButtons();
        }
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Specifies that the user must select in a value.
     * <p>
     * NOTE: The required indicator will not be visible, if there is no
     * {@code label} property set for the radiobutton group.
     *
     * @param required
     *            the boolean value to set
     */
    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * Specifies that the user must select a value
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code required} property from the webcomponent
     */
    public boolean isRequired() {
        return super.isRequiredBoolean();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Gets the current error message from the radio button group.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return super.getErrorMessageString();
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    public String getLabel() {
        return super.getLabelString();
    }

    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    private void reset() {
        keyMapper.removeAll();
        removeAll();
        clear();
        getDataProvider().fetch(new Query<>()).map(this::createRadioButton)
                .forEach(this::add);
    }

    private void resetRadioButton(T item) {
        getRadioButtons().filter(radioButton ->
            getDataProvider().getId(radioButton.getItem()).equals(getDataProvider().getId(item)))
        .findFirst()
        .ifPresent(this::updateButton);
    }

    private Component createRadioButton(T item) {
        RadioButton<T> button = new RadioButton<>(keyMapper.key(item), item);
        updateButton(button);
        return button;
    }

    private void refreshButtons() {
        getRadioButtons().forEach(this::updateButton);
    }

    @SuppressWarnings("unchecked")
    private Stream<RadioButton<T>> getRadioButtons() {
        return getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<T>) child);
    }

    private void updateButton(RadioButton<T> button) {
        updateEnabled(button);
        button.removeAll();
        button.add(getItemRenderer().createComponent(button.getItem()));
    }

    private void validateSelectionEnabledState(PropertyChangeEvent event) {
        if (!hasValidValue()) {
            T oldValue = getValue(event.getOldValue());
            // return the value back on the client side
            try {
                validationRegistration.remove();
                getElement().setProperty("value", keyMapper.key(oldValue));
            } finally {
                registerValidation();
            }
            // Now make sure that the button is still in the correct state
            Optional<RadioButton<T>> selectedButton = getRadioButtons().filter(
                    button -> button.getItem() == getValue(event.getValue()))
                    .findFirst();

            selectedButton.ifPresent(this::updateEnabled);
        }
    }

    private void updateEnabled(RadioButton<T> button) {
        boolean disabled = isDisabledBoolean()
                || !getItemEnabledProvider().test(button.getItem());
        Serializable rawValue = button.getElement().getPropertyRaw("disabled");
        if (rawValue instanceof Boolean) {
            // convert the boolean value to a String to force update the
            // property value. Otherwise since the provided value is the same as
            // the current one the update don't do anything.
            button.getElement().setProperty("disabled",
                    disabled ? Boolean.TRUE.toString() : null);
        } else {
            button.setDisabled(disabled);
        }
    }

    private T getValue(Serializable key) {
        if (key == null) {
            return null;
        }
        return keyMapper.get(key.toString());
    }

    private void registerValidation() {
        if (validationRegistration != null) {
            validationRegistration.remove();
        }
        validationRegistration = getElement().addPropertyChangeListener("value",
                validationListener);
    }
}
