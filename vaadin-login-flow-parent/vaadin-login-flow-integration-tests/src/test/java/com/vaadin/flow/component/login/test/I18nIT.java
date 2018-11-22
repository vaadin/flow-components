package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class I18nIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/ptbr");
    }

    @Test
    public void testI18n() {
        LoginElement login = $(LoginElement.class).waitForFirst();
        Assert.assertEquals("Nome do aplicativo", login.getTitle());
        Assert.assertEquals("Descrição do aplicativo", login.getMessage());
        Assert.assertEquals("Acesse a sua conta", login.getFormTitle());
        Assert.assertEquals("Usuário", login.getUsernameField().getLabel());
        Assert.assertEquals("Senha", login.getPasswordField().getLabel());
        Assert.assertEquals("Entrar", login.getSubmitButton().getText());
        Assert.assertEquals("Esqueci minha senha",
            login.getForgotPasswordButton().getText());
        Assert.assertEquals(
            "Caso necessite apresentar alguma informação extra para o usuário (como credenciais padrão), este é o lugar.",
            login.getAdditionalInformation());
    }

}
