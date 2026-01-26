/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;

/**
 * Helper class for linking upload components to an {@link UploadManager}.
 * <p>
 * This class provides shared functionality for {@link UploadButton},
 * {@link UploadDropZone}, and {@link UploadFileList} to link to an upload
 * manager on the client side.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd.
 */
class UploadManagerLink implements Serializable {

    private UploadManagerLink() {
        // Utility class
    }

    /**
     * Sets up the link between the component and the manager. This ensures the
     * client-side component is linked to the manager both when attached and
     * before the first client response.
     *
     * @param component
     *            the component to link
     * @param manager
     *            the upload manager to link to
     */
    static void link(Component component, UploadManager manager) {
        setTarget(component, manager);
        component.addAttachListener(event -> setTarget(component, manager));
    }

    private static void setTarget(Component component, UploadManager manager) {
        component.getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(component,
                        context -> component.getElement().executeJs(
                                "this.manager = $0.manager",
                                manager.getConnector())));
    }
}
