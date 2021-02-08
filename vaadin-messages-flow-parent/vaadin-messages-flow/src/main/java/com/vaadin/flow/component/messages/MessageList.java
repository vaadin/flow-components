/*
 * Copyright 2000-2021 Vaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.messages;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;

/**
 * Server-side component for the {@code vaadin-message-list} element.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-message-list")
// @JsModule("@vaadin/vaadin-messages/src/vaadin-message-list.js")
// @NpmPackage(value = "@vaadin/vaadin-messages", version = "1.0.0")
public class MessageList extends Component implements HasStyle, HasSize {

    public MessageList() {
        getElement().appendChild(new Text("This is MessageList").getElement());
    }
}
