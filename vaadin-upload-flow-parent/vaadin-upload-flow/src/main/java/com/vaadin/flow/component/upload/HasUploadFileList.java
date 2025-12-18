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

/**
 * A marker interface for components that can be used as a file list with
 * {@link Upload#setFileList(HasUploadFileList)}.
 * <p>
 * The implementing component should be able to handle the {@code items},
 * {@code i18n}, and {@code disabled} properties set by the Upload component,
 * and should fire {@code file-retry}, {@code file-abort}, and
 * {@code file-start} events.
 *
 * @author Vaadin Ltd.
 * @see Upload#setFileList(HasUploadFileList)
 * @see UploadFileList
 */
public interface HasUploadFileList extends Serializable {
}
