package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A component for performing <a href=
 * "https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a>
 * operations on a data backend (e.g entities from a database).
 *
 * <pre>
 * <u>Basic usage</u>
 *
 * {@code
 *   Crud<Person> crud = new Crud<>(Person.class, personEditor);
 *   crud.setDataProvider(personDataProvider);
 *
 *   // Handle save and delete events.
 *   crud.addSaveListener(e -> save(e.getItem()));
 *   crud.addDeleteListener(e -> delete(e.getItem()));
 * }
 * </pre>
 *
 * @param <E>
 *            the bean type
 * @author Vaadin Ltd
 */
@Tag("vaadin-crud")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/crud", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-crud", version = "23.1.0-beta1")
@JsModule("@vaadin/crud/src/vaadin-crud.js")
@JsModule("@vaadin/crud/src/vaadin-crud-edit-column.js")
public class Crud<E> extends Component implements HasSize, HasTheme, HasStyle {

    private static final String EDIT_COLUMN_KEY = "vaadin-crud-edit-column";
    private static final String EVENT_PREVENT_DEFAULT_JS = "event.preventDefault()";
    private static final String FORM_SLOT_NAME = "form";
    private static final String GRID_SLOT_NAME = "grid";
    private static final String SLOT_KEY = "slot";
    private static final String TOOLBAR_SLOT_NAME = "toolbar";

    private final Set<ComponentEventListener<NewEvent<E>>> newListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<EditEvent<E>>> editListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<SaveEvent<E>>> saveListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<CancelEvent<E>>> cancelListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<DeleteEvent<E>>> deleteListeners = new LinkedHashSet<>();

    private Class<E> beanType;
    private Grid<E> grid;
    private CrudEditor<E> editor;
    private E gridActiveItem;
    private boolean toolbarVisible = true;
    private boolean saveBtnDisabledOverridden;

    final private Button saveButton;

    final private Button cancelButton;

    final private Button deleteButton;

    /**
     * Instantiates a new Crud using a custom grid.
     *
     * @param beanType
     *            the class of items
     * @param grid
     *            the grid with which the items listing should be displayed
     * @param editor
     *            the editor for manipulating individual items
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, CrudEditor)
     *      Crud(Class, CrudEditor)
     */
    public Crud(Class<E> beanType, Grid<E> grid, CrudEditor<E> editor) {
        this();

        setGrid(grid);
        setEditor(editor);
        setBeanType(beanType);
    }

    /**
     * Instantiates a new Crud for the given bean type and uses the supplied
     * editor. Furthermore, it displays the items using the built-in grid.
     *
     * @param beanType
     *            the class of items
     * @param editor
     *            the editor for manipulating individual items
     * @see CrudGrid
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, Grid, CrudEditor)
     *      Crud(Class, Grid, CrudEditor)
     */
    public Crud(Class<E> beanType, CrudEditor<E> editor) {
        this();

        setEditor(editor);
        setBeanType(beanType);
    }

    /**
     * Instantiates a new Crud with no grid, editor and runtime bean type
     * information. The editor and bean type must be initialized before a Crud
     * is put into full use therefore this constructor only exists for partial
     * initialization in order to support template binding.
     *
     * <pre>
     * Example:
     * <code>
     *    &#064;Id
     *    Crud&lt;Person&gt; crud;
     *
     *    &#064;Id
     *    private TextField firstName;
     *
     *    &#064;Id
     *    private TextField lastName;
     *
     *    &#064;Override
     *    protected void onAttach(AttachEvent attachEvent) {
     *        super.onAttach(attachEvent);
     *
     *        Binder&lt;Person&gt; binder = new Binder&lt;&gt;(Person.class);
     *        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
     *        binder.bind(lastName, Person::getLastName, Person::setLastName);
     *
     *        crud.setEditor(new BinderCrudEditor&lt;&gt;(binder));
     *        crud.setBeanType(Person.class);
     *
     *        crud.setDataProvider(new PersonCrudDataProvider());
     *    }
     * </code>
     * </pre>
     *
     * @see #setEditor(CrudEditor)
     * @see #setBeanType(Class)
     */
    public Crud() {
        setI18n(CrudI18n.createDefault(), false);
        registerHandlers();

        saveButton = new SaveButton();
        saveButton.getElement().setAttribute("slot", "save-button");
        saveButton.addThemeName("primary");
        getElement().appendChild(saveButton.getElement());

        cancelButton = new Button();
        cancelButton.getElement().setAttribute("slot", "cancel-button");
        cancelButton.addThemeName("tertiary");
        getElement().appendChild(cancelButton.getElement());

        deleteButton = new Button();
        deleteButton.getElement().setAttribute("slot", "delete-button");
        deleteButton.addThemeNames("tertiary", "error");
        getElement().appendChild(deleteButton.getElement());
    }

    private class SaveButton extends Button {
        @Override
        public void onEnabledStateChanged(boolean enabled) {
            super.onEnabledStateChanged(enabled);
            saveBtnDisabledOverridden = true;
            overrideSaveDisabled(enabled);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (saveBtnDisabledOverridden) {
            overrideSaveDisabled(getSaveButton().isEnabled());
        }
        getElement().executeJs("this.__validate = function () {return true;}");
    }

    private void overrideSaveDisabled(boolean enabled) {
        getElement().executeJs("this.__isSaveBtnDisabled = () => {return $0;}",
                !enabled);
    }

    private void registerHandlers() {
        ComponentUtil.addListener(this, NewEvent.class,
                (ComponentEventListener) ((ComponentEventListener<NewEvent<E>>) e -> {
                    try {
                        getEditor().setItem(e.getItem() != null ? e.getItem()
                                : getBeanType().newInstance());
                        clearActiveItem();
                        setClientIsNew(true);
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Unable to instantiate new bean", ex);
                    }

                    NewEvent eventWithNewItem = new NewEvent(e.getSource(),
                            e.isFromClient(), getEditor().getItem(), null);
                    newListeners.forEach(listener -> listener
                            .onComponentEvent(eventWithNewItem));
                }));

        ComponentUtil.addListener(this, EditEvent.class,
                (ComponentEventListener) ((ComponentEventListener<EditEvent<E>>) e -> {
                    if (getEditor().getItem() != e.getItem()) {
                        getEditor().setItem(e.getItem(), true);
                        setOpened(true);
                        setClientIsNew(false);
                        if (isEditOnClick() && getGrid() instanceof CrudGrid) {
                            getGrid().select(e.getItem());
                        }
                    }

                    editListeners
                            .forEach(listener -> listener.onComponentEvent(e));
                }));

        ComponentUtil.addListener(this, CancelEvent.class,
                (ComponentEventListener) ((ComponentEventListener<CancelEvent<E>>) e -> {
                    cancelListeners
                            .forEach(listener -> listener.onComponentEvent(e));
                    if ((this.gridActiveItem != null && this.getEditor()
                            .getItem() == this.gridActiveItem)
                            || this.gridActiveItem == null) {
                        setOpened(false);
                        getEditor().clear();
                        clearActiveItem();
                    }
                }));

        ComponentUtil.addListener(this, SaveEvent.class,
                (ComponentEventListener) ((ComponentEventListener<SaveEvent<E>>) e -> {
                    if (!getEditor().validate()) {
                        return;
                    }

                    getEditor().writeItemChanges();
                    try {
                        saveListeners.forEach(
                                listener -> listener.onComponentEvent(e));
                        setOpened(false);
                        getEditor().clear();
                    } finally {
                        if (getGrid().getDataProvider() != null) {
                            getGrid().getDataProvider().refreshAll();
                        }
                    }
                }));

        ComponentUtil.addListener(this, DeleteEvent.class,
                (ComponentEventListener) ((ComponentEventListener<DeleteEvent<E>>) e -> {
                    try {
                        deleteListeners.forEach(
                                listener -> listener.onComponentEvent(e));
                        setOpened(false);
                        getEditor().clear();
                    } finally {
                        getGrid().getDataProvider().refreshAll();
                    }
                }));
    }

    /**
     * Initiates an item edit from the server-side. This sets the supplied item
     * as the working bean and opens the edit dialog.
     *
     * @param item
     *            the item to be edited
     * @param editMode
     *            the edit mode
     */
    public void edit(E item, EditMode editMode) {
        final CrudEvent<E> event;
        if (editMode == EditMode.NEW_ITEM) {
            event = new NewEvent<>(this, false, item, null);
        } else {
            setDirty(false);
            event = new EditEvent<>(this, false, item);
        }

        setOpened(true);
        ComponentUtil.fireEvent(this, event);
    }

    private void setClientIsNew(boolean isNew) {
        getElement().setProperty("__isNew", isNew);
    }

    /**
     * Opens or closes the editor. In most use cases opening or closing the
     * editor is automatically done by the component and this method does not
     * need to be called.
     *
     * @param opened
     *            true to open or false to close
     */
    public void setOpened(boolean opened) {
        getElement().callFunction("set", "editorOpened", opened);
    }

    /**
     * Set the dirty state of the Crud.
     * <p>
     * A dirty Crud has its editor Save button enabled. Ideally a Crud
     * automatically detects if it is dirty based on interactions with the form
     * fields within it but in some special cases (e.g with composites) this
     * might not be automatically detected. For such cases this method could be
     * used to explicitly set the dirty state of the Crud editor.
     * <p>
     * NOTE: editor Save button will not be automatically enabled in case its
     * enabled state was changed with {@link Crud#getSaveButton()}
     *
     * @param dirty
     *            true if dirty and false if otherwise.
     * @see #getSaveButton()
     */
    public void setDirty(boolean dirty) {
        getElement().executeJs("this.set('__isDirty', $0)", dirty);
    }

    /**
     * Gets the runtime bean type information
     *
     * @return the bean type
     */
    public Class<E> getBeanType() {
        if (beanType == null) {
            throw new IllegalStateException(
                    "The bean type must be initialized before event processing");
        }

        return beanType;
    }

    /**
     * Sets the runtime bean type information. If no grid exists a built-in grid
     * is created since the bean type information is now known. When injecting a
     * {@link Crud} with {@literal @}Id this method must be called before the
     * crud is put into use.
     *
     * @param beanType
     *            the bean type
     */
    public void setBeanType(Class<E> beanType) {
        Objects.requireNonNull(beanType, "Bean type cannot be null");

        this.beanType = beanType;

        if (this.grid == null) {
            setGrid(new CrudGrid<>(beanType, true, true));
        }
    }

    /**
     * Gets the grid
     *
     * @return the grid
     */
    public Grid<E> getGrid() {
        if (grid == null) {
            throw new IllegalStateException(
                    "The grid must be initialized before event processing");
        }

        return grid;
    }

    /**
     * Sets the grid
     *
     * @param grid
     *            the grid
     */
    public void setGrid(Grid<E> grid) {
        Objects.requireNonNull(grid, "Grid cannot be null");

        if (this.grid != null
                && getElement().equals(this.grid.getElement().getParent())) {
            this.grid.getElement().removeFromParent();
        }

        this.grid = grid;
        grid.getElement().setAttribute(SLOT_KEY, GRID_SLOT_NAME);

        // It might already have a parent e.g when injected from a template
        if (grid.getElement().getParent() == null) {
            getElement().appendChild(grid.getElement());
        }
    }

    /**
     * Gets the crud editor.
     *
     * @return the crud editor
     */
    public CrudEditor<E> getEditor() {
        if (editor == null) {
            throw new IllegalStateException(
                    "The editor must be initialized before event processing");
        }

        return editor;
    }

    /**
     * Sets the editor. When injecting a {@link Crud} with {@literal @}Id this
     * method must be called before the crud is put into use.
     *
     * @param editor
     *            the editor
     */
    public void setEditor(CrudEditor<E> editor) {
        Objects.requireNonNull(editor, "Editor cannot be null");

        if (this.editor != null && this.editor.getView() != null && this.editor
                .getView().getElement().getParent() == getElement()) {
            this.editor.getView().getElement().removeFromParent();
        }

        this.editor = editor;

        // It might already have a parent e.g when injected from a template
        if (editor.getView() != null
                && editor.getView().getElement().getParent() == null) {
            editor.getView().getElement().setAttribute(SLOT_KEY,
                    FORM_SLOT_NAME);
            getElement().appendChild(editor.getView().getElement());
        }
    }

    /**
     * Sets how editor will be presented on desktop screen.
     * <p>
     * The default position is {@link CrudEditorPosition#OVERLAY}.
     *
     * @param editorPosition
     *            the editor position, never <code>null</code>
     * @see CrudEditorPosition
     */
    public void setEditorPosition(CrudEditorPosition editorPosition) {
        if (editorPosition == null) {
            throw new IllegalArgumentException(
                    "The 'editorPosition' argument can not be null");
        }
        getElement().setProperty("editorPosition",
                editorPosition.getEditorPosition());
    }

    /**
     * Gets the current editor position on the desktop screen.
     * <p>
     * The default position is {@link CrudEditorPosition#OVERLAY}.
     *
     * @return the editor position
     */
    public CrudEditorPosition getEditorPosition() {
        return CrudEditorPosition.toPosition(
                getElement().getProperty("editorPosition", ""),
                CrudEditorPosition.OVERLAY);
    }

    private Registration gridItemClickRegistration;

    /**
     * Sets the option to open item to edit by row click.
     * <p>
     * If enabled, it removes the edit column created by {@link CrudGrid}.
     *
     * @param editOnClick
     *            {@code true} to enable it ({@code false}, by default).
     */
    public void setEditOnClick(boolean editOnClick) {
        getElement().setProperty("editOnClick", editOnClick);
        Grid<E> grid = getGrid();

        if (editOnClick) {
            if (getGrid() instanceof CrudGrid) {
                grid.setSelectionMode(Grid.SelectionMode.SINGLE);

                if (hasEditColumn(grid)) {
                    removeEditColumn(grid);
                }
            }
            gridItemClickRegistration = grid.addItemClickListener(
                    e -> this.gridActiveItem = e.getItem());
        } else if (gridItemClickRegistration != null) {
            clearActiveItem();
            gridItemClickRegistration.remove();
            if (grid instanceof CrudGrid) {
                addEditColumn(grid);
                grid.setSelectionMode(Grid.SelectionMode.NONE);
            }
        }
    }

    /**
     * Gets whether click on row to edit item is enabled or not.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEditOnClick() {
        return getElement().getProperty("editOnClick", false);
    }

    private void clearActiveItem() {
        this.gridActiveItem = null;
        grid.deselectAll();
    }

    /**
     * Sets the content of the toolbar. Any content with the attribute
     * `new-button` triggers a new item creation.
     *
     * @param components
     *            the content to be set
     */
    public void setToolbar(Component... components) {
        final Element[] existingToolbarElements = getElement().getChildren()
                .filter(e -> TOOLBAR_SLOT_NAME.equals(e.getAttribute(SLOT_KEY)))
                .toArray(Element[]::new);
        getElement().removeChild(existingToolbarElements);

        final Element[] newToolbarElements = Arrays.stream(components)
                .map(Component::getElement)
                .map(e -> e.setAttribute(SLOT_KEY, TOOLBAR_SLOT_NAME))
                .toArray(Element[]::new);
        getElement().appendChild(newToolbarElements);
    }

    /**
     * Sets the internationalized messages to be used by this crud instance.
     *
     * @param i18n
     *            the internationalized messages
     * @see CrudI18n#createDefault()
     */
    public void setI18n(CrudI18n i18n) {
        setI18n(i18n, true);
    }

    private void setI18n(CrudI18n i18n, boolean fireEvent) {
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
        if (fireEvent) {
            ComponentUtil.fireEvent(this.grid,
                    new CrudI18nUpdatedEvent(this, false, i18n));
        }
    }

    /**
     * Controls visiblity of toolbar
     *
     * @param value
     */
    public void setToolbarVisible(boolean value) {
        toolbarVisible = value;
        if (value) {
            getElement().setProperty("noToolbar", false);
        } else {
            getElement().setProperty("noToolbar", true);
        }
    }

    /**
     * Gets visiblity state of toolbar
     *
     * @param
     * @return true if toolbar is visible false otherwise
     */
    public boolean getToolbarVisible() {
        return toolbarVisible;
    }

    /**
     * Gets the Crud editor delete button
     *
     * @return the delete button
     */
    public Button getDeleteButton() {
        return deleteButton;
    }

    /**
     * Gets the Crud save button
     * <p>
     * NOTE: State of the button set with
     * {@link com.vaadin.flow.component.HasEnabled#setEnabled(boolean)} will
     * remain even if dirty state of the crud changes
     *
     * @return the save button
     * @see Crud#setDirty(boolean)
     */
    public Button getSaveButton() {
        return saveButton;
    }

    /**
     * Gets the Crud cancel button
     *
     * @return the cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(CrudVariant... variants) {
        List<String> variantNames = variantNames(variants);
        getThemeNames().addAll(variantNames);

        if (grid instanceof CrudGrid) {
            ((CrudGrid) grid).addCrudThemeVariants(variantNames);
        }
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(CrudVariant... variants) {
        List<String> variantNames = variantNames(variants);
        getThemeNames().removeAll(variantNames);

        if (grid instanceof CrudGrid) {
            ((CrudGrid) grid).removeCrudThemeVariants(variantNames);
        }
    }

    private static List<String> variantNames(CrudVariant... variants) {
        return Arrays.stream(variants).map(CrudVariant::getVariantName)
                .collect(Collectors.toList());
    }

    /**
     * Registers a listener to be notified when the user starts to create a new
     * item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addNewListener(
            ComponentEventListener<NewEvent<E>> listener) {
        newListeners.add(listener);
        return () -> newListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user starts to edit an
     * existing item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addEditListener(
            ComponentEventListener<EditEvent<E>> listener) {
        editListeners.add(listener);
        return () -> editListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user tries to save a new
     * item or modifications to an existing item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addSaveListener(
            ComponentEventListener<SaveEvent<E>> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user cancels a new item
     * creation or existing item modification in progress.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addCancelListener(
            ComponentEventListener<CancelEvent<E>> listener) {
        cancelListeners.add(listener);
        return () -> cancelListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user tries to delete an
     * existing item.
     *
     * @param listener
     *            a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addDeleteListener(
            ComponentEventListener<DeleteEvent<E>> listener) {
        deleteListeners.add(listener);
        return () -> deleteListeners.remove(listener);
    }

    /**
     * Gets the data provider supplying the grid data.
     *
     * @return the data provider for the grid
     */
    public DataProvider<E, ?> getDataProvider() {
        return grid.getDataProvider();
    }

    /**
     * Sets the data provider for the grid.
     *
     * @param provider
     *            the data provider for the grid
     */
    public void setDataProvider(DataProvider<E, ?> provider) {
        grid.setDataProvider(provider);
    }

    /**
     * A helper method to add an edit column to a grid. Clicking on the edit
     * cell for a row opens the item for editing in the editor.
     *
     * @param grid
     *            the grid in which to add the edit column
     * @see #addEditColumn(Grid, CrudI18n)
     * @see #removeEditColumn(Grid)
     * @see #hasEditColumn(Grid)
     */
    public static void addEditColumn(Grid grid) {
        addEditColumn(grid, CrudI18n.createDefault());
    }

    /**
     * A helper method to add an edit column to a grid. Clicking on the edit
     * cell for a row opens the item for editing in the editor. Additionally,
     * the i18n object is used for setting the aria-label for the button,
     * improving accessibility.
     *
     * @param grid
     *            the grid in which to add the edit column
     * @param crudI18n
     *            the i18n object for localizing the accessibility of the edit
     *            column
     */
    public static void addEditColumn(Grid grid, CrudI18n crudI18n) {
        grid.addColumn(TemplateRenderer.of(createEditColumnTemplate(crudI18n)))
                .setKey(EDIT_COLUMN_KEY).setWidth("4em").setFlexGrow(0);
    }

    private static String createEditColumnTemplate(CrudI18n crudI18n) {
        return "<vaadin-crud-edit aria-label=\"" + crudI18n.getEditLabel()
                + "\"></vaadin-crud-edit>";
    }

    /**
     * Removes the crud edit column from a grid
     *
     * @param grid
     *            the grid from which to remove the edit column
     * @see #addEditColumn(Grid)
     * @see #hasEditColumn(Grid)
     */
    public static void removeEditColumn(Grid grid) {
        grid.removeColumnByKey(EDIT_COLUMN_KEY);
    }

    /**
     * Checks if an edit column has been added to the Grid using
     * {@code Crud.addEditColumn(Grid)}
     *
     * @param grid
     *            the grid to check
     * @return true if an edit column is present or false if otherwise
     * @see Crud#addEditColumn(Grid)
     */
    public static boolean hasEditColumn(Grid grid) {
        return grid.getColumnByKey(EDIT_COLUMN_KEY) != null;
    }

    /**
     * The base class for all Crud events.
     *
     * @param <E>
     *            the bean type
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, CrudEditor)
     *      Crud(Class, CrudEditor)
     */
    static abstract class CrudEvent<E> extends ComponentEvent<Crud<E>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         */
        private CrudEvent(Crud<E> source, boolean fromClient) {
            super(source, fromClient);
        }

        /**
         * Gets the item being currently edited.
         *
         * @return the item being currently edited
         */
        public E getItem() {
            return getSource().getEditor().getItem();
        }
    }

    /**
     * Event fired when the user cancels the creation of a new item or
     * modifications to an existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("cancel")
    public static class CancelEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param ignored
         *            an ignored parameter for a side effect
         */
        public CancelEvent(Crud<E> source, boolean fromClient,
                @EventData(EVENT_PREVENT_DEFAULT_JS) Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when the user tries to delete an existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("delete")
    public static class DeleteEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param ignored
         *            an ignored parameter for a side effect
         */
        public DeleteEvent(Crud<E> source, boolean fromClient,
                @EventData(EVENT_PREVENT_DEFAULT_JS) Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when the user starts to edit an existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("edit")
    public static class EditEvent<E> extends CrudEvent<E> {

        private E item;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param item
         *            the item to be edited, provided in JSON as internally
         *            represented in Grid
         * @param ignored
         *            an ignored parameter for a side effect
         */
        public EditEvent(Crud<E> source, boolean fromClient,
                @EventData("event.detail.item") JsonObject item,
                @EventData(EVENT_PREVENT_DEFAULT_JS) Object ignored) {
            super(source, fromClient);
            this.item = source.getGrid().getDataCommunicator().getKeyMapper()
                    .get(item.getString("key"));
        }

        private EditEvent(Crud<E> source, boolean fromClient, E item) {
            super(source, fromClient);
            this.item = item;
        }

        @Override
        public E getItem() {
            return item;
        }
    }

    /**
     * Event fired when the user starts to create a new item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("new")
    public static class NewEvent<E> extends CrudEvent<E> {

        private E item;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param ignored
         *            an ignored parameter for a side effect
         */
        public NewEvent(Crud<E> source, boolean fromClient,
                @EventData(EVENT_PREVENT_DEFAULT_JS) Object ignored) {
            super(source, fromClient);
        }

        /**
         * Private constructor for server-initiated edits
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param item
         *            the item to be edited
         * @param ignored
         *            only present to workaround Java generics erasure (since E
         *            also erases to Object and clashes with the other
         *            constructor)
         */
        private NewEvent(Crud<E> source, boolean fromClient, E item,
                Object ignored) {
            super(source, fromClient);
            this.item = item;
        }

        /**
         * Gets new item being created
         *
         * @return a new instance of bean type
         */
        @Override
        public E getItem() {
            return item;
        }
    }

    /**
     * Event fired when the user tries to save a new item or modifications to an
     * existing item.
     *
     * @param <E>
     *            the bean type
     */
    @DomEvent("save")
    public static class SaveEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            <code>true</code> if the event originated from the client
         * @param ignored
         *            an ignored parameter for a side effect
         */
        public SaveEvent(Crud<E> source, boolean fromClient,
                @EventData(EVENT_PREVENT_DEFAULT_JS) Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Determines whether an item presented for editing is to be treated as a
     * new item or an existing item.
     *
     * @see Crud#edit(Object, EditMode)
     */
    public enum EditMode {
        /**
         * The item presented for editing should be treated as a new item.
         */
        NEW_ITEM,

        /**
         * The item presented for editing should be treated as an existing item.
         */
        EXISTING_ITEM
    }
}
