/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.tests.dirtystate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.HasDirtyState;

public abstract class AbstractDirtyStatePage<T extends Component & HasDirtyState>
        extends Div {
    public static final String DIRTY_STATE = "dirty-state";
    public static final String DIRTY_STATE_BUTTON = "dirty-state-button";

    protected T testField;

    protected AbstractDirtyStatePage() {
        testField = createTestField();

        Div dirtyState = new Div();
        dirtyState.setId(DIRTY_STATE);

        NativeButton dirtyStateButton = new NativeButton("Retrieve dirty state",
                event -> {
                    boolean isDirty = testField.isDirty();
                    dirtyState.setText(String.valueOf(isDirty));
                });
        dirtyStateButton.setId(DIRTY_STATE_BUTTON);

        add(testField, dirtyState, dirtyStateButton);
    }

    /**
     * A field to test.
     */
    protected abstract T createTestField();
}
