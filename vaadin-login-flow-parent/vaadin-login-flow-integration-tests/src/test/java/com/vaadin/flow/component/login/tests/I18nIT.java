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
package com.vaadin.flow.component.login.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-login/overlay/ptbr")
public class I18nIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void testI18n() {
        $("button").waitForFirst().click();
        LoginOverlayElement login = $(LoginOverlayElement.class).waitForFirst();
        Assert.assertEquals("Nome do aplicativo", login.getTitle());
        Assert.assertEquals("Descrição do aplicativo", login.getDescription());
        Assert.assertEquals("Acesse a sua conta", login.getFormTitle());
        Assert.assertEquals("Usuário", login.getUsernameField().getLabel());
        Assert.assertEquals("Senha", login.getPasswordField().getLabel());
        Assert.assertEquals("Entrar", login.getSubmitButton().getText().trim());
        Assert.assertEquals("Esqueci minha senha",
                login.getForgotPasswordButton().getText().trim());
        Assert.assertEquals(
                "Caso necessite apresentar alguma informação extra para o usuário (como credenciais padrão), este é o lugar.",
                login.getAdditionalInformation());
    }

}
