package com.vaadin.flow.component.login;

/*
 * #%L
 * Vaadin Login for Vaadin
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

/**
 * Server-side abstract component for the {@code <vaadin-login>} component.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-login")
@HtmlImport("frontend://bower_components/vaadin-login/src/vaadin-login.html")
public class Login extends AbstractLogin {

    public Login() {
    }

    public Login(LoginI18n i18n) {
        super(i18n);
    }

}
