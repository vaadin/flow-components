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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.shared.Registration;

/**
 * Mixin interface for components that can be linked to an
 * {@link UploadManager}.
 * <p>
 * This interface provides the common functionality for linking upload-related
 * components (like {@link UploadButton}, {@link UploadDropZone}, and
 * {@link UploadFileList}) to an upload manager on the client side.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd.
 */
interface HasUploadManager extends Serializable {

    static final String ATTACH_LISTENER_REGISTRATION = "UploadManagerAttachListenerRegistration";

    /**
     * Gets the upload manager that this component is linked to.
     *
     * @return the upload manager, or {@code null} if not linked
     */
    default UploadManager getUploadManager() {
        return ComponentUtil.getData((Component) this, UploadManager.class);
    }

    /**
     * Sets the upload manager that this component is linked to.
     *
     * @param manager
     *            the upload manager, or {@code null} to unlink
     */
    default void setUploadManager(UploadManager manager) {
        Component component = (Component) this;
        ComponentUtil.setData(component, UploadManager.class, manager);
        var oldRegistration = ComponentUtil.getData(component,
                ATTACH_LISTENER_REGISTRATION);
        if (oldRegistration instanceof Registration registration) {
            registration.remove();
        }
        ComponentUtil.setData(component, ATTACH_LISTENER_REGISTRATION, component
                .addAttachListener(event -> linkToManager(component, manager)));
        linkToManager(component, manager);
    }

    private static void linkToManager(Component component,
            UploadManager manager) {
        component.getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(component,
                        context -> component.getElement().executeJs(
                                "this.manager = $0 ? $0.manager : null",
                                manager != null ? manager.getConnector()
                                        : null)));
    }
}
