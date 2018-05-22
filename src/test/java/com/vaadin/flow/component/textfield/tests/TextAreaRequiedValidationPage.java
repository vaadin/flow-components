/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

@Route("text-area-required-binder")
public class TextAreaRequiedValidationPage
        extends AbstractRequiredValidationPage<TextArea> {

    @Override
    protected TextArea createTextField() {
        return new TextArea();
    }

    @Override
    protected void setRequired(TextArea field) {
        field.setRequired(true);
    }

}
