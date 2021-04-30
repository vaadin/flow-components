package com.vaadin.flow.component.login.test;

import com.vaadin.flow.component.login.testbench.LoginOverlayElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class I18nIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-login") + "/overlay/ptbr";
        getDriver().get(url);
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
        Assert.assertEquals("Entrar", login.getSubmitButton().getText());
        Assert.assertEquals("Esqueci minha senha",
            login.getForgotPasswordButton().getText());
        Assert.assertEquals(
            "Caso necessite apresentar alguma informação extra para o usuário (como credenciais padrão), este é o lugar.",
            login.getAdditionalInformation());
    }

}
