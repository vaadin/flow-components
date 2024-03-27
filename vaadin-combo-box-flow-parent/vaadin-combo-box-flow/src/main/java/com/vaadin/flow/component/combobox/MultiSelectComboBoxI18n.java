/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import java.io.Serializable;

/**
 * Class for localization of the {@link MultiSelectComboBox}
 */
public class MultiSelectComboBoxI18n implements Serializable {
    private String cleared;
    private String focused;
    private String selected;
    private String deselected;
    private String total;

    /**
     * The text that is announced by screen readers when the clear button is
     * clicked.
     * <p>
     * The value is {@code null} by default, which means the default value of
     * the web component will be used.
     *
     * @return the text that is announced by screen readers when the clear
     *         button is clicked or {@code null} if the default value of the web
     *         component is used.
     */
    public String getCleared() {
        return cleared;
    }

    /**
     * Sets the text that is announced by screen readers when the clear button
     * is clicked.
     *
     * @param cleared
     *            the text that is announced by screen readers when the clear
     *            button is clicked or {@code null} if the default value of the
     *            web component should be used.
     * @return this instance for method chaining
     */
    public MultiSelectComboBoxI18n setCleared(String cleared) {
        this.cleared = cleared;
        return this;
    }

    /**
     * The text that is announced by screen readers when a chip is focused.
     * <p>
     * The value is {@code null} by default, which means the default value of
     * the web component will be used.
     *
     * @return the text that is announced by screen readers when a chip is
     *         focused or {@code null} if the default value of the web component
     *         is used.
     */
    public String getFocused() {
        return focused;
    }

    /**
     * Sets the text that is announced by screen readers when a chip is focused.
     * The label of the chip will be prepended to this text.
     *
     * @param focused
     *            the text that is announced by screen readers when a chip is
     *            focused or {@code null} if the default value of the web
     *            component should be used.
     * @return this instance for method chaining
     */
    public MultiSelectComboBoxI18n setFocused(String focused) {
        this.focused = focused;
        return this;
    }

    /**
     * The text that is announced by screen readers when an item is added to the
     * selection.
     * <p>
     * The value is {@code null} by default, which means the default value of
     * the web component will be used.
     *
     * @return the text that is announced by screen readers when an item is
     *         added to the selection or {@code null} if the default value of
     *         the web component is used.
     */
    public String getSelected() {
        return selected;
    }

    /**
     * Sets the text that is announced by screen readers when an item is added
     * to the selection. The label of the item will be prepended to this text.
     *
     * @param selected
     *            the text that is announced by screen readers when an item is
     *            added to the selection or {@code null} if the default value of
     *            the web component should be used.
     * @return this instance for method chaining
     */
    public MultiSelectComboBoxI18n setSelected(String selected) {
        this.selected = selected;
        return this;
    }

    /**
     * The text that is announced by screen readers when an item is removed from
     * the selection.
     * <p>
     * The value is {@code null} by default, which means the default value of
     * the web component will be used.
     *
     * @return the text that is announced by screen readers when an item is
     *         removed from the selection or {@code null} if the default value
     *         of the web component is used.
     */
    public String getDeselected() {
        return deselected;
    }

    /**
     * Sets the text that is announced by screen readers when an item is removed
     * from the selection. The label of the item will be prepended to this text.
     *
     * @param deselected
     *            the text that is announced by screen readers when an item is
     *            removed from the selection or {@code null} if the default
     *            value of the web component should be used.
     * @return this instance for method chaining
     */
    public MultiSelectComboBoxI18n setDeselected(String deselected) {
        this.deselected = deselected;
        return this;
    }

    /**
     * The text that is announced by screen readers to inform about the total
     * number of selected items.
     * <p>
     * The value is {@code null} by default, which means the default value of
     * the web component will be used.
     *
     * @return the text that is announced by screen readers to inform about the
     *         total number of selected items or {@code null} if the default
     *         value of the web component is used.
     */
    public String getTotal() {
        return total;
    }

    /**
     * Sets the text that is announced by screen readers to inform about the
     * total number of selected items. The string must contain a `{count}`
     * placeholder that will be replaced with the actual count of selected items
     * by the component.
     *
     * @param total
     *            the text that is announced by screen readers to inform about
     *            the total number of selected items or {@code null} if the
     *            default value of the web component should be used.
     * @return this instance for method chaining
     */
    public MultiSelectComboBoxI18n setTotal(String total) {
        if (total != null && !total.contains("{count}")) {
            throw new IllegalArgumentException(
                    "Text must contain a {count} placeholder");
        }
        this.total = total;
        return this;
    }
}
