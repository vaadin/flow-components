package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A component for performing <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a>
 * operations on a data backend (e.g entities from a database).<br>
 * <br>
 * <u>Basic usage</u><br>
 * <br>
 * <code>
 * Crud&lt;Person&gt; crud = new Crud&lt;&gt;(Person.class, personEditor);<br>
 * crud.setDataProvider(personDataProvider);<br>
 *  <br>
 * // Handle save and delete events.<br>
 * crud.addSaveListener(e -&gt; save(e.getItem()));<br>
 * crud.addDeleteListener(e -&gt; delete(e.getItem()));<br>
 *  <br>
 * // Set a footer text or component if desired.<br>
 * crud.setFooter("Flight manifest for XX210");<br>
 * </code>
 *
 * @author Vaadin Ltd
 *
 * @param <E> the bean type
 */
@Tag("vaadin-crud")
@HtmlImport("frontend://bower_components/vaadin-crud/src/vaadin-crud.html")
@HtmlImport("frontend://bower_components/vaadin-crud/src/vaadin-crud-grid-edit-column.html")
public class Crud<E> extends Component {

    private final Class<E> beanType;
    private final Grid<E> grid;
    private final CrudEditor<E> editor;
    private Component footer;

    private final Set<ComponentEventListener<NewEvent<E>>> newListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<EditEvent<E>>> editListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<SaveEvent<E>>> saveListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<CancelEvent<E>>> cancelListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<DeleteEvent<E>>> deleteListeners = new LinkedHashSet<>();

    /**
     * Instantiates a new Crud for the given bean type and uses the supplied editor.
     * Furthermore, it displays the items using the built-in grid.<br>
     * <br>
     * Example:<br>
     * <code>
     *     Crud&lt;Person&gt; crud = new Crud&lt;&gt;(Person.class, new PersonEditor());<br>
     * </code>
     *
     * @param beanType the class of items
     * @param editor the editor for manipulating individual items
     * @see CrudGrid
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, Grid, CrudEditor) Crud(Class, Grid, CrudEditor)
     */
    public Crud(Class<E> beanType, CrudEditor<E> editor) {
        this(beanType, new CrudGrid<>(beanType, true), editor);
    }

    /**
     * Instantiates a new Crud using a custom grid.
     *
     * @param beanType the class of items
     * @param grid the grid with which the items listing should be displayed
     * @param editor the editor for manipulating individual items
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, CrudEditor) Crud(Class, CrudEditor)
     */
    public Crud(Class<E> beanType, Grid<E> grid, CrudEditor<E> editor) {
        this.beanType = beanType;

        this.grid = grid;
        this.grid.getElement().setAttribute("slot", "grid");

        this.editor = editor;
        this.editor.getView().getElement().setAttribute("slot", "form");

        registerHandlers();

        getElement().appendChild(grid.getElement(), editor.getView().getElement());
    }

    private void registerHandlers() {
        ComponentUtil.addListener(this, NewEvent.class, (ComponentEventListener)
                ((ComponentEventListener<NewEvent<E>>) e -> {
                    try {
                        editor.setItem(beanType.newInstance());
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to instantiate new bean", ex);
                    }

                    newListeners.forEach(listener -> listener.onComponentEvent(e));
                }));

        ComponentUtil.addListener(this, EditEvent.class, (ComponentEventListener)
                ((ComponentEventListener<EditEvent<E>>) e -> {
                    editor.setItem(e.getItem());

                    editListeners.forEach(listener -> listener.onComponentEvent(e));
                }));

        ComponentUtil.addListener(this, CancelEvent.class, (ComponentEventListener)
                ((ComponentEventListener<CancelEvent<E>>) e -> {
                    cancelListeners.forEach(listener -> listener.onComponentEvent(e));

                    getEditor().clear();
                    setOpened(false);
                }));

        ComponentUtil.addListener(this, SaveEvent.class, (ComponentEventListener)
                ((ComponentEventListener<SaveEvent<E>>) e -> {
                    saveListeners.forEach(listener -> listener.onComponentEvent(e));

                    getEditor().clear();
                    getGrid().getDataProvider().refreshAll();
                    setOpened(false);
                }));

        ComponentUtil.addListener(this, DeleteEvent.class, (ComponentEventListener)
                ((ComponentEventListener<DeleteEvent<E>>) e -> {
                    deleteListeners.forEach(listener -> listener.onComponentEvent(e));

                    getEditor().clear();
                    getGrid().getDataProvider().refreshAll();
                    setOpened(false);
                }));
    }

    /**
     * Opens or closes the editor. In most use cases opening or closing the editor
     * is automatically done by the component and this method does not need to be called.
     *
     * @param opened true to open or false to close
     */
    public void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    public Grid<E> getGrid() {
        return grid;
    }

    /**
     * Gets the crud editor.
     *
     * @return the crud editor
     */
    public CrudEditor<E> getEditor() {
        return editor;
    }

    /**
     * Gets the crud footer.
     *
     * @return the crud footer
     * @see #setFooter(Component)
     * @see #setFooter(String)
     */
    public Component getFooter() {
        return footer;
    }

    /**
     * Sets a component to be displayed as the crud footer.
     * This could, for example, be used to display a banner or anything else.
     *
     * @param footer the footer component
     * @see #setFooter(String)
     */
    public void setFooter(Component footer) {
        footer.getElement().setAttribute("slot", "footer");
        getElement().insertChild(0, footer.getElement());

        if (this.footer != null) {
            getElement().removeChild(this.footer.getElement());
        }

        this.footer = footer;
    }

    /**
     * Sets a text to be displayed as the crud footer.
     * This is a convenience version of {@link #setFooter(String)} when displaying simple texts.
     *
     * @param footer the footer text
     * @see #setFooter(Component)
     */
    public void setFooter(String footer) {
        setFooter(new Span(footer));
    }

    /**
     * Registers a listener to be notified when the user starts to create a new item.
     *
     * @param listener a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addNewListener(ComponentEventListener<NewEvent<E>> listener) {
        newListeners.add(listener);
        return () -> newListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user starts to edit an existing item.
     *
     * @param listener a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addEditListener(ComponentEventListener<EditEvent<E>> listener) {
        editListeners.add(listener);
        return () -> editListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user tries to save a new item
     * or modifications to an existing item.
     *
     * @param listener a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addSaveListener(ComponentEventListener<SaveEvent<E>> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user cancels a new item creation or existing item
     * modification in progress.
     *
     * @param listener a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addCancelListener(ComponentEventListener<CancelEvent<E>> listener) {
        cancelListeners.add(listener);
        return () -> cancelListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the user tries to delete an existing item.
     *
     * @param listener a listener to be notified
     * @return a handle that can be used to unregister the listener
     */
    public Registration addDeleteListener(ComponentEventListener<DeleteEvent<E>> listener) {
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
     * @param provider the data provider for the grid
     */
    public void setDataProvider(DataProvider<E, ?> provider) {
        grid.setDataProvider(provider);
    }

    /**
     * A helper method to add an edit column to a grid.
     * Clicking on the edit cell for a row opens the item for editing in the editor.
     *
     * @param grid the grid in which to add the edit column
     */
    public static void addEditColumn(Grid grid) {
        grid.addColumn(
                TemplateRenderer.of("<vaadin-crud-grid-edit></vaadin-crud-grid-edit>"))
                .setWidth("40px")
                .setFlexGrow(0);
    }

    /**
     * The base class for all Crud events.
     *
     * @param <E> the bean type
     * @see com.vaadin.flow.component.crud.Crud#Crud(Class, CrudEditor) Crud(Class, CrudEditor)
     */
    static abstract class CrudEvent<E> extends ComponentEvent<Crud<E>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
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
     * Event fired when the user cancels the creation of a new item
     * or modifications to an existing item.
     *
     * @param <E> the bean type
     */
    @DomEvent("cancel")
    public static class CancelEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         * @param ignored an ignored parameter for a side effect
         */
        public CancelEvent(Crud<E> source, boolean fromClient,
                           @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when the user tries to delete an existing item.
     *
     * @param <E> the bean type
     */
    @DomEvent("delete")
    public static class DeleteEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         * @param ignored an ignored parameter for a side effect
         */
        public DeleteEvent(Crud<E> source, boolean fromClient,
                           @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when the user starts to edit an existing item.
     *
     * @param <E> the bean type
     */
    @DomEvent("edit")
    public static class EditEvent<E> extends CrudEvent<E> {

        private E item;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         * @param item the item to be edited, provided in JSON as internally represented in Grid
         * @param ignored an ignored parameter for a side effect
         */
        public EditEvent(Crud<E> source, boolean fromClient,
                         @EventData("event.detail.item") JsonObject item,
                         @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
            this.item = source.getGrid().getDataCommunicator()
                    .getKeyMapper().get(item.getString("key"));
        }

        public E getItem() {
            return item;
        }
    }

    /**
     * Event fired when the user starts to create a new item.
     *
     * @param <E> the bean type
     */
    @DomEvent("new")
    public static class NewEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         * @param ignored an ignored parameter for a side effect
         */
        public NewEvent(Crud<E> source, boolean fromClient,
                        @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    /**
     * Event fired when the user tries to save a new item or modifications to an existing item.
     *
     * @param <E> the bean type
     */
    @DomEvent("save")
    public static class SaveEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         * @param ignored an ignored parameter for a side effect
         */
        public SaveEvent(Crud<E> source, boolean fromClient,
                         @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }
}
