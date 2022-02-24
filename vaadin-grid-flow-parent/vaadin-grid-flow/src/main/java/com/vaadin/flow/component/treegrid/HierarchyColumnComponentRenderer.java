package com.vaadin.flow.component.treegrid;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.renderer.ComponentDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;

import elemental.json.JsonObject;

/**
 * Renders components as hierarchy column for tree grid. Basically puts
 * <code>flow-component-renderer</code> tag inside of
 * <code>vaadin-grid-tree-toggle</code>
 *
 * @param <COMPONENT>
 *            the type of the output component
 * @param <SOURCE>
 *            the type of the input model object
 */
public class HierarchyColumnComponentRenderer<COMPONENT extends Component, SOURCE>
        extends ComponentRenderer<COMPONENT, SOURCE> {

    public HierarchyColumnComponentRenderer(
            ValueProvider<SOURCE, COMPONENT> componentProvider) {
        super(componentProvider);
    }

    public HierarchyColumnComponentRenderer<COMPONENT, SOURCE> withProperty(
            String property, ValueProvider<SOURCE, ?> provider) {
        setProperty(property, provider);
        return this;
    }

    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, Element contentTemplate) {

        ComponentRendering rendering = new ComponentRendering(
                keyMapper == null ? null : keyMapper::key);
        rendering.setTemplateElement(contentTemplate);

        container.getNode().runWhenAttached(ui -> setupTemplateWhenAttached(ui,
                container, rendering, keyMapper));
        return rendering;
    }

    private void setupTemplateWhenAttached(UI ui, Element owner,
            ComponentRendering rendering, DataKeyMapper<SOURCE> keyMapper) {
        String appId = ui.getInternals().getAppId();
        Element templateElement = rendering.getTemplateElement();
        owner.appendChild(templateElement);

        Element container = new Element("div");
        owner.appendVirtualChild(container);
        rendering.setContainer(container);
        String templateInnerHtml;

        if (keyMapper != null) {
            String nodeIdPropertyName = "_renderer_"
                    + templateElement.getNode().getId();

            templateInnerHtml = String.format(
                    "<flow-component-renderer appid=\"%s\" nodeid=\"[[item.%s]]\"></flow-component-renderer>",
                    appId, nodeIdPropertyName);
            rendering.setNodeIdPropertyName(nodeIdPropertyName);
        } else {
            COMPONENT component = createComponent(null);
            if (component != null) {
                container.appendChild(component.getElement());

                templateInnerHtml = String.format(
                        "<flow-component-renderer appid=\"%s\" nodeid=\"%s\"></flow-component-renderer>",
                        appId, component.getElement().getNode().getId());
            } else {
                templateInnerHtml = "";
            }
        }
        templateInnerHtml = "<vaadin-grid-tree-toggle class$='[[item.cssClassName]]' leaf='[[!item.children]]' expanded='{{expanded}}' level='[[level]]'>"
                + templateInnerHtml + "</vaadin-grid-tree-toggle>";
        templateElement.setProperty("innerHTML", templateInnerHtml);
    }

    private class ComponentRendering extends ComponentDataGenerator<SOURCE>
            implements Rendering<SOURCE> {

        private Element templateElement;

        public ComponentRendering(ValueProvider<SOURCE, String> keyMapper) {
            super(HierarchyColumnComponentRenderer.this, keyMapper);
        }

        public void setTemplateElement(Element templateElement) {
            this.templateElement = templateElement;
        }

        @Override
        public Element getTemplateElement() {
            return templateElement;
        }

        @Override
        public Optional<DataGenerator<SOURCE>> getDataGenerator() {
            return Optional.of(this);
        }

        @Override
        public void generateData(SOURCE item, JsonObject jsonObject) {
            super.generateData(item, jsonObject);
            // in order to add item.children property
            getValueProviders().forEach((key, provider) -> jsonObject.put(key,
                    JsonSerializer.toJson(provider.apply(item))));
        }
    }
}
