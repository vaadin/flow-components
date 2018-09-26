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

    public Crud(Class<E> beanType, CrudEditor<E> editor) {
        this(beanType, new CrudGrid<>(beanType, true), editor);
    }

    public Crud(Class<E> beanType, Grid<E> grid, CrudEditor<E> editor) {
        this.beanType = beanType;

        setEntityName(beanType.getSimpleName());

        this.grid = grid;
        this.grid.getElement().setAttribute("slot", "grid");

        this.editor = editor;
        this.editor.getView().setAttribute("slot", "form");

        registerHandlers();

        getElement().appendChild(grid.getElement(), editor.getView());
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

    public void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    public void setEntityName(String entityName) {
        getElement().setProperty("entityName", entityName);
    }

    public Grid<E> getGrid() {
        return grid;
    }

    public CrudEditor<E> getEditor() {
        return editor;
    }

    public Component getFooter() {
        return footer;
    }

    public void setFooter(Component footer) {
        footer.getElement().setAttribute("slot", "footer");
        getElement().insertChild(0, footer.getElement());

        if (this.footer != null) {
            getElement().removeChild(this.footer.getElement());
        }

        this.footer = footer;
    }

    public void setFooter(String footer) {
        setFooter(new Span(footer));
    }

    public Registration addNewListener(ComponentEventListener<NewEvent<E>> listener) {
        newListeners.add(listener);
        return () -> newListeners.remove(listener);
    }

    public Registration addEditListener(ComponentEventListener<EditEvent<E>> listener) {
        editListeners.add(listener);
        return () -> editListeners.remove(listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent<E>> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }

    public Registration addCancelListener(ComponentEventListener<CancelEvent<E>> listener) {
        cancelListeners.add(listener);
        return () -> cancelListeners.remove(listener);
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent<E>> listener) {
        deleteListeners.add(listener);
        return () -> deleteListeners.remove(listener);
    }

    public DataProvider<E, ?> getDataProvider() {
        return grid.getDataProvider();
    }

    public void setDataProvider(DataProvider<E, ?> provider) {
        grid.setDataProvider(provider);
    }

    public static void addEditColumn(Grid grid) {
        grid.addColumn(
                TemplateRenderer.of("<vaadin-crud-grid-edit></vaadin-crud-grid-edit>"))
                .setWidth("40px")
                .setFlexGrow(0);
    }

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

        public E getItem() {
            return getSource().getEditor().getItem();
        }
    }

    @DomEvent("cancel")
    public static class CancelEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public CancelEvent(Crud<E> source, boolean fromClient,
                           @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    @DomEvent("delete")
    public static class DeleteEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public DeleteEvent(Crud<E> source, boolean fromClient,
                           @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    @DomEvent("edit")
    public static class EditEvent<E> extends CrudEvent<E> {

        private E item;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
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

    @DomEvent("new")
    public static class NewEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public NewEvent(Crud<E> source, boolean fromClient,
                        @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }

    @DomEvent("save")
    public static class SaveEvent<E> extends CrudEvent<E> {

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public SaveEvent(Crud<E> source, boolean fromClient,
                         @EventData("event.stopPropagation()") Object ignored) {
            super(source, fromClient);
        }
    }
}
