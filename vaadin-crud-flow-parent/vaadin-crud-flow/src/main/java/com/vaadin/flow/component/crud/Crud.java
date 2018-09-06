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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import java.util.LinkedHashSet;
import java.util.Set;

@Tag("vaadin-crud")
@HtmlImport("frontend://bower_components/vaadin-crud/src/vaadin-crud.html")
@JavaScript("frontend://crudConnector.js")
public class Crud<E> extends Component {

    private final Class<E> beanType;
    private final Grid<E> grid;
    private final CrudEditor<E> editor;

    private final Set<ComponentEventListener<NewEvent>> newListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<EditEvent<E>>> editListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<SaveEvent>> saveListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<CancelEvent>> cancelListeners = new LinkedHashSet<>();
    private final Set<ComponentEventListener<DeleteEvent>> deleteListeners = new LinkedHashSet<>();

    public Crud(Class<E> beanType, CrudEditor<E> editor) {
        this(beanType, new CrudGrid<>(beanType, true), editor);
    }

    public Crud(Class<E> beanType, Grid<E> grid, CrudEditor<E> editor) {
        this.beanType = beanType;

        this.grid = grid;
        this.grid.getElement().setAttribute("slot", "grid");

        this.editor = editor;
        this.editor.getView().setAttribute("slot", "form");

        registerHandlers();

        getElement().appendChild(grid.getElement(), editor.getView());
    }

    private void registerHandlers() {
        ComponentUtil.addListener(this, NewEvent.class, (ComponentEventListener<NewEvent>) e -> {
            try {
                editor.setItem(beanType.newInstance());
            } catch (Exception ex) {
                throw new RuntimeException("Unable to instantiate new bean", ex);
            }

            newListeners.forEach(listener -> listener.onComponentEvent(e));
        });

        ComponentUtil.addListener(this, EditEvent.class, (ComponentEventListener)
                ((ComponentEventListener<EditEvent<E>>) e -> {
                    editor.setItem(e.getItem());

                    editListeners.forEach(listener -> listener.onComponentEvent(e));
                }));

        ComponentUtil.addListener(this, CancelEvent.class, (ComponentEventListener<CancelEvent>) e -> {
            cancelListeners.forEach(listener -> listener.onComponentEvent(e));

            getEditor().setItem(null);
            closeEditor();
        });

        ComponentUtil.addListener(this, SaveEvent.class, (ComponentEventListener<SaveEvent>) e -> {
            saveListeners.forEach(listener -> listener.onComponentEvent(e));

            getEditor().setItem(null);
            getGrid().getDataProvider().refreshAll();
            closeEditor();
        });

        ComponentUtil.addListener(this, DeleteEvent.class, (ComponentEventListener<DeleteEvent>) e -> {
            deleteListeners.forEach(listener -> listener.onComponentEvent(e));

            getEditor().setItem(null);
            getGrid().getDataProvider().refreshAll();
            closeEditor();
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getUI().orElseThrow(() -> new IllegalStateException(
                "Connector can only be initialized for an attached Crud"))
                .getPage().executeJavaScript(
                "window.Vaadin.Flow.crudConnector.initLazy($0)",
                getElement());
    }

    private void closeEditor() {
        getElement().callFunction("closeDialog");
    }

    public Grid<E> getGrid() {
        return grid;
    }

    public CrudEditor<E> getEditor() {
        return editor;
    }

    public Registration addNewListener(ComponentEventListener<NewEvent> listener) {
        newListeners.add(listener);
        return () -> newListeners.remove(listener);
    }

    public Registration addEditListener(ComponentEventListener<EditEvent<E>> listener) {
        editListeners.add(listener);
        return () -> editListeners.remove(listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        cancelListeners.add(listener);
        return () -> cancelListeners.remove(listener);
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        deleteListeners.add(listener);
        return () -> deleteListeners.remove(listener);
    }

    public DataProvider<E, ?> getDataProvider() {
        return grid.getDataProvider();
    }

    public void setDataProvider(DataProvider<E, ?> provider) {
        grid.setDataProvider(provider);
    }

    @DomEvent("crud-cancel")
    public static class CancelEvent extends ComponentEvent<Crud<?>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public CancelEvent(Crud<?> source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("crud-delete")
    public static class DeleteEvent extends ComponentEvent<Crud<?>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public DeleteEvent(Crud<?> source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("crud-edit")
    public static class EditEvent<E> extends ComponentEvent<Crud<E>> {

        private E item;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public EditEvent(Crud<E> source, boolean fromClient,
                         @EventData("event.detail.item") JsonObject item) {
            super(source, fromClient);
            try {
                this.item = source.getGrid().getDataCommunicator()
                        .getKeyMapper().get(item.getString("key"));
            } catch (NullPointerException ex) {
                // TODO(oluwasayo): Remove when WC no longer fires edit event on grid active item change
            }
        }

        public E getItem() {
            return item;
        }
    }

    @DomEvent("crud-new")
    public static class NewEvent extends ComponentEvent<Crud<?>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public NewEvent(Crud<?> source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    @DomEvent("crud-save")
    public static class SaveEvent extends ComponentEvent<Crud<?>> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public SaveEvent(Crud<?> source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
