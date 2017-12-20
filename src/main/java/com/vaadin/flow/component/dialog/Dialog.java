/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.dom.Element;

/**
 * Server-side component for the {@code <vaadin-dialog>} element.
 * 
 * @author Vaadin Ltd
 */
public class Dialog extends GeneratedVaadinDialog<Dialog> {

    /**
     * Creates an empty dialog.
     */
    public Dialog() {
        this("");
    }

    /**
     * Creates a dialog with the given String rendered as it's HTML content.
     * 
     * @param content
     *            the content of the Dialog as HTML markup
     */
    public Dialog(String content) {
        Element templateElement = new Element("template");
        getElement().appendChild(templateElement);

        templateElement.setProperty("innerHTML", content);
    }

    /**
     * Opens the dialog.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Gets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     * 
     * @return {@code true} if this dialog can be closed with the esc-key,
     *         {@code false} otherwise
     */
    public boolean isCloseOnEsc() {
        return !getElement().getProperty("noCloseOnEsc", false);
    }

    /**
     * Sets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     * 
     * @param closeOnEsc
     *            {@code true} to enable closing this dialog with the esc-key,
     *            {@code false} to disable it
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        getElement().setProperty("noCloseOnEsc", !closeOnEsc);
    }

    /**
     * Gets whether this dialog can be closed by clicking outside of it or not.
     * <p>
     * By default, the dialog is closable with an outside click.
     * 
     * @return {@code true} if this dialog can be closed by an outside click,
     *         {@code false} otherwise
     */
    public boolean isCloseOnOutsideClick() {
        return !getElement().getProperty("noCloseOnOutsideClick", false);
    }

    /**
     * Sets whether this dialog can be closed by clicking outside of it or not.
     * <p>
     * By default, the dialog is closable with an outside click.
     * 
     * @param closeOnOutsideClick
     *            {@code true} to enable closing this dialog with an outside
     *            click, {@code false} to disable it
     */
    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        getElement().setProperty("noCloseOnOutsideClick", !closeOnOutsideClick);
    }

}
