package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud
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

import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Internationalization object for customizing the component UI texts. An
 * instance with the default messages can be obtained using
 * {@link CrudI18n#createDefault()}
 *
 * @see Crud#setI18n(CrudI18n)
 */
public class CrudI18n implements Serializable {

    private static final JsonValue DEFAULT_I18N;

    private String newItem;
    private String editItem;
    private String saveItem;
    private String deleteItem;
    private String cancel;
    private String editLabel;
    private Confirmations confirm;

    static {
        try {
            final JsonFactory JSON_FACTORY = new JreJsonFactory();
            DEFAULT_I18N = JSON_FACTORY.parse(
                    IOUtils.toString(CrudI18n.class.getResource("i18n.json"),
                            StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot find the default i18n configuration");
        }
    }

    /**
     * Creates a new instance with the default messages
     *
     * @return a new instance with the default messages
     */
    public static CrudI18n createDefault() {
        return JsonSerializer.toObject(CrudI18n.class, DEFAULT_I18N);
    }

    /**
     * Gets the new button and editor title text
     *
     * @return the new button and editor title text
     */
    public String getNewItem() {
        return newItem;
    }

    /**
     * Sets the new button and editor title text
     *
     * @param newItem
     *            the new button and editor title text
     */
    public void setNewItem(String newItem) {
        this.newItem = newItem;
    }

    /**
     * Gets the save button text
     *
     * @return the save button text
     */
    public String getSaveItem() {
        return saveItem;
    }

    /**
     * Sets the save button text
     *
     * @param saveItem
     *            the save button text
     */
    public void setSaveItem(String saveItem) {
        this.saveItem = saveItem;
    }

    /**
     * Gets the delete button text
     *
     * @return the delete button text
     */
    public String getDeleteItem() {
        return deleteItem;
    }

    /**
     * Sets the delete button text
     *
     * @param deleteItem
     *            the delete button text
     */
    public void setDeleteItem(String deleteItem) {
        this.deleteItem = deleteItem;
    }

    /**
     * Gets the edit editor title text
     *
     * @return the edit editor title text
     */
    public String getEditItem() {
        return editItem;
    }

    /**
     * Sets the edit editor title text
     *
     * @param editItem
     *            the edit editor title text
     */
    public void setEditItem(String editItem) {
        this.editItem = editItem;
    }

    /**
     * Gets the cancel button text
     *
     * @return the cancel button text
     */
    public String getCancel() {
        return cancel;
    }

    /**
     * Sets the cancel button text
     *
     * @param cancel
     *            the cancel button text
     */
    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    /**
     * Gets the edit button aria label
     *
     * @return the edit button aria label
     */
    public String getEditLabel() {
        return editLabel;
    }

    /**
     * Sets the edit button aria label
     *
     * @param editLabel
     *            the edit button aria label
     */
    public void setEditLabel(String editLabel) {
        this.editLabel = editLabel;
    }

    /**
     * Gets the confirmation dialogs
     *
     * @return the confirmation dialogs
     */
    public Confirmations getConfirm() {
        return confirm;
    }

    /**
     * Sets the confirmation dialogs
     *
     * @param confirm
     *            the confirmation dialogs
     */
    public void setConfirm(Confirmations confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return "CrudI18n{" + "newItem='" + newItem + '\'' + ", editItem='"
                + editItem + '\'' + ", saveItem='" + saveItem + '\''
                + ", deleteItem='" + deleteItem + '\'' + ", cancel='" + cancel
                + '\'' + ", editLabel='" + editLabel + '\'' + ", confirm="
                + confirm + '}';
    }

    /**
     * The confirmation dialogs used in the component
     */
    public static class Confirmations implements Serializable {

        private Confirmation delete;
        private Confirmation cancel;

        /**
         * Gets the delete confirmation dialog
         *
         * @return the delete confirmation dialog
         */
        public Confirmation getDelete() {
            return delete;
        }

        /**
         * Sets the delete confirmation dialog
         *
         * @param delete
         *            the delete confirmation dialog
         */
        public void setDelete(Confirmation delete) {
            this.delete = delete;
        }

        /**
         * Gets the cancel confirmation dialog
         *
         * @return the cancel confirmation dialog
         */
        public Confirmation getCancel() {
            return cancel;
        }

        /**
         * Sets the cancel confirmation dialog
         *
         * @param cancel
         *            the cancel confirmation dialog
         */
        public void setCancel(Confirmation cancel) {
            this.cancel = cancel;
        }

        @Override
        public String toString() {
            return "Confirmations{" + "delete=" + delete + ", cancel=" + cancel
                    + '}';
        }

        /**
         * Represents texts in the confirmation dialogs
         */
        public static class Confirmation implements Serializable {

            private String content;
            private Button button;
            private String title;

            /**
             * Gets the main content in a dialog
             *
             * @return the main content
             */
            public String getContent() {
                return content;
            }

            /**
             * Sets the main content in a dialog
             *
             * @param content
             *            the main content
             */
            public void setContent(String content) {
                this.content = content;
            }

            /**
             * Gets the confirmation options in a dialog
             *
             * @return the confirmation options
             */
            public Button getButton() {
                return button;
            }

            /**
             * Sets the confirmation options in a dialog
             *
             * @param button
             *            the confirmation options
             */
            public void setButton(Button button) {
                this.button = button;
            }

            /**
             * Gets the title on a dialog
             *
             * @return the title
             */
            public String getTitle() {
                return title;
            }

            /**
             * Sets the title on a dialog
             *
             * @param title
             *            the title
             */
            public void setTitle(String title) {
                this.title = title;
            }

            @Override
            public String toString() {
                return getClass().getSimpleName() + "{" + "content='" + content
                        + '\'' + ", confirmationOptions=" + button + ", title='"
                        + title + '\'' + '}';
            }

            /**
             * The confirmation options on a dialog
             */
            public static class Button implements Serializable {

                private String confirm;
                private String dismiss;

                /**
                 * Gets the confirm text
                 *
                 * @return the confirm text
                 */
                public String getConfirm() {
                    return confirm;
                }

                /**
                 * Sets the confirm text
                 *
                 * @param confirm
                 *            the confirm text
                 */
                public void setConfirm(String confirm) {
                    this.confirm = confirm;
                }

                /**
                 * Gets the dismiss text
                 *
                 * @return the dismiss text
                 */
                public String getDismiss() {
                    return dismiss;
                }

                /**
                 * Sets the dismiss text
                 *
                 * @param dismiss
                 *            the dismiss text
                 */
                public void setDismiss(String dismiss) {
                    this.dismiss = dismiss;
                }

                @Override
                public String toString() {
                    return "Button{" + "confirm='" + confirm + '\''
                            + ", dismiss='" + dismiss + '\'' + '}';
                }
            }
        }
    }
}
