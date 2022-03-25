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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@PreserveOnRefresh
@Route(value = "vaadin-grid/beangridpage")
public class BeanGridPage extends Div {

    public BeanGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        
        grid.setItems(IntStream.range(0, 100).mapToObj(i -> new Person("Name " + i, 100)));

        grid.setColumns("firstName", "age");

        grid.addComponentColumn(item -> {
            TextField tf = new TextField();
            tf.setValue(item.getFirstName());
            return tf;
        });

        grid.addColumn(new RecyclingComponentRenderer<Person, TextField>(){
            @Override
            TextField createComponent() {
                return new TextField();
            }

            @Override
            void updateComponent(TextField component, Person item) {
                component.setValue(item.getFirstName());
            }
        });

        add(grid);
    }

    public abstract class RecyclingComponentRenderer<SOURCE, COMPONENT extends Component> extends LitRenderer<SOURCE>{

        private final Map<String, COMPONENT> components = new java.util.HashMap<>();
        private Element container;
        private DataKeyMapper<SOURCE> keyMapper;

        public RecyclingComponentRenderer() {
            super("<flow-component-renderer .requestedItemKey=${itemKey} appid='" + UI.getCurrent().getInternals().getAppId() + "' " +
            ".update=${(() => { " +
            "   if (root.rendererInstanceId === undefined) { " +
            // TODO: Unique id (scope under element by renderer namespace)
            "     root.rendererInstanceId = itemKey; " +
            "     rendererOwner.rendererInstances = rendererOwner.rendererInstances || {}; " +
            "     rendererOwner.rendererInstances[root.rendererInstanceId] = root; " +
            "   } " +
            "   if (root.firstElementChild && root.firstElementChild.requestedItemKey !== root.firstElementChild.updatedItemKey) { " + 
            "       root.firstElementChild.style.opacity = 0; " +
            "   } " +
            "   rendererItemUpdated(root.rendererInstanceId, item); " +
            " })() " +
            "} " +
            "></flow-component-renderer>");

            withFunction("rendererItemUpdated", (item, params) -> {
                String rendererInstanceId = params.getString(0);
                if (!components.containsKey(rendererInstanceId)) {
                    COMPONENT component = createComponent();
                    components.put(rendererInstanceId, component);
                    
                    container.appendVirtualChild(component.getElement());
                    container.executeJs("this.rendererInstances[$0].firstElementChild.nodeid = $1;", rendererInstanceId, component.getElement().getNode().getId());
                }
                updateComponent(components.get(rendererInstanceId), item);
               
                container.executeJs("const fcr = this.rendererInstances[$0].firstElementChild; if (fcr.requestedItemKey === $1) fcr.style.opacity = 1;", rendererInstanceId, keyMapper.key(item));
            });
        }

        @Override
        public Rendering<SOURCE> render(Element container, DataKeyMapper<SOURCE> keyMapper, String rendererName) {
            this.container = container;
            this.keyMapper = keyMapper;
            
            Registration detachListenerRegistration = container.addDetachListener(e -> clearComponents());

            Rendering<SOURCE> rendering = super.render(container, keyMapper, rendererName);
            
            return new Rendering<SOURCE>() {
                @Override
                public Optional<DataGenerator<SOURCE>> getDataGenerator() {
                    return rendering.getDataGenerator();
                }
    
                @Override
                public Element getTemplateElement() {
                    return null;
                }
    
                @Override
                public Registration getRegistration() {
                    List<Registration> registrations = new ArrayList<>();
                    registrations.add(rendering.getRegistration());
                    registrations.add(detachListenerRegistration);
                    registrations.add(() -> clearComponents());
                    return () -> registrations.forEach(Registration::remove);
                }
            };
        }

        private void clearComponents() {
            components.values().forEach((component) -> container.removeVirtualChild(component.getElement()));
            components.clear();
        }

        abstract COMPONENT createComponent();

        abstract void updateComponent(COMPONENT component, SOURCE item);
            
    }
}
