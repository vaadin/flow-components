vaadin-board
-----
Use 8.1-SNAPSHOT version of vaadin from prereleases repository.
Add maven frontend plugin:

            <plugin>
                <groupId>com.vaadin</groupId>
                <artifactId>vaadin-frontend-maven-plugin</artifactId>
                <version>0.1-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>update</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.4</version>
                <configuration>
                    <nodeVersion>v6.9.1</nodeVersion>
                    <yarnVersion>v0.22.0</yarnVersion>
                    <workingDirectory>src/main/frontend</workingDirectory>
                    <skip>${vaadin.frontend.ok}</skip>
                </configuration>
                <executions>
                    <execution>
                        <id>install node and yarn</id>
                        <goals>
                            <goal>install-node-and-yarn</goal>
                        </goals>
                        <configuration></configuration>
                    </execution>
                    <execution>
                        <id>yarn install</id>
                        <goals>
                            <goal>yarn</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
