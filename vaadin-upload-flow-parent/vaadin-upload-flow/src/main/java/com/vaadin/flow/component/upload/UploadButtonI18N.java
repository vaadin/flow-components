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
package com.vaadin.flow.component.upload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The I18N helper class for the {@link UploadButton} component.
 * <p>
 * This class contains the translations used by the upload button:
 * <ul>
 * <li>{@code addFiles} - Button text for single and multi-file upload
 * modes</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadButtonI18N implements Serializable {

    private AddFiles addFiles;

    /**
     * Translations for the button text in single and multi-file upload modes.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddFiles implements Serializable {
        private String one;
        private String many;

        /**
         * Gets the button text for single file upload mode.
         *
         * @return translation string or {@code null} if none set
         */
        public String getOne() {
            return one;
        }

        /**
         * Sets the button text for single file upload mode.
         *
         * @param one
         *            translation string
         * @return this instance for chaining
         */
        public AddFiles setOne(String one) {
            this.one = one;
            return this;
        }

        /**
         * Gets the button text for multi-file upload mode.
         *
         * @return translation string or {@code null} if none set
         */
        public String getMany() {
            return many;
        }

        /**
         * Sets the button text for multi-file upload mode.
         *
         * @param many
         *            translation string
         * @return this instance for chaining
         */
        public AddFiles setMany(String many) {
            this.many = many;
            return this;
        }
    }

    /**
     * Gets the add files translations.
     *
     * @return add files translations
     */
    public AddFiles getAddFiles() {
        return addFiles;
    }

    /**
     * Sets the add files translations.
     *
     * @param addFiles
     *            add files translations
     * @return this instance for chaining
     */
    public UploadButtonI18N setAddFiles(AddFiles addFiles) {
        this.addFiles = addFiles;
        return this;
    }
}
