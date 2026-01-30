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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

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
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha3")
@JsModule("@vaadin/upload/src/vaadin-upload-file-list.js")
public class UploadFileList extends Component implements HasUploadManager {

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

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Element state is not persisted across attach/detach
        if (this.i18n != null) {
            setI18nWithJS();
        }
    }

    private void setI18nWithJS() {
        ObjectNode i18nJson = JacksonUtils.beanToJson(this.i18n);

        // Assign new I18N object to WC, by deeply merging the existing
        // WC I18N, and the values from the new UploadFileListI18N instance,
        // into an empty object
        getElement().executeJs(
                "const file = Object.assign({}, this.i18n.file, $0.file);"
                        + "const error = Object.assign({}, this.i18n.error, $0.error);"
                        + "const uploadingStatus = Object.assign({}, this.i18n.uploading.status, $0.uploading && $0.uploading.status);"
                        + "const uploadingRemainingTime = Object.assign({}, this.i18n.uploading.remainingTime, $0.uploading && $0.uploading.remainingTime);"
                        + "const uploadingError = Object.assign({}, this.i18n.uploading.error, $0.uploading && $0.uploading.error);"
                        + "const uploading = {status: uploadingStatus,"
                        + "  remainingTime: uploadingRemainingTime,"
                        + "  error: uploadingError};"
                        + "const units = $0.units || this.i18n.units;"
                        + "this.i18n = Object.assign({}, this.i18n, $0, {"
                        + "  file: file, error: error, uploading: uploading, units: units});",
                i18nJson);
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }
}
