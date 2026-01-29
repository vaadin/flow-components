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
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The I18N helper class for the {@link UploadFileList} component.
 * <p>
 * This class contains the translations used by the file list display:
 * <ul>
 * <li>{@code file} - Button aria-labels (retry, start, remove)</li>
 * <li>{@code error} - Validation error messages (tooManyFiles, fileIsTooBig,
 * incorrectFileType)</li>
 * <li>{@code uploading} - Status messages, remaining time, and upload
 * errors</li>
 * <li>{@code units} - File size units</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadFileListI18N implements Serializable {

    private File file;
    private Error error;
    private Uploading uploading;
    private Units units;

    /**
     * File button aria-label translations.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class File implements Serializable {
        private String retry;
        private String start;
        private String remove;

        /**
         * Get aria-label of the retry file button.
         *
         * @return aria-label translation string
         */
        public String getRetry() {
            return retry;
        }

        /**
         * Set aria-label on the retry file button.
         *
         * @param retry
         *            aria-label translation string
         * @return this instance for chaining
         */
        public File setRetry(String retry) {
            this.retry = retry;
            return this;
        }

        /**
         * Get aria-label of the start file button.
         *
         * @return aria-label translation string
         */
        public String getStart() {
            return start;
        }

        /**
         * Set aria-label on the start file button.
         *
         * @param start
         *            aria-label translation string
         * @return this instance for chaining
         */
        public File setStart(String start) {
            this.start = start;
            return this;
        }

        /**
         * Get aria-label of the remove file button.
         *
         * @return aria-label translation string
         */
        public String getRemove() {
            return remove;
        }

        /**
         * Set aria-label on the remove file button.
         *
         * @param remove
         *            aria-label translation string
         * @return this instance for chaining
         */
        public File setRemove(String remove) {
            this.remove = remove;
            return this;
        }
    }

    /**
     * Validation error translations displayed in file items.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Error implements Serializable {
        private String tooManyFiles;
        private String fileIsTooBig;
        private String incorrectFileType;

        /**
         * Gets the too many files error message.
         *
         * @return translation string or null if none set
         */
        public String getTooManyFiles() {
            return tooManyFiles;
        }

        /**
         * Sets the too many files error message.
         *
         * @param tooManyFiles
         *            translation string
         * @return this instance for chaining
         */
        public Error setTooManyFiles(String tooManyFiles) {
            this.tooManyFiles = tooManyFiles;
            return this;
        }

        /**
         * Gets the file is too big error message.
         *
         * @return translation string or null if none set
         */
        public String getFileIsTooBig() {
            return fileIsTooBig;
        }

        /**
         * Sets the file is too big error message.
         *
         * @param fileIsTooBig
         *            translation string
         * @return this instance for chaining
         */
        public Error setFileIsTooBig(String fileIsTooBig) {
            this.fileIsTooBig = fileIsTooBig;
            return this;
        }

        /**
         * Gets the incorrect file type error message.
         *
         * @return translation string or null if none set
         */
        public String getIncorrectFileType() {
            return incorrectFileType;
        }

        /**
         * Sets the incorrect file type error message.
         *
         * @param incorrectFileType
         *            translation string
         * @return this instance for chaining
         */
        public Error setIncorrectFileType(String incorrectFileType) {
            this.incorrectFileType = incorrectFileType;
            return this;
        }
    }

    /**
     * Upload time translation strings.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Uploading implements Serializable {
        private Status status;
        private RemainingTime remainingTime;
        private UploadError error;

        /**
         * Get status translations.
         *
         * @return status translations
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Set status translations.
         *
         * @param status
         *            status translations
         * @return this instance for chaining
         */
        public Uploading setStatus(Status status) {
            this.status = status;
            return this;
        }

        /**
         * Get remaining time translations.
         *
         * @return remaining time translations
         */
        public RemainingTime getRemainingTime() {
            return remainingTime;
        }

        /**
         * Set remaining time translations.
         *
         * @param remainingTime
         *            remaining time translations
         * @return this instance for chaining
         */
        public Uploading setRemainingTime(RemainingTime remainingTime) {
            this.remainingTime = remainingTime;
            return this;
        }

        /**
         * Get upload error translations.
         *
         * @return error translations
         */
        public UploadError getError() {
            return error;
        }

        /**
         * Set upload error translations.
         *
         * @param error
         *            error translations
         * @return this instance for chaining
         */
        public Uploading setError(UploadError error) {
            this.error = error;
            return this;
        }

        /**
         * Upload status strings.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Status implements Serializable {
            private String connecting;
            private String stalled;
            private String processing;
            private String held;

            /**
             * Get connecting translation.
             *
             * @return translation string
             */
            public String getConnecting() {
                return connecting;
            }

            /**
             * Set connecting translation.
             *
             * @param connecting
             *            translation string
             * @return this instance for chaining
             */
            public Status setConnecting(String connecting) {
                this.connecting = connecting;
                return this;
            }

            /**
             * Get stalled translation.
             *
             * @return translation string
             */
            public String getStalled() {
                return stalled;
            }

            /**
             * Set stalled translation.
             *
             * @param stalled
             *            translation string
             * @return this instance for chaining
             */
            public Status setStalled(String stalled) {
                this.stalled = stalled;
                return this;
            }

            /**
             * Get processing translation.
             *
             * @return translation string
             */
            public String getProcessing() {
                return processing;
            }

            /**
             * Set processing translation.
             *
             * @param processing
             *            translation string
             * @return this instance for chaining
             */
            public Status setProcessing(String processing) {
                this.processing = processing;
                return this;
            }

            /**
             * Get held translation.
             *
             * @return translation string
             */
            public String getHeld() {
                return held;
            }

            /**
             * Set held translation.
             *
             * @param held
             *            translation string
             * @return this instance for chaining
             */
            public Status setHeld(String held) {
                this.held = held;
                return this;
            }
        }

        /**
         * Time remaining translations.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class RemainingTime implements Serializable {
            private String prefix;
            private String unknown;

            /**
             * Get prefix translation.
             *
             * @return translation string
             */
            public String getPrefix() {
                return prefix;
            }

            /**
             * Set prefix translation.
             *
             * @param prefix
             *            translation string
             * @return this instance for chaining
             */
            public RemainingTime setPrefix(String prefix) {
                this.prefix = prefix;
                return this;
            }

            /**
             * Get unknown translation.
             *
             * @return translation string
             */
            public String getUnknown() {
                return unknown;
            }

            /**
             * Set unknown translation.
             *
             * @param unknown
             *            translation string
             * @return this instance for chaining
             */
            public RemainingTime setUnknown(String unknown) {
                this.unknown = unknown;
                return this;
            }
        }

        /**
         * Communication error translations.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class UploadError implements Serializable {
            private String serverUnavailable;
            private String unexpectedServerError;
            private String forbidden;

            /**
             * Gets the server unavailable error message.
             *
             * @return translation string
             */
            public String getServerUnavailable() {
                return serverUnavailable;
            }

            /**
             * Sets the server unavailable error message.
             *
             * @param serverUnavailable
             *            translation string
             * @return this instance for chaining
             */
            public UploadError setServerUnavailable(String serverUnavailable) {
                this.serverUnavailable = serverUnavailable;
                return this;
            }

            /**
             * Gets the unexpected server error message.
             *
             * @return translation string
             */
            public String getUnexpectedServerError() {
                return unexpectedServerError;
            }

            /**
             * Sets the unexpected server error message.
             *
             * @param unexpectedServerError
             *            translation string
             * @return this instance for chaining
             */
            public UploadError setUnexpectedServerError(
                    String unexpectedServerError) {
                this.unexpectedServerError = unexpectedServerError;
                return this;
            }

            /**
             * Gets the forbidden error message.
             *
             * @return translation string
             */
            public String getForbidden() {
                return forbidden;
            }

            /**
             * Sets the forbidden error message.
             *
             * @param forbidden
             *            translation string
             * @return this instance for chaining
             */
            public UploadError setForbidden(String forbidden) {
                this.forbidden = forbidden;
                return this;
            }
        }
    }

    /**
     * Unit translations for file sizes.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Units implements Serializable {
        private List<String> size = Arrays.asList("B", "kB", "MB", "GB", "TB",
                "PB", "EB", "ZB", "YB");
        private Integer sizeBase;

        /**
         * Creates unit translations with default size units.
         */
        public Units() {
        }

        /**
         * Creates unit translations with the given size units.
         *
         * @param size
         *            list of unit translations
         */
        public Units(List<String> size) {
            this.size = size;
        }

        /**
         * Creates unit translations with the given size units and base.
         *
         * @param size
         *            list of unit translations
         * @param sizeBase
         *            units size base
         */
        public Units(List<String> size, Integer sizeBase) {
            this.size = size;
            this.sizeBase = sizeBase;
        }

        /**
         * Gets the units size list.
         *
         * @return list of unit strings
         */
        public List<String> getSize() {
            return size;
        }

        /**
         * Sets the units size list.
         *
         * @param size
         *            list of unit translations
         * @return this instance for chaining
         */
        public Units setSize(List<String> size) {
            this.size = size;
            return this;
        }

        /**
         * Gets the units size base.
         *
         * @return the units size base
         */
        public Integer getSizeBase() {
            return sizeBase;
        }

        /**
         * Sets the units size base.
         *
         * @param sizeBase
         *            units size base
         * @return this instance for chaining
         */
        public Units setSizeBase(Integer sizeBase) {
            this.sizeBase = sizeBase;
            return this;
        }
    }

    /**
     * Get file button translations.
     *
     * @return file translations
     */
    public File getFile() {
        return file;
    }

    /**
     * Set file button translations.
     *
     * @param file
     *            file translations
     * @return this instance for chaining
     */
    public UploadFileListI18N setFile(File file) {
        this.file = file;
        return this;
    }

    /**
     * Get error translations.
     *
     * @return error translations
     */
    public Error getError() {
        return error;
    }

    /**
     * Set error translations.
     *
     * @param error
     *            error translations
     * @return this instance for chaining
     */
    public UploadFileListI18N setError(Error error) {
        this.error = error;
        return this;
    }

    /**
     * Get uploading translations.
     *
     * @return uploading translations
     */
    public Uploading getUploading() {
        return uploading;
    }

    /**
     * Set uploading translations.
     *
     * @param uploading
     *            uploading translations
     * @return this instance for chaining
     */
    public UploadFileListI18N setUploading(Uploading uploading) {
        this.uploading = uploading;
        return this;
    }

    /**
     * Get unit translations.
     *
     * @return unit translations
     */
    public Units getUnits() {
        return units;
    }

    /**
     * Set unit translations.
     *
     * @param units
     *            unit translations
     * @return this instance for chaining
     */
    public UploadFileListI18N setUnits(Units units) {
        this.units = units;
        return this;
    }

    /**
     * Set unit translations.
     *
     * @param units
     *            list of unit translations
     * @return this instance for chaining
     */
    public UploadFileListI18N setUnits(List<String> units) {
        this.units = new Units(units);
        return this;
    }

    /**
     * Set unit translations.
     *
     * @param units
     *            list of unit translations
     * @param sizeBase
     *            units size base
     * @return this instance for chaining
     */
    public UploadFileListI18N setUnits(List<String> units, int sizeBase) {
        this.units = new Units(units, sizeBase);
        return this;
    }
}
