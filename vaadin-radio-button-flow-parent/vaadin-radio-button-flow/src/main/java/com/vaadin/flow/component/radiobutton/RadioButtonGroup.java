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
package com.vaadin.flow.component.radiobutton;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupDataView;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.SelectionPreservationHandler;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;
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
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Radio Button Group allows the user to select exactly one value from a list of
 * related but mutually exclusive options.
 * <h2>Validation</h2>
 * <p>
 * Radio Button Group comes with a built-in validation mechanism that verifies
 * that a radio button is selected when
 * {@link #setRequiredIndicatorVisible(boolean) required} is enabled. Validation
 * is triggered whenever the user selects a radio button or the value is updated
 * programmatically. In practice, however, the required error can only occur if
 * the value is cleared programmatically. This is because radio buttons, by
 * design, don't allow users to clear a selection through UI interaction. If the
 * required error occurs, the component is marked as invalid and an error
 * message is displayed below the group.
 * <p>
 * The required error message can be configured using either
 * {@link RadioButtonGroupI18n#setRequiredErrorMessage(String)} or
 * {@link #setErrorMessage(String)}.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. Please note that Binder provides its own API for the required
 * validation, see {@link Binder.BindingBuilder#asRequired(String)
 * asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the built-in validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-radio-group")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/radio-group", version = "24.8.0-alpha18")
@JsModule("@vaadin/radio-group/src/vaadin-radio-group.js")
public class RadioButtonGroup<T>
        extends AbstractSinglePropertyField<RadioButtonGroup<T>, T>
        implements HasAriaLabel, HasClientValidation,
        HasDataView<T, Void, RadioButtonGroupDataView<T>>,
        HasListDataView<T, RadioButtonGroupListDataView<T>>,
        InputField<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<T>, T>, T>,
        HasThemeVariant<RadioGroupVariant>, HasValidationProperties,
        HasValidator<T>, SingleSelect<RadioButtonGroup<T>, T> {

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>(
            itemLabelGenerator);

    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    private RadioButtonGroupI18n i18n;

    private Validator<T> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(
                        RadioButtonGroupI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<RadioButtonGroup<T>, T> validationController = new ValidationController<>(
            this);

    private SelectionPreservationHandler<T> selectionPreservationHandler;

    private static <T> T presentationToModel(
            RadioButtonGroup<T> radioButtonGroup, String presentation) {
        if (radioButtonGroup.keyMapper == null) {
            return null;
        }
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
        super("value", null, String.class,
                RadioButtonGroup::presentationToModel,
                RadioButtonGroup::modelToPresentation);

        getElement().setProperty("manualValidation", true);

        addValueChangeListener(e -> validate());

        initSelectionPreservationHandler();
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
                this::identifierProviderChanged,
                (filter, sorting) -> rebuild());
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
        T item = keyMapper.get(selectedKey);
        // The item enabled provider should not be invoked for null values.
        if (item == null) {
            return true;
        }
        return itemEnabledProvider.test(item);
    }

    /**
     * Sets a generic data provider for the RadioButtonGroup to use.
     * <p>
     * Use this method when none of the {@code setItems} methods are applicable,
     * e.g. when having a data provider with filter that cannot be transformed
     * to {@code DataProvider<T, Void>}.
     *
     * @param dataProvider
     *            DataProvider instance to use, not <code>null</code>
     */
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        DataViewUtils.removeComponentFilterAndSortComparator(this);

        keyMapper.removeAll();
        clear();
        rebuild();

        setupDataProviderListener(dataProvider);
    }

    private void setupDataProviderListener(DataProvider<T, ?> dataProvider) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(this::handleDataChange);
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the radio button group for each item. By default,
     * {@link String#valueOf(Object)} is used.
     * <p>
     * Setting an item label generator removes any previously set item renderer.
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
        if (value == null) {
            getRadioButtons().forEach(rb -> rb.setChecked(false));
        } else {
            getRadioButtons().forEach(
                    rb -> rb.setChecked(valueEquals(rb.getItem(), value)));
        }
        refreshButtons();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null) {
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
     * Gets the data provider used by this RadioButtonGroup.
     *
     * <p>
     * To get information and control over the items in the RadioButtonGroup,
     * use either {@link #getListDataView()} or {@link #getGenericDataView()}
     * instead.
     *
     * @return the data provider used by this RadioButtonGroup
     */
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
        getElement().setProperty("readonly", readOnly);
        refreshButtons();
    }

    @Override
    public boolean isReadOnly() {
        return getElement().getProperty("readonly", false);
    }

    /**
     * Sets whether the user is required to select a radio button. When
     * required, an indicator appears next to the label and the field
     * invalidates if the selection is cleared programmatically.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see RadioButtonGroupI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to select a radio button.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Alias for {@link #isRequiredIndicatorVisible()}
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredIndicatorVisible();
    }

    /**
     * Alias for {@link #setRequiredIndicatorVisible(boolean)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     */
    public void setRequired(boolean required) {
        setRequiredIndicatorVisible(required);
    }

    /**
     * Sets the label for the field.
     *
     * @param label
     *            value for the {@code label} property in the webcomponent
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Sets the selection preservation mode. Determines what happens with the
     * selection when {@link DataProvider#refreshAll} is called. The selection
     * is discarded in any case when a new data provider is set. The default is
     * {@link SelectionPreservationMode#DISCARD}.
     *
     * @param selectionPreservationMode
     *            the selection preservation mode to switch to, not {@code null}
     *
     * @see SelectionPreservationMode
     */
    public void setSelectionPreservationMode(
            SelectionPreservationMode selectionPreservationMode) {
        selectionPreservationHandler
                .setSelectionPreservationMode(selectionPreservationMode);
    }

    /**
     * Gets the selection preservation mode.
     *
     * @return the selection preservation mode
     *
     * @see #setSelectionPreservationMode(SelectionPreservationMode)
     */
    public SelectionPreservationMode getSelectionPreservationMode() {
        return selectionPreservationHandler.getSelectionPreservationMode();
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String labelledBy) {
        getElement().setProperty("accessibleNameRef", labelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    @SuppressWarnings("unchecked")
    private void rebuild() {
        synchronized (dataProvider) {
            // Cache helper component before removal
            Component helperComponent = getHelperComponent();

            // Remove all known children (doesn't remove client-side-only
            // children such as the label)
            getChildren()
                    .forEach(child -> child.getElement().removeFromParent());

            // reinsert helper component
            setHelperComponent(helperComponent);

            final AtomicInteger itemCounter = new AtomicInteger(0);
            getDataProvider().fetch(DataViewUtils.getQuery(this))
                    .map(item -> createRadioButton((T) item))
                    .forEach(component -> {
                        getElement().appendChild(
                                ((Component) component).getElement());
                        itemCounter.incrementAndGet();
                    });
            lastFetchedDataSize = itemCounter.get();

            // Ignore new size requests unless the last one has been executed
            // so as to avoid multiple beforeClientResponses.
            if (sizeRequest == null) {
                // Using anonymous class to fix serialization issue:
                // https://github.com/vaadin/flow-components/issues/6555
                // Do not replace with lambda
                sizeRequest = new SerializableConsumer<>() {
                    @Override
                    public void accept(UI ui) {
                        fireSizeEvent();
                        sizeRequest = null;
                    }
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

    /**
     * If true, the user cannot interact with this element.
     *
     * @param disabled
     *            the boolean value to set
     */
    private void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * If true, the user cannot interact with this element.
     *
     * @return the {@code disabled} property from the webcomponent
     */
    private boolean isDisabled() {
        return getElement().getProperty("disabled", false);
    }

    private void updateEnabled(RadioButton<T> button) {
        boolean disabled = isDisabled()
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

    private void identifierProviderChanged(
            IdentifierProvider<T> identifierProvider) {
        keyMapper.setIdentifierGetter(identifierProvider);
    }

    private void initSelectionPreservationHandler() {
        selectionPreservationHandler = new SelectionPreservationHandler<>(
                SelectionPreservationMode.DISCARD) {

            @Override
            public void onPreserveAll(DataChangeEvent<T> dataChangeEvent) {
                // NO-OP
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onPreserveExisting(DataChangeEvent<T> dataChangeEvent) {
                T initialValue = getValue();
                if (getDataProvider()
                        .fetch(DataViewUtils.getQuery(RadioButtonGroup.this))
                        .noneMatch(
                                item -> valueEquals((T) item, initialValue))) {
                    clear();
                }
            }

            @Override
            public void onDiscard(DataChangeEvent<T> dataChangeEvent) {
                clear();
            }
        };
    }

    private void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        if (dataChangeEvent instanceof DataChangeEvent.DataRefreshEvent<T> refreshEvent) {
            keyMapper.refresh(refreshEvent.getItem());
            resetRadioButton(refreshEvent.getItem());
        } else {
            keyMapper.removeAll();
            selectionPreservationHandler.handleDataChange(dataChangeEvent);
            rebuild();
        }
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    @Override
    public Validator<T> getDefaultValidator() {
        return defaultValidator;
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property based on
     * the result. If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message defined in the i18n object is used.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(RadioButtonGroupI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public RadioButtonGroupI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(RadioButtonGroupI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<RadioButtonGroupI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link RadioButtonGroup}.
     */
    public static class RadioButtonGroupI18n implements Serializable {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see RadioButtonGroup#isRequiredIndicatorVisible()
         * @see RadioButtonGroup#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link RadioButtonGroup#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see RadioButtonGroup#isRequiredIndicatorVisible()
         * @see RadioButtonGroup#setRequiredIndicatorVisible(boolean)
         */
        public RadioButtonGroupI18n setRequiredErrorMessage(
                String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
