---
description: Sets up the scaffolding for a new Vaadin Flow component
argument-hint: <ComponentName>
---

You are setting up the file scaffolding for a new Vaadin Flow component module. You will create ALL the required files following the existing module patterns precisely.

Arguments: [ComponentName]

The component name is given in PascalCase (e.g. `Breadcrumb`, `DatePicker`). Derive:
- **kebab-name**: PascalCase → kebab-case (e.g. `DatePicker` → `date-picker`)
- **Human Name**: PascalCase → space-separated (e.g. `DatePicker` → `Date Picker`)

## Steps

### 1. Determine the current NpmPackage version

Read the `@NpmPackage` annotation from an existing component, e.g. `vaadin-button-flow-parent/vaadin-button-flow/src/main/java/com/vaadin/flow/component/button/Button.java`. Extract the version string (e.g. `25.2.0-alpha6`) to use for the new component's `@NpmPackage` annotation.

### 2. Create the parent module directory structure

Create all these files under `vaadin-{kebab-name}-flow-parent/`:

```
vaadin-{kebab-name}-flow-parent/
├── pom.xml
├── vaadin-{kebab-name}-flow/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/vaadin/flow/component/{kebab-name-without-hyphens}/
│       │   └── {ComponentName}.java
│       └── test/java/com/vaadin/flow/component/{kebab-name-without-hyphens}/
│           └── {ComponentName}SerializableTest.java
├── vaadin-{kebab-name}-testbench/
│   ├── pom.xml
│   └── src/main/java/com/vaadin/flow/component/{kebab-name-without-hyphens}/testbench/
│       └── {ComponentName}Element.java
└── vaadin-{kebab-name}-flow-integration-tests/
    ├── pom.xml
    ├── vite.config.ts
    └── src/
        ├── main/java/
        │   ├── com/vaadin/flow/component/app/
        │   │   └── TestAppShell.java
        │   └── com/vaadin/flow/component/{kebab-name-without-hyphens}/tests/
        │       └── {ComponentName}Page.java
        └── test/java/com/vaadin/flow/component/{kebab-name-without-hyphens}/tests/
            └── {ComponentName}IT.java
```

**Note on Java package names**: The Java package uses the kebab-name with hyphens removed (e.g. `datepicker` for `date-picker`, `checkbox` for `checkbox`). Check existing components for the convention.

### 3. File contents

Use the following templates for each file. Base them on the `vaadin-card-flow-parent` module as the reference, since Card is a simple, recent component.

#### Parent pom.xml (`vaadin-{kebab-name}-flow-parent/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-flow-components</artifactId>
        <version>25.2-SNAPSHOT</version>
    </parent>
    <artifactId>vaadin-{kebab-name}-flow-parent</artifactId>
    <packaging>pom</packaging>
    <name>Vaadin {Human Name} Parent</name>
    <description>Vaadin {Human Name} Parent</description>
    <modules>
        <module>vaadin-{kebab-name}-flow</module>
        <module>vaadin-{kebab-name}-testbench</module>
    </modules>
    <dependencies/>
    <profiles>
        <profile>
            <id>default</id>
            <activation>
                <property>
                    <name>!release</name>
                </property>
            </activation>
            <modules>
                <module>vaadin-{kebab-name}-flow-integration-tests</module>
            </modules>
        </profile>
    </profiles>
</project>
```

#### Component flow pom.xml (`vaadin-{kebab-name}-flow/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-{kebab-name}-flow-parent</artifactId>
        <version>25.2-SNAPSHOT</version>
    </parent>
    <artifactId>vaadin-{kebab-name}-flow</artifactId>
    <packaging>jar</packaging>
    <name>Vaadin {Human Name}</name>
    <description>Vaadin {Human Name}</description>
    <properties>
        <surefire.argLine>-javaagent:${org.mockito:mockito-core:jar}</surefire.argLine>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-html-components</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-test-generic</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-test-util</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-flow-components-base</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-flow-components-test-util</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-web-api</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>biz.aQute.bnd</groupId>
                <artifactId>bnd-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <profiles>
        <profile>
            <id>attach-docs</id>
            <activation>
                <property>
                    <name>with-docs</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-source-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>build-helper-maven-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

#### Testbench pom.xml (`vaadin-{kebab-name}-testbench/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-{kebab-name}-flow-parent</artifactId>
        <version>25.2-SNAPSHOT</version>
    </parent>
    <artifactId>vaadin-{kebab-name}-testbench</artifactId>
    <packaging>jar</packaging>
    <name>Vaadin {Human Name} Testbench API</name>
    <description>Vaadin {Human Name} Testbench API</description>
    <dependencies>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-testbench-shared</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins/>
    </build>
    <profiles>
        <profile>
            <id>attach-docs</id>
            <activation>
                <property>
                    <name>with-docs</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-source-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

#### Integration tests pom.xml (`vaadin-{kebab-name}-flow-integration-tests/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-{kebab-name}-flow-parent</artifactId>
        <version>25.2-SNAPSHOT</version>
    </parent>
    <artifactId>vaadin-{kebab-name}-integration-tests</artifactId>
    <packaging>war</packaging>
    <name>Vaadin {Human Name} Integration Tests</name>
    <description>Vaadin {Human Name} Integration Tests</description>
    <dependencies>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-data</artifactId>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-html-components</artifactId>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>flow-test-util</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-{kebab-name}-flow</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-{kebab-name}-testbench</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-dev-server</artifactId>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-flow-components-test-util</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-lumo-theme</artifactId>
        </dependency>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-testbench-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-clean-plugin</artifactId>
                <configuration>
                    <filesets>
                        <fileset>
                            <directory>${project.basedir}</directory>
                            <includes>
                                <include>package*.json</include>
                                <include>pnpm*</include>
                                <include>vite.generated.ts</include>
                                <include>types.d.ts</include>
                                <include>tsconfig.json</include>
                                <include>frontend/routes.tsx</include>
                                <include>frontend/App.tsx</include>
                            </includes>
                        </fileset>
                        <fileset>
                            <directory>${project.basedir}/node_modules</directory>
                        </fileset>
                        <fileset>
                            <directory>${project.basedir}/frontend/generated</directory>
                        </fileset>
                    </filesets>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-failsafe-plugin</artifactId>
            </plugin>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.sonatype.plugins</groupId>
                <artifactId>nexus-staging-maven-plugin</artifactId>
                <configuration>
                    <skipNexusStagingDeployMojo>true</skipNexusStagingDeployMojo>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>properties-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <artifactId>maven-install-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <profiles>
        <profile>
            <id>build-frontend</id>
            <activation>
                <property>
                    <name>!skipFrontend</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>com.vaadin</groupId>
                        <artifactId>flow-maven-plugin</artifactId>
                        <configuration>
                            <frontendDirectory>./frontend</frontendDirectory>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
        <profile>
            <id>run-jetty</id>
            <activation>
                <property>
                    <name>!skipJetty</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.eclipse.jetty.ee10</groupId>
                        <artifactId>jetty-ee10-maven-plugin</artifactId>
                        <configuration>
                            <scan>5</scan>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

#### Main component class (`{ComponentName}.java`)

```java
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
package com.vaadin.flow.component.{package-name};

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * {Human Name} is a component for ...
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-{kebab-name}")
@NpmPackage(value = "@vaadin/{kebab-name}", version = "{npm-version}")
@JsModule("@vaadin/{kebab-name}/src/vaadin-{kebab-name}.js")
public class {ComponentName} extends Component implements HasSize {

    /**
     * Creates a new {@link {ComponentName}}.
     */
    public {ComponentName}() {
    }
}
```

Keep the component implementation minimal — just the skeleton. Do NOT implement component-specific logic.

#### Serializable test (`{ComponentName}SerializableTest.java`)

```java
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
package com.vaadin.flow.component.{package-name};

import com.vaadin.flow.testutil.ClassesSerializableTest;

class {ComponentName}SerializableTest extends ClassesSerializableTest {
}
```

#### TestBench element (`{ComponentName}Element.java`)

```java
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
package com.vaadin.flow.component.{package-name}.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-{kebab-name}&gt;</code> element.
 */
@Element("vaadin-{kebab-name}")
public class {ComponentName}Element extends TestBenchElement {
}
```

#### TestAppShell.java (integration tests)

```java
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
package com.vaadin.flow.component.app;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;

@StyleSheet(Lumo.STYLESHEET)
public class TestAppShell implements AppShellConfigurator {
}
```

#### Integration test page (`{ComponentName}Page.java`)

```java
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
package com.vaadin.flow.component.{package-name}.tests;

import com.vaadin.flow.component.{package-name}.{ComponentName};
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-{kebab-name}")
public class {ComponentName}Page extends Div {

    public {ComponentName}Page() {
        {ComponentName} component = new {ComponentName}();
        add(component);
    }
}
```

#### Integration test class (`{ComponentName}IT.java`)

```java
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
package com.vaadin.flow.component.{package-name}.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.{package-name}.testbench.{ComponentName}Element;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-{kebab-name}")
public class {ComponentName}IT extends AbstractComponentIT {

    private {ComponentName}Element component;

    @Before
    public void init() {
        open();
        component = $({ComponentName}Element.class).waitForFirst();
    }

    @Test
    public void componentIsPresent() {
        Assert.assertNotNull(component);
    }
}
```

#### vite.config.ts (integration tests)

```typescript
// @ts-ignore can not be resolved until NPM packages are installed
import { defineConfig, UserConfigFn } from 'vite';
// @ts-ignore can not be resolved until Flow generates base Vite config
import { vaadinConfig } from './vite.generated';
import { sharedConfig, mergeConfigs } from '../../shared/shared-vite-config';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
});

export default defineConfig((env) => mergeConfigs(
  vaadinConfig(env),
  sharedConfig(env),
  customConfig(env)
));
```

### 4. Register the module in the root pom.xml

Add `<module>vaadin-{kebab-name}-flow-parent</module>` to the root `pom.xml` `<modules>` section, in alphabetical order among the existing modules.

### 5. Verify the build

Run `mvn clean install -pl vaadin-{kebab-name}-flow-parent -DskipTests` to verify the module structure compiles correctly.

## Important guidelines

- Use the **exact pom.xml templates** shown above — they match the existing module conventions precisely
- The component Java class should be minimal — just the skeleton with `@Tag`, `@NpmPackage`, `@JsModule`, and a default constructor
- All license headers must use: `Copyright 2000-2026 Vaadin Ltd.`
- The `@NpmPackage` version must match the version used by other components (read it from an existing component)
- The integration tests artifact ID follows the pattern `vaadin-{kebab-name}-integration-tests` (without `-flow-` in the middle), matching existing components
- Do NOT implement any component-specific logic — this is just scaffolding
- Do NOT run integration tests — just create the files and verify the build
- Java package names use the kebab-name with hyphens removed (e.g. `datepicker`, `checkbox`, `breadcrumb`)
