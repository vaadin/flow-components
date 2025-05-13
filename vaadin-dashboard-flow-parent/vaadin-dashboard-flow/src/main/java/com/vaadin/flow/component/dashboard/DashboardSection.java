/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * DashboardSection is a container for organizing multiple
 * {@link DashboardWidget} instances within a {@link Dashboard}.
 *
 * @see Dashboard
 * @see DashboardWidget
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard-section")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard-section.js")
@NpmPackage(value = "@vaadin/dashboard", version = "24.8.0-alpha18")
public class DashboardSection extends Component implements HasWidgets {

    private final List<DashboardWidget> widgets = new ArrayList<>();

    private final DashboardChildDetachHandler childDetachHandler;

    /**
     * Creates an empty section.
     */
    public DashboardSection() {
        this(null);
    }

    /**
     * Creates an empty section with title.
     *
     * @param title
     *            the title to set
     */
    public DashboardSection(String title) {
        super();
        childDetachHandler = getChildDetachHandler();
        setTitle(title);
    }

    /**
     * Returns the title of the section.
     *
     * @return the {@code sectionTitle} property from the web component
     */
    public String getTitle() {
        return getElement().getProperty("sectionTitle");
    }

    /**
     * Sets the title of the section.
     *
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        getElement().setProperty("sectionTitle", title);
    }

    @Override
    public List<DashboardWidget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    @Override
    public Stream<Component> getChildren() {
        return widgets.stream().map(Component.class::cast);
    }

    @Override
    public void add(Collection<DashboardWidget> widgets) {
        Objects.requireNonNull(widgets, "Widgets to add cannot be null.");
        widgets.forEach(widget -> Objects.requireNonNull(widget,
                "Widget to add cannot be null."));
        widgets.forEach(this::doAddWidget);
        updateClient();
    }

    @Override
    public void addWidgetAtIndex(int index, DashboardWidget widget) {
        Objects.requireNonNull(widget, "Widget to add cannot be null.");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a widget with a negative index.");
        }
        if (index > getWidgets().size()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot add a widget with index %d when there are %d widgets",
                    index, getWidgets().size()));
        }
        doAddWidgetAtIndex(index, widget);
        updateClient();
    }

    @Override
    public void remove(Collection<DashboardWidget> widgets) {
        Objects.requireNonNull(widgets, "Widgets to remove cannot be null.");
        var toRemove = new ArrayList<DashboardWidget>(widgets.size());
        for (DashboardWidget widget : widgets) {
            Objects.requireNonNull(widget, "Widget to remove cannot be null.");
            var parent = widget.getElement().getParent();
            if (parent == null) {
                LoggerFactory.getLogger(getClass()).debug(
                        "Removal of a widget with no parent does nothing.");
                continue;
            }
            if (getElement().equals(parent)) {
                toRemove.add(widget);
            } else {
                throw new IllegalArgumentException("The given widget (" + widget
                        + ") is not a child of this section");
            }
        }
        if (!toRemove.isEmpty()) {
            toRemove.forEach(this::doRemoveWidget);
            updateClient();
        }
    }

    @Override
    public void removeAll() {
        if (getWidgets().isEmpty()) {
            return;
        }
        doRemoveAll();
        updateClient();
    }

    @Override
    public void removeFromParent() {
        getParent().ifPresent(parent -> ((Dashboard) parent).remove(this));
    }

    /**
     * @throws UnsupportedOperationException
     *             Dashboard section does not support setting visibility
     */
    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException(
                "Dashboard section does not support setting visibility");
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    private void doRemoveAll() {
        new ArrayList<>(widgets).forEach(this::doRemoveWidget);
    }

    private void doRemoveWidget(DashboardWidget widget) {
        getElement().removeChild(widget.getElement());
        widgets.remove(widget);
    }

    private void doAddWidgetAtIndex(int index, DashboardWidget widget) {
        getElement().appendChild(widget.getElement());
        widgets.add(index, widget);
    }

    private void doAddWidget(DashboardWidget widget) {
        getElement().appendChild(widget.getElement());
        widgets.add(widget);
    }

    void reorderWidgets(List<DashboardWidget> orderedWidgets) {
        widgets.clear();
        widgets.addAll(orderedWidgets);
    }

    void updateClient() {
        childDetachHandler.refreshListeners();
        getParent().ifPresent(parent -> ((Dashboard) parent).updateClient());
    }

    private DashboardChildDetachHandler getChildDetachHandler() {
        return new DashboardChildDetachHandler(this) {
            @Override
            void removeChild(Component child) {
                widgets.remove(child);
                updateClient();
            }
        };
    }
}
