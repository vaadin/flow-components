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
package com.vaadin.flow.component.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

/**
 * <p>
 * Description copied from corresponding location in WebComponent:
 * </p>
 * <p>
 * {@code <vaadin-dialog>} is a Web Component for customized modal dialogs.
 * </p>
 * <p>
 * &lt;vaadin-dialog opened&gt; &lt;template&gt; Sample dialog &lt;/template&gt;
 * &lt;/vaadin-dialog&gt;
 * </p>
 * <h3>Styling</h3>
 * <p>
 * See <a href=
 * "https://github.com/vaadin/vaadin-overlay/blob/master/src/vaadin-overlay.html"
 * >{@code <vaadin-overlay>} documentation</a> for
 * {@code <vaadin-dialog-overlay>} parts.
 * </p>
 * <p>
 * See
 * <a href="https://github.com/vaadin/vaadin-themable-mixin/wiki">ThemableMixin
 * – how to apply styles for shadow parts</a>
 * </p>
 *
 * @deprecated since v23.3, generated classes will be removed in v24.
 */
@Deprecated
@Tag("vaadin-dialog")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.3.25")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/dialog", version = "23.3.25")
@NpmPackage(value = "@vaadin/vaadin-dialog", version = "23.3.25")
@JsModule("@vaadin/dialog/src/vaadin-dialog.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
public abstract class GeneratedVaadinDialog<R extends GeneratedVaadinDialog<R>>
        extends Component {

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * True if the overlay is currently displayed.
     * <p>
     * This property is synchronized automatically from client side when a
     * 'opened-changed' event happens.
     * </p>
     *
     * @return the {@code opened} property from the webcomponent
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    @Synchronize(property = "opened", value = "opened-changed")
    protected boolean isOpenedBoolean() {
        return getElement().getProperty("opened", false);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * True if the overlay is currently displayed.
     * </p>
     *
     * @param opened
     *            the boolean value to set
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set the {@code aria-label} attribute for assistive technologies like
     * screen readers. An {@code undefined} value for this property (the
     * default) means that the {@code aria-label} attribute is not present at
     * all.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code ariaLabel} property from the webcomponent
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected String getAriaLabelString() {
        return getElement().getProperty("ariaLabel");
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set the {@code aria-label} attribute for assistive technologies like
     * screen readers. An {@code undefined} value for this property (the
     * default) means that the {@code aria-label} attribute is not present at
     * all.
     * </p>
     *
     * @param ariaLabel
     *            the String value to set
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected void setAriaLabel(String ariaLabel) {
        getElement().setProperty("ariaLabel",
                ariaLabel == null ? "" : ariaLabel);
    }

    /**
     * @deprecated since v23.3, generated classes will be removed in v24. Use
     *             {@link Dialog.OpenedChangeEvent} instead.
     */
    @Deprecated
    public static class OpenedChangeEvent<R extends GeneratedVaadinDialog<R>>
            extends ComponentEvent<R> {
        private final boolean opened;

        public OpenedChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpenedBoolean();
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
     * @deprecated since v23.3, generated classes will be removed in v24.
     */
    @Deprecated
    protected Registration addOpenedChangeListener(
            ComponentEventListener<Dialog.OpenedChangeEvent<Dialog>> listener) {
        return getElement().addPropertyChangeListener("opened",
                event -> listener.onComponentEvent(
                        new Dialog.OpenedChangeEvent<>((Dialog) this,
                                event.isUserOriginated())));
    }
}
