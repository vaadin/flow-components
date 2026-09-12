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
package com.vaadin.flow.component.shared.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Says that the React components bring the npm packages a class declares with
 * {@link NpmPackage}, so that a React application installs the React components
 * instead of installing those packages of their own.
 * <p>
 * The versions file the build generates writes it down for each package, and
 * the packages keep the version they are declared with either way.
 * <p>
 * A commercial component names {@link #PRO}, and a package the React components
 * do not bring, such as one a connector uses for itself or a theme, is left
 * without the annotation:
 *
 * <pre>
 * &#64;NpmPackage(value = "@vaadin/date-picker", version = "25.3.0")
 * &#64;NpmPackage(value = "date-fns", version = "4.4.0")
 * &#64;ReactComponents(packages = "@vaadin/date-picker")
 * public class DatePicker ...
 * </pre>
 * <p>
 * The annotation is read from the sources when the versions file is generated,
 * so it is not kept in the class file.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ReactComponents {

    /**
     * The npm package of the React components of the core components.
     */
    String CORE = "@vaadin/react-components";

    /**
     * The npm package of the React components of the commercial components.
     */
    String PRO = "@vaadin/react-components-pro";

    /**
     * The React components bringing the packages, the core ones by default.
     *
     * @return the npm package of the React components
     */
    String value() default CORE;

    /**
     * The packages of the class the React components bring, all of the ones it
     * declares by default.
     *
     * @return the npm packages the React components bring
     */
    String[] packages() default {};
}
