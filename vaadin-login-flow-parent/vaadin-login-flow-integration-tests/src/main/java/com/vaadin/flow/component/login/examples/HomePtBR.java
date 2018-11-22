package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.router.Route;

@Route(value = "ptbr")
public class HomePtBR extends Div {

    public HomePtBR() {
        this.setSizeFull();
        final LoginI18n i18n = LoginI18n.createDefault();
        i18n.getHeader().setTitle("Nome do aplicativo");
        i18n.getHeader().setDescription("Descrição do aplicativo");
        i18n.getForm().setUsername("Usuário");
        i18n.getForm().setTitle("Acesse a sua conta");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setPassword("Senha");
        i18n.getForm().setForgotPassword("Esqueci minha senha");
        i18n.getErrorMessage().setTitle("Usuário/senha inválidos");
        i18n.getErrorMessage()
            .setMessage("Confira seu usuário e senha e tente novamente.");
        i18n.setAdditionalInformation(
            "Caso necessite apresentar alguma informação extra para o usuário (como credenciais padrão), este é o lugar.");
        add(new Login(i18n));
    }

}
