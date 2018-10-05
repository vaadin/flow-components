package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * Internationalization object for customizing the component UI texts.
 * An instance with the default messages can be obtained using {@link CrudI18n#createDefault()}
 *
 * @see Crud#setI18n(CrudI18n)
 */
public class CrudI18n implements Serializable {

    private static final JsonValue DEFAULT_I18N;

    private String newItem;
    private String editItem;
    private String save;
    private String cancel;
    private String delete;
    private Confirmations confirm;

    static {
        try {
            final JsonFactory JSON_FACTORY = new JreJsonFactory();
            DEFAULT_I18N = JSON_FACTORY.parse(IOUtils.toString(
                    CrudI18n.class.getResource("/i18n.json")));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find the default i18n configuration");
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
     * Gets the new button and editor header text
     *
     * @return the new button and editor header text
     */
    public String getNewItem() {
        return newItem;
    }

    /**
     * Sets the new button and editor header text
     *
     * @param newItem the new button and editor header text
     */
    public void setNewItem(String newItem) {
        this.newItem = newItem;
    }

    /**
     * Gets the save button text
     *
     * @return the save button text
     */
    public String getSave() {
        return save;
    }

    /**
     * Sets the save button text
     *
     * @param save the save button text
     */
    public void setSave(String save) {
        this.save = save;
    }

    /**
     * Gets the delete button text
     *
     * @return the delete button text
     */
    public String getDelete() {
        return delete;
    }

    /**
     * Sets the delete button text
     *
     * @param delete the delete button text
     */
    public void setDelete(String delete) {
        this.delete = delete;
    }

    /**
     * Gets the edit editor header text
     *
     * @return the edit editor header text
     */
    public String getEditItem() {
        return editItem;
    }

    /**
     * Sets the edit editor header text
     *
     * @param editItem the edit editor header text
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
     * @param cancel the cancel button text
     */
    public void setCancel(String cancel) {
        this.cancel = cancel;
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
     * @param confirm the confirmation dialogs
     */
    public void setConfirm(Confirmations confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return "CrudI18n{" +
                "newItem='" + newItem + '\'' +
                ", save='" + save + '\'' +
                ", delete='" + delete + '\'' +
                ", editItem='" + editItem + '\'' +
                ", cancel='" + cancel + '\'' +
                ", confirm=" + confirm +
                '}';
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
         * @param delete the delete confirmation dialog
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
         * @param cancel the cancel confirmation dialog
         */
        public void setCancel(Confirmation cancel) {
            this.cancel = cancel;
        }

        @Override
        public String toString() {
            return "Confirmations{" +
                    "delete=" + delete +
                    ", cancel=" + cancel +
                    '}';
        }

        /**
         * Represents texts in the confirmation dialogs
         */
        public static class Confirmation implements Serializable {

            private String message;
            private Button button;
            private String header;

            /**
             * Gets the main message in a dialog
             *
             * @return the main message
             */
            public String getMessage() {
                return message;
            }

            /**
             * Sets the main message in a dialog
             *
             * @param message the main message
             */
            public void setMessage(String message) {
                this.message = message;
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
             * @param button the confirmation options
             */
            public void setButton(Button button) {
                this.button = button;
            }

            /**
             * Gets the header on a dialog
             *
             * @return the header
             */
            public String getHeader() {
                return header;
            }

            /**
             * Sets the header on a dialog
             *
             * @param header the header
             */
            public void setHeader(String header) {
                this.header = header;
            }

            @Override
            public String toString() {
                return getClass().getSimpleName() + "{" +
                        "message='" + message + '\'' +
                        ", confirmationOptions=" + button +
                        ", header='" + header + '\'' +
                        '}';
            }

            /**
             * The confirmation options on a dialog
             */
            public static class Button implements Serializable {

                private String ok;
                private String cancel;

                /**
                 * Gets the ok text
                 *
                 * @return the ok text
                 */
                public String getOk() {
                    return ok;
                }

                /**
                 * Sets the ok text
                 *
                 * @param ok the ok text
                 */
                public void setOk(String ok) {
                    this.ok = ok;
                }

                /**
                 * Gets the cancel text
                 *
                 * @return the cancel text
                 */
                public String getCancel() {
                    return cancel;
                }

                /**
                 * Sets the cancel text
                 *
                 * @param cancel the cancel text
                 */
                public void setCancel(String cancel) {
                    this.cancel = cancel;
                }

                @Override
                public String toString() {
                    return "Button{" +
                            "ok='" + ok + '\'' +
                            ", cancel='" + cancel + '\'' +
                            '}';
                }
            }
        }
    }
}
