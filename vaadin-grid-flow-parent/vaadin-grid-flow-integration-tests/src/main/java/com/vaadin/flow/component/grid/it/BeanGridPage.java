/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/beangridpage")
public class BeanGridPage extends Div {

    public BeanGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        
        grid.setItems(IntStream.range(0, 100).mapToObj(i -> new Person("Name " + i, 100)));

        grid.setColumns("firstName", "age");

        grid.addColumn(new RecyclingComponentRenderer<Person, TextField>(){
            @Override
            TextField createComponent() {
                return new TextField();
            }

            @Override
            void updateComponent(TextField component, Person item) {
                component.setValue(item.getFirstName());
            }
        }.create());

        add(grid);
    }

    public abstract class RecyclingComponentRenderer<SOURCE, COMPONENT extends Component> extends LitRenderer<SOURCE>{

        private final String RENDERER_ID = UUID.randomUUID().toString();
        private final String APP_ID = UI.getCurrent().getInternals().getAppId();
        private final Map<String, COMPONENT> components = new java.util.HashMap<>();

        public RecyclingComponentRenderer() {
            super();
        }
        
        public Renderer<SOURCE> create() {

            String template =  "<flow-component-renderer appid='" + APP_ID + "' " +
                    ".update=${(() => { " +
                    "   if (root.rendererId === undefined) { " +
                    // TODO: Unique id
                    "     root.rendererId = itemKey; " +
                    "     window.renderers = window.renderers || {}; " +
                    "     window.renderers[root.rendererId] = root; " +
                    "   } " +
                    // TODO: debounce
                    "   rendererItemUpdated(root.rendererId, item); " +
                    " })() " +
                    "} " +
                    "></flow-component-renderer>";
                
            return LitRenderer.<SOURCE>of(template).withFunction("rendererItemUpdated", (item, params) -> {
                String rendererId = params.getString(0);
                if (!components.containsKey(rendererId)) {
                    COMPONENT component = createComponent();
                    components.put(rendererId, component);
                    
                    // TODO: The instances must be cleaned up
                    UI.getCurrent().getElement().appendVirtualChild(component.getElement());
                    UI.getCurrent().getElement().executeJs("window.renderers[$0].firstElementChild.nodeid = $1; delete window.renderers[$0];", rendererId, component.getElement().getNode().getId());
                }
                updateComponent(components.get(rendererId), item);
            });
        }

        abstract COMPONENT createComponent();

        abstract void updateComponent(COMPONENT component, SOURCE item);
            
    }
}
