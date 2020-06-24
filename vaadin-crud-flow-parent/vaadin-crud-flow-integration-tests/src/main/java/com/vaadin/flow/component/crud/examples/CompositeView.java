package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.crud.examples.CompositeView.Localization.Country;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route
@Theme(Lumo.class)
public class CompositeView extends Div {

    public CompositeView() {
        final CustomField language = new CustomField("Language");
        language.getElement().setAttribute("editor-role", "language");

        final ComboBox<Country> countries = new ComboBox<>("Country", Country.values());

        final FormLayout form = new FormLayout(language, countries);

        final Binder<Localization> binder = new Binder<>(Localization.class);
        binder.bind(language, Localization::getLanguage, Localization::setLanguage);
        binder.bind(countries, Localization::getCountry, Localization::setCountry);

        final BinderCrudEditor<Localization> editor = new BinderCrudEditor<>(binder, form);
        final Crud<Localization> crud = new Crud<>(Localization.class, editor);

        language.addValueChangeListener(e -> crud.setDirty(true));

        final CrudI18n i18n = CrudI18n.createDefault();
        i18n.setNewItem("New Locale");
        i18n.setEditItem("Edit Locale");

        crud.setI18n(i18n);

        add(crud);
    }

    public static class Localization {

        private String language;
        private Country country;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Country getCountry() {
            return country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }

        public enum Country {

            FINLAND,
            CANADA,
            BRAZIL,
            NIGERIA;

            @Override
            public String toString() {
                return name().charAt(0) + name().substring(1).toLowerCase();
            }
        }
    }

    public static class CustomField extends AbstractCompositeField<Div, CustomField, String> {

        private final Span valueSpan = new Span();
        private final Div labelSpan = new Div();

        public CustomField() {
            super(null);

            final Button change = new Button(VaadinIcon.PENCIL.create(), event -> {
                final Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("editor-role", "composite-dialog");

                final TextField languageField = new TextField();
                languageField.getElement().setAttribute("editor-role", "language-field");
                languageField.setValueChangeMode(ValueChangeMode.EAGER);
                languageField.addValueChangeListener(e -> setValue(e.getValue()));

                final Button close = new Button("Close", e -> dialog.close());

                dialog.add(languageField, close);
                dialog.open();
            });
            change.getElement().setAttribute("editor-role", "language-confirm");

            labelSpan.setWidth("100%");
            getContent().add(labelSpan, valueSpan, change);
        }

        public CustomField(String label) {
            this();
            labelSpan.setText(label);
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            valueSpan.setText(newPresentationValue);
        }
    }
}
