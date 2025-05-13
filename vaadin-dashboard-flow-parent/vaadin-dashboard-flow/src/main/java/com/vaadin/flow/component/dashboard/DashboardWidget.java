/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.SlotUtils;

/**
 * DashboardWidget represents a customizable widget that can be placed within a
 * {@link Dashboard}. It supports layout options such as colspan and rowspan,
 * and allows setting content and header content.
 *
 * @see Dashboard
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard-widget")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard-widget.js")
@NpmPackage(value = "@vaadin/dashboard", version = "24.8.0-alpha18")
public class DashboardWidget extends Component {

    private int colspan = 1;

    private int rowspan = 1;

    private boolean featureFlagEnabled;

    /**
     * Creates an empty widget.
     */
    public DashboardWidget() {
        this(null, null);
    }

    /**
     * Creates a widget with the specified title.
     *
     * @param title
     *            the title to set
     */
    public DashboardWidget(String title) {
        this(title, null);
    }

    /**
     * Creates a widget with the specified content.
     *
     * @param content
     *            the content to set
     */
    public DashboardWidget(Component content) {
        this(null, content);
    }

    /**
     * Creates a widget with the specified title and content.
     *
     * @param title
     *            the title to set
     * @param content
     *            the content to set
     */
    public DashboardWidget(String title, Component content) {
        super();
        if (title != null) {
            setTitle(title);
        }
        if (content != null) {
            setContent(content);
        }
    }

    /**
     * Returns the title of the widget.
     *
     * @return the {@code widgetTitle} property from the web component
     */
    public String getTitle() {
        return getElement().getProperty("widgetTitle");
    }

    /**
     * Sets the title of the widget.
     *
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        getElement().setProperty("widgetTitle", title == null ? "" : title);
    }

    /**
     * Returns the colspan of the widget. The default is 1.
     *
     * @return the colspan of the widget
     */
    public int getColspan() {
        return colspan;
    }

    /**
     * Sets the colspan of the widget. Cannot be lower than 1.
     *
     * @param colspan
     *            the colspan to set
     */
    public void setColspan(int colspan) {
        if (colspan < 1) {
            throw new IllegalArgumentException(
                    "Cannot set a colspan lower than 1.");
        }
        if (this.colspan == colspan) {
            return;
        }
        this.colspan = colspan;
        notifyParentDashboardOrSection();
    }

    /**
     * Returns the rowspan of the widget. The default is 1.
     *
     * @return the rowspan of the widget
     */
    public int getRowspan() {
        return rowspan;
    }

    /**
     * Sets the rowspan of the widget. Cannot be lower than 1.
     *
     * @param rowspan
     *            the rowspan to set
     */
    public void setRowspan(int rowspan) {
        if (rowspan < 1) {
            throw new IllegalArgumentException(
                    "Cannot set a rowspan lower than 1.");
        }
        if (this.rowspan == rowspan) {
            return;
        }
        this.rowspan = rowspan;
        notifyParentDashboardOrSection();
    }

    /**
     * Returns the content of the widget. Returns {@code null} if the widget has
     * no content.
     *
     * @return the content of the widget
     */
    public Component getContent() {
        return getChildren().filter(
                component -> !component.getElement().hasAttribute("slot"))
                .findAny().orElse(null);
    }

    /**
     * Sets the content to the widget. Set {@code null} to remove the current
     * content.
     *
     * @param content
     *            the content to set
     */
    public void setContent(Component content) {
        Component initialContent = getContent();
        if (initialContent == content) {
            return;
        }
        if (initialContent != null) {
            getElement().removeChild(initialContent.getElement());
        }
        if (content != null) {
            getElement().appendChild(content.getElement());
        }
    }

    /**
     * Gets the content in the header content slot of this widget.
     *
     * @return the header content of this widget, or {@code null} if no header
     *         content has been set
     */
    public Component getHeaderContent() {
        return SlotUtils.getChildInSlot(this, "header-content");
    }

    /**
     * Sets the content in the header content slot of this widget, replacing any
     * existing header content.
     *
     * @param header
     *            the content to set, can be {@code null} to remove existing
     *            header content
     */
    public void setHeaderContent(Component header) {
        SlotUtils.setSlot(this, "header-content", header);
    }

    /**
     * @throws UnsupportedOperationException
     *             Dashboard widget does not support setting visibility
     */
    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException(
                "Dashboard widget does not support setting visibility");
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag();
    }

    private void notifyParentDashboardOrSection() {
        getParent().ifPresent(parent -> {
            if (parent instanceof Dashboard dashboard) {
                dashboard.updateClient();
            } else if (parent instanceof DashboardSection section) {
                section.updateClient();
            }
        });
    }

    /**
     * Checks whether the Dashboard component feature flag is active. Succeeds
     * if the flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#DASHBOARD_COMPONENT} feature is
     *             not enabled
     */
    private void checkFeatureFlag() {
        boolean enabled = featureFlagEnabled || getFeatureFlags()
                .isEnabled(FeatureFlags.DASHBOARD_COMPONENT);
        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the feature flags for the current UI.
     * <p>
     * Not private in order to support mocking
     *
     * @return the current set of feature flags
     */
    FeatureFlags getFeatureFlags() {
        return FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext());
    }

    /**
     * Only for test use.
     */
    void setFeatureFlagEnabled(boolean featureFlagEnabled) {
        this.featureFlagEnabled = featureFlagEnabled;
    }
}
