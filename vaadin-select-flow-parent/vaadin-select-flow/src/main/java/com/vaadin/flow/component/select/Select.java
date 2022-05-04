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
package com.vaadin.flow.component.select;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.select.data.SelectDataView;
import com.vaadin.flow.component.select.data.SelectListDataView;
import com.vaadin.flow.component.select.generated.GeneratedVaadinSelect;
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Select allows users to choose a single value from a list of options presented
 * in an overlay. The dropdown can be opened with a click, up/down arrow keys,
 * or by typing the initial character for one of the options.
 *
 * @param <T>
 *            the type of the items for the select
 * @author Vaadin Ltd.
 */
@JsModule("./selectConnector.js")
public class Select<T> extends GeneratedVaadinSelect<Select<T>, T>
        implements HasItemComponents<T>, HasSize, HasValidation,
        SingleSelect<Select<T>, T>, HasListDataView<T, SelectListDataView<T>>,
        HasDataView<T, Void, SelectDataView<T>>, HasHelper, HasLabel, HasTheme {

    public static final String LABEL_ATTRIBUTE = "label";

    private static final String VALUE_PROPERTY_NAME = "value";

    private final InternalListBox listBox = new InternalListBox();

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private ComponentRenderer<? extends Component, T> itemRenderer;

    private SerializablePredicate<T> itemEnabledProvider = null;

    private ItemLabelGenerator<T> itemLabelGenerator = null;

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private boolean resetPending = true;

    private boolean emptySelectionAllowed;

    private String emptySelectionCaption;

    private VaadinItem<T> emptySelectionItem;

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    /**
     * Constructs a select.
     */
    public Select() {
        super(null, null, String.class, Select::presentationToModel,
                Select::modelToPresentation, true);

        getElement().setProperty("invalid", false);
        getElement().setProperty("opened", false);
        getElement().setAttribute("suppress-template-warning", true);
        // Trigger model-to-presentation conversion in constructor, so that
        // the client side component has a correct initial value of an empty
        // string
        setPresentationValue(null);

        getElement().appendChild(listBox.getElement());

        registerValidation();
    }

    /**
     * Constructs a select with the given items.
     *
     * @param items
     *            the items for the select
     * @see #setItems(Object...)
     * @deprecated as of 23.1. Please use {@link #setItems(Object[])} instead.
     */
    @Deprecated
    @SafeVarargs
    public Select(T... items) {
        this();

        setItems(items);
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
        if (select.keyMapper == null
                || !select.keyMapper.containsKey(presentation)) {
            return null;
        }
        return select.keyMapper.get(presentation);
    }

    private static <T> String modelToPresentation(Select<T> select, T model) {
        if (model == null || select.keyMapper == null) {
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
    @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
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
     * Gets the placeholder hint set for the user.
     *
     * @return the placeholder or {@code null} if none set
     */
    public String getPlaceholder() {
        return super.getPlaceholderString();
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
    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
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
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the string for the label element.
     *
     * @return the label string, or {@code null} if not set
     */
    @Override
    public String getLabel() {
        return super.getLabelString();
    }

    /**
     * Sets the select to have focus when the page loads.
     * <p>
     * Default is {@code false}.
     *
     * @param autofocus
     *            the autofocus to set
     */
    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Gets whether this select has been set to autofocus when the page loads.
     *
     * @return {@code true} if set to autofocus, {@code false} if not
     */
    public boolean isAutofocus() {
        return super.isAutofocusBoolean();
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
     * {@inheritDoc}
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link SelectListDataView} or
     *             {@link SelectDataView}
     */
    @Deprecated
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
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
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
        setDisabled(!enabled);
        getItems().forEach(this::updateItemEnabled);
    }

    /**
     * {@inheritDoc}
     *
     * <em>NOTE:</em> The required indicator will not be visible, if the
     * {@link #setLabel(String)} property is not set for the select.
     */
    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // this would be the same as setRequired(boolean) but we don't expose
        // both
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    /**
     * {@inheritDoc}
     *
     * <em>NOTE:</em> The required indicator will not be visible, if the
     * {@link #setLabel(String)} property is not set for the select.
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredBoolean();
    }

    /**
     * Sets the error message to show to the user on invalid selection.
     *
     * @param errorMessage
     *            the error message or {@code null} to clear it
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Gets the error message to show to the user on invalid selection
     *
     * @return the error message or {@code null} if not set
     */
    @Override
    public String getErrorMessage() {
        return super.getErrorMessageString();
    }

    /**
     * Sets the select to show as invalid state and display error message.
     *
     * @param invalid
     *            {@code true} for invalid, {@code false} for valid
     */
    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    /**
     * Gets whether the select is currently in invalid state.
     *
     * @return {@code true} for invalid, {@code false} for valid
     */
    @Override
    public boolean isInvalid() {
        return super.isInvalidBoolean();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <em>NOTE:</em> If you add a component with the {@code slot} attribute
     * set, it will be placed in the light-dom of the {@code vaadin-select}
     * instead of the drop down, similar to {@link #addToPrefix(Component...)}
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

    /**
     * {@inheritDoc}
     * <p>
     * <em>NOTE:</em> If you add a component with the {@code slot} attribute
     * set, it will be placed in the light-dom of the {@code vaadin-select}
     * instead of the drop down, similar to {@link #addToPrefix(Component...)}
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
     * instead of the drop down, similar to {@link #addToPrefix(Component...)}
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
    public void addToPrefix(Component... components) {
        super.addToPrefix(components);
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
    @Override
    public void remove(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            if (component.getElement().hasAttribute("slot")) {
                super.remove(component);
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
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(SelectVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(SelectVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(SelectVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(SelectVariant::getVariantName)
                        .collect(Collectors.toList()));
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
        FieldValidationUtil.disableClientValidation(this);
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
        boolean disabled = isDisabledBoolean() || !itemEnabled;

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
            getItems()
                    .filter(vaadinItem -> updatedItemId.equals(
                            identifierProvider.apply(vaadinItem.getItem())))
                    .findAny().ifPresent(this::updateItem);
        } else {
            reset();
        }
    }

    private T getValue(Serializable key) {
        if (key == null || "".equals(key)) {
            return null;
        }
        return keyMapper.get(key.toString());
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

    private void validateSelectionEnabledState(PropertyChangeEvent event) {
        if (!event.isUserOriginated()) {
            return;
        }
        if (!hasValidValue() || isReadOnly()) {
            T oldValue = getValue(event.getOldValue());
            // return the value back on the client side
            try {
                validationRegistration.remove();
                getElement().setProperty(VALUE_PROPERTY_NAME,
                        keyMapper.key(oldValue));
            } finally {
                registerValidation();
            }
            // Now make sure that the item is still in the correct state
            Optional<VaadinItem<T>> selectedItem = getItems().filter(
                    item -> item.getItem() == getValue(event.getValue()))
                    .findFirst();

            selectedItem.ifPresent(this::updateItemEnabled);
        }
    }

    private void registerValidation() {
        if (validationRegistration != null) {
            validationRegistration.remove();
        }
        validationRegistration = getElement().addPropertyChangeListener(
                VALUE_PROPERTY_NAME, validationListener);
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

}
