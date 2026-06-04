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
package com.vaadin.flow.repro;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Placeholder view. Replace the content with a reproduction for the issue under
 * investigation.
 */
@Route("")
public class ReproView extends Div {

    public ReproView() {
        add(new Div("Replace this view with a reproduction"));
    }
}
