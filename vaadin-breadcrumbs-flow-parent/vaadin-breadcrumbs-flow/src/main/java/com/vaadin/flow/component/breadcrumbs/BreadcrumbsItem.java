/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.breadcrumbs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.SignalPropertySupport;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.internal.UrlUtil;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.signals.Signal;

/**
 * An item of the {@link Breadcrumbs} component, representing a single entry in
 * the navigation trail.
 * <p>
 * An item carries the text shown in the trail and, optionally, a {@code path}
 * it links to. An item without a path is the current page and is rendered as a
 * non-link. The link target can be set as a plain URL string via
 * {@link #setPath(String)} or resolved from a Flow route class via
 * {@link #setPath(Class)} / {@link #setPath(Class, RouteParameters)}. An
 * optional prefix component (typically an icon) can be shown before the text.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-breadcrumbs-item")
@NpmPackage(value = "@vaadin/breadcrumbs", version = "25.2.0")
@JsModule("@vaadin/breadcrumbs/src/vaadin-breadcrumbs-item.js")
public class BreadcrumbsItem extends Component
        implements HasText, HasEnabled, HasPrefix {

    private final Text textNode = new Text("");

    private final SignalPropertySupport<String> textSupport = SignalPropertySupport
            .create(this, this::updateText);

    /**
     * Creates a breadcrumbs item with the given text and no path. An item
     * without a path represents the current page and is rendered as a non-link.
     *
     * @param text
     *            the text of the item
     */
    public BreadcrumbsItem(String text) {
        setText(text);
    }

    /**
     * Creates a breadcrumbs item with the given text that links to the given
     * path.
     *
     * @param text
     *            the text of the item
     * @param path
     *            the path to link to
     * @throws IllegalArgumentException
     *             if {@code path} uses a scheme that is not considered safe;
     *             see {@link #setUnsafePath(String)} and the
     *             {@value InitParameters#URL_SAFE_SCHEMES} configuration
     *             property
     */
    public BreadcrumbsItem(String text, String path) {
        setPath(path);
        setText(text);
    }

    /**
     * Creates a breadcrumbs item with the given text that links to the given
     * view.
     *
     * @param text
     *            the text of the item
     * @param view
     *            the view to link to
     */
    public BreadcrumbsItem(String text, Class<? extends Component> view) {
        setPath(view);
        setText(text);
    }

    /**
     * Creates a breadcrumbs item with the given text that links to the given
     * view using the given route parameters.
     *
     * @param text
     *            the text of the item
     * @param view
     *            the view to link to
     * @param params
     *            the route parameters
     */
    public BreadcrumbsItem(String text, Class<? extends Component> view,
            RouteParameters params) {
        setPath(view, params);
        setText(text);
    }

    /**
     * Creates a breadcrumbs item with the given text and prefix component (like
     * an icon) that links to the given path.
     *
     * @param text
     *            the text of the item
     * @param path
     *            the path to link to
     * @param prefixComponent
     *            the prefix component for the item (usually an icon)
     * @throws IllegalArgumentException
     *             if {@code path} uses a scheme that is not considered safe;
     *             see {@link #setUnsafePath(String)} and the
     *             {@value InitParameters#URL_SAFE_SCHEMES} configuration
     *             property
     */
    public BreadcrumbsItem(String text, String path,
            Component prefixComponent) {
        this(text, path);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Creates a breadcrumbs item with the given text and prefix component (like
     * an icon) that links to the given view.
     *
     * @param text
     *            the text of the item
     * @param view
     *            the view to link to
     * @param prefixComponent
     *            the prefix component for the item (usually an icon)
     */
    public BreadcrumbsItem(String text, Class<? extends Component> view,
            Component prefixComponent) {
        this(text, view);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Creates a breadcrumbs item with the given text and prefix component (like
     * an icon) that links to the given view using the given route parameters.
     *
     * @param text
     *            the text of the item
     * @param view
     *            the view to link to
     * @param params
     *            the route parameters
     * @param prefixComponent
     *            the prefix component for the item (usually an icon)
     */
    public BreadcrumbsItem(String text, Class<? extends Component> view,
            RouteParameters params, Component prefixComponent) {
        this(text, view, params);
        setPrefixComponent(prefixComponent);
    }

    /**
     * Sets the given string as the text content of this item.
     * <p>
     * This method removes any existing content in the default slot and replaces
     * it with the given text. Other slotted children (such as the prefix) are
     * preserved.
     *
     * @param text
     *            the text content to set, or {@code null} to remove existing
     *            text
     */
    @Override
    public void setText(String text) {
        textSupport.set(text);
    }

    @Override
    public String getText() {
        return textSupport.get();
    }

    @Override
    public SignalBinding<String> bindText(Signal<String> textSignal) {
        return textSupport.bind(textSignal);
    }

    private void updateText(String text) {
        textNode.setText(text);

        if (text == null || text.isEmpty()) {
            textNode.removeFromParent();
            return;
        }

        if (textNode.getParent().isEmpty()) {
            getElement().appendChild(textNode.getElement());
        }
    }

    /**
     * Gets the path this item links to.
     *
     * @return the path this item links to, or {@code null} if the item has no
     *         path
     */
    public String getPath() {
        return getElement().getAttribute("path");
    }

    /**
     * Sets the path, as a URL string, this item links to.
     * <p>
     * Note that there is also an alternative way of setting the link path via
     * {@link #setPath(Class)}.
     *
     * @param path
     *            the path to link to, or {@code null} to remove the path and
     *            render the item as the current page (a non-link)
     *
     * @see #setPath(Class)
     */
    public void setPath(String path) {
        if (path != null && !UrlUtil.isSafeUrl(path)) {
            throw new IllegalArgumentException(UrlUtil.getUnsafeUrlMessage(
                    "path", path, "setUnsafePath(String)"));
        }
        doSetPath(path);
    }

    /**
     * Sets the path this item links to without validating its scheme.
     * <p>
     * Unlike {@link #setPath(String)}, this method does not reject paths based
     * on the {@value InitParameters#URL_SAFE_SCHEMES} configuration. Use it
     * only for paths that are fully under your control and known to be safe,
     * such as a hard-coded {@code javascript:} URL. Passing untrusted input
     * here can expose the application to cross-site scripting (XSS) attacks.
     *
     * @see #setPath(String)
     *
     * @param path
     *            the path to link to, or {@code null} to remove the path and
     *            render the item as the current page (a non-link)
     */
    public void setUnsafePath(String path) {
        doSetPath(path);
    }

    private void doSetPath(String path) {
        if (path == null) {
            getElement().removeAttribute("path");
        } else {
            getElement().setAttribute("path", path);
        }
    }

    /**
     * Resolves the URL of the given view via the Vaadin router and sets it as
     * the path this item links to.
     *
     * @param view
     *            the view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation. Set to
     *            {@code null} to remove the path.
     *
     * @see #setPath(String)
     */
    public void setPath(Class<? extends Component> view) {
        setPath(view, RouteParameters.empty());
    }

    /**
     * Resolves the URL of the given view via the Vaadin router using the given
     * route parameters and sets it as the path this item links to.
     *
     * @param view
     *            the view to link to. The view should be annotated with the
     *            {@link com.vaadin.flow.router.Route} annotation. Set to
     *            {@code null} to remove the path.
     * @param parameters
     *            the route parameters
     *
     * @see #setPath(String)
     */
    public void setPath(Class<? extends Component> view,
            RouteParameters parameters) {
        if (view == null) {
            setPath((String) null);
        } else {
            RouteConfiguration routeConfiguration = RouteConfiguration
                    .forRegistry(ComponentUtil.getRouter(this).getRegistry());
            setPath(routeConfiguration.getUrl(view, parameters));
        }
    }
}
