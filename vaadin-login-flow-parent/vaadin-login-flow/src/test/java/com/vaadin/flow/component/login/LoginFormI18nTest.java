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
package com.vaadin.flow.component.login;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoginFormI18nTest {

    @Test
    void createDefault() {
        LoginI18n i18n = LoginI18n.createDefault();

        Assertions.assertEquals("Log in", i18n.getForm().getTitle());
        Assertions.assertEquals("Username", i18n.getForm().getUsername());
        Assertions.assertEquals("Password", i18n.getForm().getPassword());
        Assertions.assertEquals("Forgot password",
                i18n.getForm().getForgotPassword());
        Assertions.assertEquals("Log in", i18n.getForm().getSubmit());

        Assertions.assertEquals("Incorrect username or password",
                i18n.getErrorMessage().getTitle());
        Assertions.assertEquals(
                "Check that you have entered the correct username and password and try again.",
                i18n.getErrorMessage().getMessage());

        Assertions.assertEquals("Username is required",
                i18n.getErrorMessage().getUsername());
        Assertions.assertEquals("Password is required",
                i18n.getErrorMessage().getPassword());

        Assertions.assertNull(i18n.getAdditionalInformation());
    }
}
