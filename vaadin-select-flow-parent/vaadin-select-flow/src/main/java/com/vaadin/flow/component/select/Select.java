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
package com.vaadin.flow.component.select;

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
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.select.data.SelectDataView;
import com.vaadin.flow.component.select.data.SelectListDataView;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasItemComponents;
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
 * Select allows users to choose a single value from a list of options presented
 * in an overlay. The dropdown can be opened with a click, up/down arrow keys,
 * or by typing the initial character for one of the options.
 * <h2>Validation</h2>
 * <p>
 * Select comes with a built-in validation mechanism that verifies that the
 * field is not empty when {@link #setRequiredIndicatorVisible(boolean)
 * required} is enabled.
 * <p>
 * Validation is triggered whenever the user initiates a value change, for
 * example by selecting an item from the dropdown. Programmatic value changes
 * trigger validation as well. If validation fails, the component is marked as
 * invalid and an error message is displayed below the input.
 * <p>
 * The required error message can be configured using either
 * {@link SelectI18n#setRequiredErrorMessage(String)} or
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
 * @param <T>
 *            the type of the items for the select
 * @author Vaadin Ltd.
 */
@Tag("vaadin-select")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/select", version = "24.8.0-alpha18")
@JsModule("@vaadin/select/src/vaadin-select.js")
@JsModule("./selectConnector.js")
public class Select<T> extends AbstractSinglePropertyField<Select<T>, T>
        implements Focusable<Select<T>>, HasAriaLabel, HasClientValidation,
        HasDataView<T, Void, SelectDataView<T>>, HasItemComponents<T>,
        InputField<AbstractField.ComponentValueChangeEvent<Select<T>, T>, T>,
        HasListDataView<T, SelectListDataView<T>>, HasOverlayClassName,
        HasPrefix, HasThemeVariant<SelectVariant>, HasValidationProperties,
        HasValidator<T>, SingleSelect<Select<T>, T>, HasPlaceholder {

    public static final String LABEL_ATTRIBUTE = "label";

    private static final String VALUE_PROPERTY_NAME = "value";

    private final InternalListBox listBox = new InternalListBox();

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private ComponentRenderer<? extends Component, T> itemRenderer;

    private SerializablePredicate<T> itemEnabledProvider = null;

    private ItemLabelGenerator<T> itemLabelGenerator = null;

    private Registration dataProviderListenerRegistration;

    private boolean resetPending = true;

    private boolean emptySelectionAllowed;

    private String emptySelectionCaption;

    private VaadinItem<T> emptySelectionItem;

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    private SelectI18n i18n;

    private Validator<T> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(SelectI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<Select<T>, T> validationController = new ValidationController<>(
            this);

    /**
     * Constructs a select.
     */
    public Select() {
        super("value", null, String.class, Select::presentationToModel,
                Select::modelToPresentation);

        getElement().setProperty("manualValidation", true);

        setInvalid(false);
        setOpened(false);
        // Trigger model-to-presentation conversion in constructor, so that
        // the client side component has a correct initial value of an empty
        // string
        setPresentationValue(null);

        getElement().appendChild(listBox.getElement());

        addValueChangeListener(e -> validate());

        getElement().addPropertyChangeListener("opened", event -> fireEvent(
                new OpenedChangeEvent(this, event.isUserOriginated())));

        getElement().addPropertyChangeListener("invalid", event -> fireEvent(
                new InvalidChangeEvent(this, event.isUserOriginated())));
    }

    /**
     * Constructs a select with the initial value change listener.
     *
     * @param listener
     *            the value change listener to add
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Select(
            ValueChangeListener<ComponentValueChangeEvent<Select<T>, T>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs a select with the initial label text and value change
     * listener.
     *
     * @param label
     *            the label describing the select
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Select(String label,
            ValueChangeListener<ComponentValueChangeEvent<Select<T>, T>> listener) {
        this();
        setLabel(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a select with the initial label text and value change
     * listener.
     *
     * @param label
     *            the label describing the select
     * @param listener
     *            the value change listener to add
     * @param items
     *            the items to be shown in the list of the select
     * @see #setLabel(String)
     * @see #setItems(Object...)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    @SafeVarargs
    public Select(String label,
            ValueChangeListener<ComponentValueChangeEvent<Select<T>, T>> listener,
            T... items) {
        this(label, listener);
        setItems(items);
    }

    private static <T> T presentationToModel(Select<T> select,
            String presentation) {
        if (select.keyMapper == null) {
            return null;
        }

        if (!select.keyMapper.containsKey(presentation)) {
            return null;
        }
        return select.keyMapper.get(presentation);
    }

    private static <T> String modelToPresentation(Select<T> select, T model) {
        if (model == null) {
            return "";
        }
        if (!select.keyMapper.has(model)) {
            return null;
        }
        return select.keyMapper.key(model);
    }

    /*
     * Internal version of list box that is just used to delegate the child
     * components to.
     *
     * Using this internally allows all events and updates to the children
     * (items, possible child components) to work even though the list box
     * element is moved on the client side in the renderer method from light-dom
     * to be a child of the select overlay.
     *
     * Not using the proper ListBox because all communication & updates are
     * going through the Select. Using ListBox would just duplicate things, and
     * cause e.g. unnecessary synchronizations and dependency to the Java
     * integration.
     *
     * The known side effect is that at the element level, the child components
     * are not the correct ones, e.g. the list box is the only child of select,
     * even though that is not visible from the component level.
     */
    @Tag("vaadin-select-list-box")
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
    @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
    private class InternalListBox extends Component
            implements HasItemComponents<T> {

        @Override
        public int getItemPosition(T item) {
            // null item is the empty selection item and that is always first
            if (item == null && isEmptySelectionAllowed()) {
                return 0;
            } else {
                return HasItemComponents.super.getItemPosition(item);
            }
        }
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer or {@code null} if none set
     * @see #setRenderer(ComponentRenderer)
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this select group. The renderer is applied to
     * each item to create a component which represents the item option in the
     * select's drop down.
     * <p>
     * Default is {@code null} which means that the item's {@link #toString()}
     * method is used and set as the text content of the vaadin item element.
     *
     * @param renderer
     *            the item renderer, or {@code null} to clear
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> renderer) {
        this.itemRenderer = renderer;
        refreshItems();
    }

    /**
     * Convenience setter for creating a {@link TextRenderer} from the given
     * function that converts the item to a string.
     * <p>
     * <em>NOTE:</em> even though this accepts an {@link ItemLabelGenerator},
     * this is not the same as
     * {@link #setItemLabelGenerator(ItemLabelGenerator)} which does a different
     * thing.
     *
     * @param itemLabelGenerator
     *            the function that creates the text content from the item, not
     *            {@code null}
     */
    public void setTextRenderer(ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator);
        setRenderer(new TextRenderer<>(itemLabelGenerator));
    }

    /**
     * Sets whether the user is allowed to select nothing. When set {@code true}
     * a special empty item is shown to the user.
     * <p>
     * Default is {@code false}. The empty selection item can be customized with
     * {@link #setEmptySelectionCaption(String)}.
     *
     * @param emptySelectionAllowed
     *            {@code true} to allow not selecting anything, {@code false} to
     *            require selection
     * @see #setEmptySelectionCaption(String)
     */
    public void setEmptySelectionAllowed(boolean emptySelectionAllowed) {
        if (isEmptySelectionAllowed() == emptySelectionAllowed) {
            return;
        }
        if (isEmptySelectionAllowed()) {
            removeEmptySelectionItem();
        } else {
            addEmptySelectionItem();
        }
        this.emptySelectionAllowed = emptySelectionAllowed;
    }

    /**
     * Returns whether the user is allowed to select nothing.
     *
     * @return {@code true} if empty selection is allowed, {@code false}
     *         otherwise
     */
    public boolean isEmptySelectionAllowed() {
        return emptySelectionAllowed;
    }

    /**
     * Sets the empty selection caption when
     * {@link #setEmptySelectionAllowed(boolean)} has been enabled. The caption
     * is shown for the empty selection item in the drop down.
     * <p>
     * When the empty selection item is selected, the select shows the value
     * provided by {@link #setItemLabelGenerator(ItemLabelGenerator)} for the
     * {@code null} item, or the string set with {@link #setPlaceholder(String)}
     * or an empty string if not placeholder is set.
     * <p>
     * Default is an empty string "", which will show the place holder when
     * selected.
     *
     * @param emptySelectionCaption
     *            the empty selection caption to set, not {@code null}
     * @see #setEmptySelectionAllowed(boolean)
     */
    public void setEmptySelectionCaption(String emptySelectionCaption) {
        Objects.requireNonNull(emptySelectionCaption,
                "Empty selection caption must not be null");

        this.emptySelectionCaption = emptySelectionCaption;

        if (emptySelectionItem != null) {
            updateItem(emptySelectionItem);
        }
    }

    public String getEmptySelectionCaption() {
        return emptySelectionCaption == null ? "" : emptySelectionCaption;
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate or {@code null} if not set
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    /**
     * Sets the item enabled predicate for this select. The predicate is applied
     * to each item to determine whether the item should be enabled
     * ({@code true}) or disabled ({@code false}). Disabled items are displayed
     * as grayed out and the user cannot select them.
     * <p>
     * By default is {@code null} and all the items are enabled.
     *
     * @param itemEnabledProvider
     *            the item enable predicate or {@code null} to clear
     */
    public void setItemEnabledProvider(
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = itemEnabledProvider;
        refreshItems();
    }

    /**
     * Gets the item label generator. It generates the text that is shown in the
     * input part for the item when it has been selected.
     * <p>
     * Default is {@code null}.
     *
     * @return the item label generator, {@code null} if not set
     */
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Sets the item label generator. It generates the text that is shown in the
     * input part for the item when it has been selected.
     * <p>
     * Default is {@code null} and the text content generated for the item with
     * {@link #setRenderer(ComponentRenderer)} is used instead.
     *
     * @param itemLabelGenerator
     *            the item label generator to set, or {@code null} to clear
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<T> itemLabelGenerator) {
        this.itemLabelGenerator = itemLabelGenerator;
        refreshItems();
    }

    /**
     * Sets the placeholder hint for the user.
     * <p>
     * The placeholder will be displayed in the case that there is no item
     * selected, or the selected item has an empty string label, or the selected
     * item has no label and it's DOM content is empty.
     * <p>
     * Default value is {@code null}.
     *
     * @param placeholder
     *            the placeholder to set, or {@code null} to remove
     */
    public void setPlaceholder(String placeholder) {
        HasPlaceholder.super.setPlaceholder(placeholder);
    }

    /**
     * Sets the string for the label element.
     * <p>
     * <em>NOTE:</em> the label must be set for the required indicator to be
     * visible.
     *
     * @param label
     *            string or {@code null} to clear it
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the string for the label element.
     *
     * @return the label string, or {@code null} if not set
     */
    public String getLabel() {
        return getElement().getProperty("label");
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
    public void setAriaLabelledBy(String ariaLabelledBy) {
        getElement().setProperty("accessibleNameRef", ariaLabelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Sets the select to have focus when the page loads.
     * <p>
     * Default is {@code false}.
     *
     * @param autofocus
     *            the autofocus to set
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Gets whether this select has been set to autofocus when the page loads.
     *
     * @return {@code true} if set to autofocus, {@code false} if not
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * Defines whether the overlay should overlap the input element in the
     * y-axis, or be positioned right above/below it.
     *
     * @param noVerticalOverlap
     *            whether the overlay should overlap the input element
     */
    public void setNoVerticalOverlap(boolean noVerticalOverlap) {
        getElement().setProperty("noVerticalOverlap", noVerticalOverlap);
    }

    /**
     * Returns whether the overlay should overlap the input element
     *
     * @return {@code true} if the overlay should overlap the input element,
     *         {@code false} otherwise
     */
    public boolean isNoVerticalOverlap() {
        return getElement().getProperty("noVerticalOverlap", false);
    }

    /**
     * Sets a generic data provider for the Select to use.
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
        reset();

        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(this::onDataChange);
    }

    /**
     * Gets the data provider used by this Select.
     *
     * <p>
     * To get information and control over the items in the Select, use either
     * {@link #getListDataView()} or {@link #getGenericDataView()} instead.
     *
     * @return the data provider used by this Select
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider.get();
    }

    @Override
    public SelectDataView<T> setItems(DataProvider<T, Void> dataProvider) {
        this.setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public SelectDataView<T> setItems(
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
    public SelectListDataView<T> setItems(ListDataProvider<T> dataProvider) {
        this.setDataProvider(dataProvider);
        return getListDataView();
    }

    /**
     * Gets the generic data view for the {@link Select}. This data view should
     * only be used when {@link #getListDataView()} is not applicable for the
     * underlying data provider.
     *
     * @return the generic DataView instance implementing {@link Select}
     */
    @Override
    public SelectDataView<T> getGenericDataView() {
        return new SelectDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged);
    }

    /**
     * Gets the list data view for the {@link Select}. This data view should
     * only be used when the items are in-memory and set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory, an exception is thrown.
     *
     * @return the list data view that provides access to the data bound to the
     *         {@link Select}
     */
    @Override
    public SelectListDataView<T> getListDataView() {
        return new SelectListDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged, (filter, sorting) -> reset());
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        getElement().setProperty("disabled", !enabled);
        getItems().forEach(this::updateItemEnabled);
    }

    /**
     * Sets whether the user is required to provide a value. When required, an
     * indicator appears next to the label and the field invalidates if the
     * value is cleared.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to provide a value.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <em>NOTE:</em> If you add a component with the {@code slot} attribute
     * set, it will be placed in the light-dom of the {@code vaadin-select}
     * instead of the dropdown.
     */
    @Override
    public void add(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            if (component.getElement().hasAttribute("slot")) {
                HasItemComponents.super.add(component);
            } else {
                listBox.add(component);
            }
        }
    }

    @Override
    public void addComponents(T afterItem, Component... components) {
        listBox.addComponents(afterItem, components);
    }

    @Override
    public void prependComponents(T beforeItem, Component... components) {
        listBox.prependComponents(beforeItem, components);
    }

    @Override
    public int getItemPosition(T item) {
        return listBox.getItemPosition(item);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <em>NOTE:</em> If you add a component with the {@code slot} attribute
     * set, it will be placed in the light-dom of the {@code vaadin-select}
     * instead of the dropdown.
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        Objects.requireNonNull(component, "Component should not be null");
        if (component.getElement().hasAttribute("slot")) {
            HasItemComponents.super.addComponentAtIndex(index, component);
        } else {
            listBox.addComponentAtIndex(index, component);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <em>NOTE:</em> If you add a component with the {@code slot} attribute
     * set, it will be placed in the light-dom of the {@code vaadin-select}
     * instead of the dropdown.
     */
    @Override
    public void addComponentAsFirst(Component component) {
        Objects.requireNonNull(component, "Component should not be null");
        if (component.getElement().hasAttribute("slot")) {
            HasItemComponents.super.addComponentAsFirst(component);
        } else {
            listBox.addComponentAsFirst(component);
        }
    }

    @Override
    public Stream<Component> getChildren() {
        // do not provide access to items or list box as touching those will
        // hurt
        return Stream.concat(
                super.getChildren().filter(component -> component != listBox),
                listBox.getChildren().filter(
                        component -> !(component instanceof VaadinItem)));
    }

    /**
     * Removes the given child components from this component.
     * <p>
     * <em>NOTE:</em> any component with the {@code slot} attribute will be
     * attempted to removed from the light dom of the vaadin-select, instead of
     * inside the options drop down.
     *
     * @param components
     *            the components to remove
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component
     */
    public void remove(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            if (component.getElement().hasAttribute("slot")) {
                if (getElement().equals(component.getElement().getParent())) {
                    component.getElement().removeAttribute("slot");
                    getElement().removeChild(component.getElement());
                }
            } else {
                listBox.remove(components);
            }
        }
    }

    /**
     * Removes all child components that are not items. To remove all items,
     * reset the data provider or use {@link #setItems(Object[])}.
     * <p>
     * <em>NOTE:</em> this will remove all non-items from the drop down and any
     * slotted components from vaadin-select's light dom.
     *
     * @see HasComponents#removeAll()
     */
    @Override
    public void removeAll() {
        // Only remove list box children that are not vaadin-item since it makes
        // no sense
        // to allow removing those, causing the component to be in flux state.
        // Also do not remove the list box but remove any slotted components
        // (see add())
        getChildren().forEach(this::remove);
    }

    /**
     * Sets the dropdown overlay width.
     *
     * @param width
     *            the new dropdown width. Pass in null to set the dropdown width
     *            back to the default value.
     */
    public void setOverlayWidth(String width) {
        getStyle().set("--vaadin-select-overlay-width", width);
    }

    /**
     * Sets the dropdown overlay width. Negative number implies unspecified size
     * (the dropdown width is reverted back to the default value).
     *
     * @param width
     *            the width of the dropdown.
     * @param unit
     *            the unit used for the dropdown.
     */
    public void setOverlayWidth(float width, Unit unit) {
        Objects.requireNonNull(unit, "Unit can not be null");
        setOverlayWidth(HasSize.getCssSize(width, unit));
    }

    /**
     * Set true to open the dropdown overlay.
     *
     * @param opened
     *            the boolean value to set
     */
    protected void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    /**
     * Whether the dropdown is opened or not.
     *
     * @return {@code true} if the drop-down is opened, {@code false} otherwise
     */
    @Synchronize(property = "opened", value = "opened-changed")
    protected boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    @Override
    protected boolean hasValidValue() {
        // this is not about whether the value is actually "valid",
        // this is about whether or not is something that should be committed to
        // the _value_ of this field. E.g, it might be a value that is
        // acceptable,
        // but the component status should still be _invalid_.
        String selectedKey = getElement().getProperty(VALUE_PROPERTY_NAME);
        T item = keyMapper.get(selectedKey);
        if (item == null) {
            return isEmptySelectionAllowed() && isItemEnabled(item);
        }

        return isItemEnabled(item);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
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

    private void initConnector() {
        runBeforeClientResponse(ui -> {
            ui.getPage().executeJs(
                    "window.Vaadin.Flow.selectConnector.initLazy($0)",
                    getElement());
            // connector init will handle first data setting
            resetPending = false;
        });
    }

    private boolean isItemEnabled(T item) {
        return itemEnabledProvider == null || itemEnabledProvider.test(item);
    }

    private Component createItem(T bean) {
        VaadinItem<T> item = new VaadinItem<>(keyMapper.key(bean), bean);
        updateItem(item);
        return item;
    }

    private void updateItem(VaadinItem<T> vaadinItem) {
        vaadinItem.removeAll();
        T item = vaadinItem.getItem();

        if (vaadinItem == emptySelectionItem) {
            vaadinItem.setText(emptySelectionCaption);
        } else if (getItemRenderer() != null) {
            vaadinItem.add(getItemRenderer().createComponent(item));
        } else if (getItemLabelGenerator() != null) {
            vaadinItem.setText(getItemLabelGenerator().apply(item));
        } else {
            vaadinItem.setText(item.toString());
        }

        if (getItemLabelGenerator() != null) {
            vaadinItem.getElement().setAttribute(LABEL_ATTRIBUTE,
                    getItemLabelGenerator().apply(item));
        } else if (item == emptySelectionItem) {
            vaadinItem.getElement().setAttribute(LABEL_ATTRIBUTE, "");
        } else {
            vaadinItem.getElement().removeAttribute(LABEL_ATTRIBUTE);
        }
        updateItemEnabled(vaadinItem);

        requestClientSideContentUpdateIfNotPending();
    }

    private void updateItemEnabled(VaadinItem<T> item) {
        boolean itemEnabled = isItemEnabled(item.getItem());
        boolean isDisabled = getElement().getProperty("disabled", false);
        boolean disabled = isDisabled || !itemEnabled;

        // The disabled attribute should be set when the item is disabled,
        // but not if only the select is disabled, because setting disabled
        // attribute clears the selected value of an item.
        item.getElement().setEnabled(!disabled);
        item.getElement().setAttribute("disabled", !itemEnabled);
    }

    private void refreshItems() {
        getItems().forEach(this::updateItem);
    }

    @SuppressWarnings("unchecked")
    private Stream<VaadinItem<T>> getItems() {
        return listBox.getChildren()
                .filter(component -> component instanceof VaadinItem)
                .map(child -> (VaadinItem<T>) child);
    }

    @SuppressWarnings("unchecked")
    private void reset() {
        keyMapper.removeAll();
        listBox.removeAll();
        clear();
        requestClientSideContentUpdateIfNotPending();

        if (isEmptySelectionAllowed()) {
            addEmptySelectionItem();
        }

        synchronized (dataProvider) {
            final AtomicInteger itemCounter = new AtomicInteger(0);
            getDataProvider().fetch(DataViewUtils.getQuery(this))
                    .map(item -> createItem((T) item)).forEach(component -> {
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

    private void requestClientSideContentUpdateIfNotPending() {

        // reset added at this point to avoid unnecessary selected item update
        if (!resetPending) {
            resetPending = true;
            runBeforeClientResponse(ui -> {
                ui.getPage().executeJs("$0.requestContentUpdate();",
                        getElement());
                resetPending = false;
            });
        }
    }

    private void onDataChange(DataChangeEvent<T> event) {
        if (event instanceof DataChangeEvent.DataRefreshEvent) {
            T updatedItem = ((DataChangeEvent.DataRefreshEvent<T>) event)
                    .getItem();
            IdentifierProvider<T> identifierProvider = getIdentifierProvider();
            Object updatedItemId = identifierProvider.apply(updatedItem);
            keyMapper.refresh(updatedItem);
            getItems()
                    .filter(vaadinItem -> updatedItemId.equals(
                            identifierProvider.apply(vaadinItem.getItem())))
                    .findAny().ifPresent(item -> {
                        item.setItem(updatedItem);
                        updateItem(item);
                    });
        } else {
            reset();
        }
    }

    private void addEmptySelectionItem() {
        if (emptySelectionItem == null) {
            emptySelectionItem = new VaadinItem<>("", null);
        }

        updateItem(emptySelectionItem);
        addComponentAsFirst(emptySelectionItem);
        if (getValue() == null) {
            setValue(null);
        }
    }

    private void removeEmptySelectionItem() {
        if (emptySelectionItem != null) {
            listBox.remove(emptySelectionItem);
        }
        emptySelectionItem = null;
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

    @SuppressWarnings("unchecked")
    private IdentifierProvider<T> getIdentifierProvider() {
        IdentifierProvider<T> identifierProviderObject = ComponentUtil
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

    private Object getItemId(T item) {
        return getIdentifierProvider().apply(item);
    }

    private void identifierProviderChanged(
            IdentifierProvider<T> identifierProvider) {
        keyMapper.setIdentifierGetter(identifierProvider);
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
     * {@code opened-changed} event is sent when the overlay opened state
     * changes.
     */
    public static class OpenedChangeEvent extends ComponentEvent<Select> {
        private final boolean opened;

        public OpenedChangeEvent(Select source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener for {@code opened-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    protected Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
    }

    /**
     * {@code invalid-changed} event is sent when the invalid state changes.
     */
    public static class InvalidChangeEvent extends ComponentEvent<Select> {
        private final boolean invalid;

        public InvalidChangeEvent(Select source, boolean fromClient) {
            super(source, fromClient);
            this.invalid = source.isInvalid();
        }

        public boolean isInvalid() {
            return invalid;
        }
    }

    /**
     * Adds a listener for {@code invalid-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    protected Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent> listener) {
        return addListener(InvalidChangeEvent.class, listener);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using {@link #setI18n(SelectI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public SelectI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(SelectI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(Function<SelectI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link Select}.
     */
    public static class SelectI18n implements Serializable {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see Select#isRequiredIndicatorVisible()
         * @see Select#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link Select#setErrorMessage(String)} take priority over i18n error
         * messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see Select#isRequiredIndicatorVisible()
         * @see Select#setRequiredIndicatorVisible(boolean)
         */
        public SelectI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
