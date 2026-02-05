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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * A component that displays the list of files being uploaded. When linked to an
 * {@link UploadManager}, it automatically displays upload progress, status, and
 * controls for each file.
 * <p>
 * The component automatically syncs files from the manager and forwards
 * retry/abort/start events back to the manager.
 * <p>
 * Example usage with UploadManager:
 *
 * <pre>
 * var manager = new UploadManager(uploadHandler);
 * var fileList = new UploadFileList(manager);
 * add(fileList);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-file-list")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha6")
@JsModule("@vaadin/upload/src/vaadin-upload-file-list.js")
public class UploadFileList extends Component implements HasUploadManager,
        HasThemeVariant<UploadFileListVariant>, HasSize {

    private UploadFileListI18N i18n;

    /**
     * Creates a new empty file list without a manager. The manager must be set
     * later using {@link #setUploadManager(UploadManager)}.
     */
    public UploadFileList() {
    }

    /**
     * Creates a new file list linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to, not {@code null}
     * @throws NullPointerException
     *             if manager is {@code null}
     */
    public UploadFileList(UploadManager manager) {
        setUploadManager(Objects.requireNonNull(manager,
                "manager cannot be null, use the default constructor instead"));
    }

    /**
     * Set the internationalization properties for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(UploadFileListI18N i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
    }

    /**
     * Get the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(UploadFileListI18N)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public UploadFileListI18N getI18n() {
        return i18n;
    }
}
