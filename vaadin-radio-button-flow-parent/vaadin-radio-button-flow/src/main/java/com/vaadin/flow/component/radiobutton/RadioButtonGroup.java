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
package com.vaadin.flow.component.radiobutton;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupDataView;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Radio Button Group allows the user to select exactly one value from a list of
 * related but mutually exclusive options.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/radio-group", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-radio-button", version = "23.1.0-beta1")
public class RadioButtonGroup<T>
        extends GeneratedVaadinRadioGroup<RadioButtonGroup<T>, T>
        implements HasItemComponents<T>, SingleSelect<RadioButtonGroup<T>, T>,
        HasListDataView<T, RadioButtonGroupListDataView<T>>,
        HasDataView<T, Void, RadioButtonGroupDataView<T>>, HasValidation,
        HasHelper, HasSize, HasLabel {

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>(
            itemLabelGenerator);

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

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

    /**
     * Default constructor. Creates an empty radio button group.
     */
    public RadioButtonGroup() {
        super(null, null, String.class, RadioButtonGroup::presentationToModel,
                RadioButtonGroup::modelToPresentation, true);

        registerValidation();
    }

    /**
     * Creates an empty radio button group with the defined label.
     *
     * @param label
     *            the label describing the radio button group
     * @see #setLabel(String)
     */
    public RadioButtonGroup(String label) {
        this();
        setLabel(label);
    }

    /**
     * Creates a radio button group with the defined label and populated with
     * the items in the collection.
     *
     * @param label
     *            the label describing the radio button group
     * @param items
     *            the items to be shown in the list of the radio button group
     * @see #setLabel(String)
     * @see #setItems(Collection)
     */
    public RadioButtonGroup(String label, Collection<T> items) {
        this(label);
        setItems(items);
    }

    /**
     * Creates a radio button group with the defined label and populated with
     * the items in the array.
     *
     * @param label
     *            the label describing the radio button group
     * @param items
     *            the items to be shown in the list of the radio button group
     * @see #setLabel(String)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public RadioButtonGroup(String label, T... items) {
        this(label);
        setItems(items);
    }

    /**
     * Constructs a radio button group with a value change listener.
     *
     * @param listener
     *            the value change listener to add
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public RadioButtonGroup(
            ValueChangeListener<ComponentValueChangeEvent<RadioButtonGroup<T>, T>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs a radio button group with the defined label and a value change
     * listener.
     *
     * @param label
     *            the label describing the radio button group
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public RadioButtonGroup(String label,
            ValueChangeListener<ComponentValueChangeEvent<RadioButtonGroup<T>, T>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a radio button group with the defined label, a value change
     * listener and populated with the items in the array.
     *
     * @param label
     *            the label describing the radio button group
     * @param listener
     *            the value change listener to add
     * @param items
     *            the items to be shown in the list of the radio button group
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public RadioButtonGroup(String label,
            ValueChangeListener<ComponentValueChangeEvent<RadioButtonGroup<T>, T>> listener,
            T... items) {
        this(label, listener);
        setItems(items);
    }

    @Override
    public RadioButtonGroupDataView<T> setItems(
            DataProvider<T, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public RadioButtonGroupDataView<T> setItems(
            InMemoryDataProvider<T> inMemoryDataProvider) {
        // We don't use DataProvider.withConvertedFilter() here because it's
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<T, Void> convertedDataProvider = new DataProviderWrapper<T, Void, SerializablePredicate<T>>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<T> getFilter(Query<T, Void> query) {
                // Just ignore the query filter (Void) and apply the
                // predicate only
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true);
            }
        };
        return setItems(convertedDataProvider);
    }

    @Override
    public RadioButtonGroupListDataView<T> setItems(
            ListDataProvider<T> dataProvider) {
        setDataProvider(dataProvider);
        return getListDataView();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Because the stream is collected to a list anyway, use
     *             {@link HasListDataView#setItems(Collection)} instead.
     */
    @Deprecated
    public void setItems(Stream<T> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
    }

    /**
     * Gets the list data view for the RadioButtonGroup. This data view should
     * only be used when the items are in-memory and set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown.
     *
     * @return the list data view that provides access to the data bound to the
     *         RadioButtonGroup
     */
    @Override
    public RadioButtonGroupListDataView<T> getListDataView() {
        return new RadioButtonGroupListDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged, (filter, sorting) -> reset());
    }

    /**
     * Gets the generic data view for the RadioButtonGroup. This data view
     * should only be used when {@link #getListDataView()} is not applicable for
     * the underlying data provider.
     *
     * @return the generic DataView instance implementing
     *         {@link RadioButtonGroupDataView}
     */
    @Override
    public RadioButtonGroupDataView<T> getGenericDataView() {
        return new RadioButtonGroupDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged);
    }

    @Override
    protected boolean hasValidValue() {
        String selectedKey = getElement().getProperty("value");
        return itemEnabledProvider.test(keyMapper.get(selectedKey));
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link RadioButtonGroupListDataView} or
     *             {@link RadioButtonGroupDataView}
     */
    @Deprecated
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        DataViewUtils.removeComponentFilterAndSortComparator(this);
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
                                ((DataChangeEvent.DataRefreshEvent<T>) event)
                                        .getItem());
                    } else {
                        reset();
                    }
                });
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the radio button group for each item. By default,
     * {@link String#valueOf(Object)} is used.
     * <p>
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        setRenderer(new TextRenderer<>(itemLabelGenerator));
    }

    /**
     * Gets the item label generator that is used to produce the strings shown
     * in the radio button group for each item.
     *
     * @return the item label generator used, not null
     */
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
        getRadioButtons().forEach(
                rb -> rb.setChecked(Objects.equals(rb.getItem(), value)));
        refreshButtons();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null) {
            setupDataProviderListener(getDataProvider());
        }
        FieldValidationUtil.disableClientValidation(this);
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
     * @deprecated use {@link #getListDataView()} or
     *             {@link #getGenericDataView()} instead
     */
    @Deprecated
    public DataProvider<T, ?> getDataProvider() {
        return Optional.ofNullable(dataProvider).map(AtomicReference::get)
                .orElse(null);
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
     * <p>
     * Note: Component acts as a label to the button and clicks on it trigger
     * the radio button. Hence interactive components like DatePicker or
     * ComboBox cannot be used.
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
        setDisabled(!enabled);
        refreshButtons();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadonly(readOnly);
        refreshButtons();
    }

    @Override
    public boolean isReadOnly() {
        return super.isReadonlyBoolean();
    }

    /**
     * Specifies that the user must select in a value.
     * <p>
     * NOTE: The required indicator will not be visible, if there is no
     * {@code label} property set for the RadioButtonGroup.
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

    /**
     * Sets the label for the field.
     *
     * @param label
     *            value for the {@code label} property in the webcomponent
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    @Override
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

    @SuppressWarnings("unchecked")
    private void reset() {
        keyMapper.removeAll();
        clear();

        synchronized (dataProvider) {
            // Cache helper component before removal
            Component helperComponent = getHelperComponent();

            // Remove all known children (doesn't remove client-side-only
            // children such as the label)
            getChildren().forEach(this::remove);

            // reinsert helper component
            setHelperComponent(helperComponent);

            final AtomicInteger itemCounter = new AtomicInteger(0);
            getDataProvider().fetch(DataViewUtils.getQuery(this))
                    .map(item -> createRadioButton((T) item))
                    .forEach(component -> {
                        add((Component) component);
                        itemCounter.incrementAndGet();
                    });
            lastFetchedDataSize = itemCounter.get();

            // Ignore new size requests unless the last one has been executed
            // so as to avoid multiple beforeClientResponses.
            if (sizeRequest == null) {
                sizeRequest = ui -> {
                    fireSizeEvent();
                    sizeRequest = null;
                };
                // Size event is fired before client response so as to avoid
                // multiple size change events during server round trips
                runBeforeClientResponse(sizeRequest);
            }
        }
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void fireSizeEvent() {
        final int newSize = lastFetchedDataSize;
        if (lastNotifiedDataSize != newSize) {
            lastNotifiedDataSize = newSize;
            fireEvent(new ItemCountChangeEvent<>(this, newSize, false));
        }
    }

    private void resetRadioButton(T item) {
        getRadioButtons()
                .filter(radioButton -> getItemId(radioButton.getItem())
                        .equals(getItemId(item)))
                .findFirst().ifPresent(this::updateButton);
    }

    private Object getItemId(T item) {
        return getIdentifierProvider().apply(item);
    }

    @SuppressWarnings("unchecked")
    private IdentifierProvider<T> getIdentifierProvider() {
        IdentifierProvider<T> identifierProviderObject = (IdentifierProvider<T>) ComponentUtil
                .getData(this, IdentifierProvider.class);
        if (identifierProviderObject == null) {
            DataProvider<T, ?> dataProvider = getDataProvider();
            if (dataProvider != null) {
                return dataProvider::getId;
            } else {
                return IdentifierProvider.identity();
            }
        } else {
            return identifierProviderObject;
        }
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
        Component labelComponent = getItemRenderer()
                .createComponent(button.getItem());
        button.setLabelComponent(labelComponent);
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

    /**
     * Compares two value instances to each other to determine whether they are
     * equal. Equality is used to determine whether to update internal state and
     * fire an event when {@link #setValue(Object)} or
     * {@link #setModelValue(Object, boolean)} is called. Subclasses can
     * override this method to define an alternative comparison method instead
     * of {@link Objects#equals(Object)}.
     *
     * @param value1
     *            the first instance
     * @param value2
     *            the second instance
     * @return <code>true</code> if the instances are equal; otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean valueEquals(T value1, T value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        return getItemId(value1).equals(getItemId(value2));
    }

    private void updateEnabled(RadioButton<T> button) {
        boolean disabled = isDisabledBoolean()
                || !getItemEnabledProvider().test(button.getItem());

        if (this.isReadOnly() && !button.isCheckedBoolean()) {
            // Mark non-checked radio buttons in a readonly group as disabled.
            disabled = true;
        }

        button.setEnabled(!disabled);
        button.setDisabled(disabled);
        // When enabling a disabled radio group, individual button Web
        // Components that should remain disabled (due to itemEnabledProvider),
        // may end up rendering as enabled.
        // Enforce the Web Component state using JS.
        button.getElement().executeJs("this.disabled = $0", disabled);
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

    private void identifierProviderChanged(
            IdentifierProvider<T> identifierProvider) {
        keyMapper.setIdentifierGetter(identifierProvider);
    }

}
