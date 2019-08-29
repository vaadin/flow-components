package com.vaadin.flow.component.richtexteditor;

/*
 * #%L
 * Vaadin Rich Text Editor for Vaadin 10
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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import javax.xml.bind.ValidationEvent;
import java.util.Objects;

import java.io.Serializable;

/**
 * Server-side component for the {@code <vaadin-rich-text-editor>} component.
 *
 * @author Vaadin Ltd
 *
 */
@Tag("vaadin-rich-text-editor")
@HtmlImport("frontend://bower_components/vaadin-rich-text-editor/src/vaadin-rich-text-editor.html")
public class RichTextEditor extends GeneratedVaadinRichTextEditor<RichTextEditor, String>
        implements HasSize, HasValueChangeMode, InputNotifier, KeyNotifier, CompositionNotifier {

    private ValueChangeMode currentMode;
    private RichTextEditorI18n i18n;

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link RichTextEditor#setI18n(RichTextEditorI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public RichTextEditorI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(RichTextEditorI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;
        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                JsonObject i18nObject = (JsonObject) JsonSerializer
                        .toJson(this.i18n);
                for (String key : i18nObject.keys()) {
                    ui.getPage().executeJavaScript(
                            "$0.set('i18n." + key + "', $1)", getElement(),
                            i18nObject.get(key));
                }
            }
        });
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Constructs an empty {@code RichTextEditor}.
     */
    public RichTextEditor() {
        super("", "", false);
        setValueChangeMode(ValueChangeMode.ON_CHANGE);
    }

    /**
     * Constructs a {@code RichTextEditor} with the initial value
     *
     * @param initialValue
     *            the initial value
     *
     * @see #setValue(Object)
     */
    public RichTextEditor(String initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Constructs an empty {@code RichTextEditor} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public RichTextEditor(
            ValueChangeListener<? super ComponentValueChangeEvent<RichTextEditor, String>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code RichTextEditor} with a value change
     * listener and an initial value.
     *
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     *
     * @see #setValue(Object)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public RichTextEditor(String initialValue,
                          ValueChangeListener<? super ComponentValueChangeEvent<RichTextEditor, String>> listener) {
        this();
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "value-changed"));
    }

    /**
     * Sets the value of this editor. Should be in <a href="https://github.com/quilljs/delta">Delta</a> format.
     * If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with the editor, as long as no new converter is defined.
     *
     * @param value
     *            the new value in Delta format, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Sets content represented by sanitized HTML string into the editor.
     * The HTML string is interpreted by
     * <a href="http://quilljs.com/docs/modules/clipboard/#matchers">Quill's Clipboard matchers</a>
     * on the client side, which may not produce the exactly input HTML.
     * <p>
     * Note: The value will be set asynchronously with client-server roundtrip.
     *
     * @param htmlValueString
     *            the HTML string
     */
    private PendingJavaScriptResult setHtmlValueAsynchronously(String htmlValueString) {
        if (htmlValueString != null) {
            htmlValueString = sanitize(htmlValueString);
        }
        return getElement().callJsFunction("dangerouslySetHtmlValue", htmlValueString);
    }

    @ClientCallable
    private void updateValue(String value) {
        setValue(value);
    }

    /**
     * Returns the current value of the text editor in <a href="https://github.com/quilljs/delta">Delta</a> format. By default, the empty
     * editor will return an empty string.
     *
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }


    /**
     * Value of the editor presented as HTML string.
     *
     * @return the sanitized {@code htmlValue} property from the webcomponent.
     */
    public String getHtmlValue() {
        // Using basic whitelist and adding img tag with data protocol enabled.
        return sanitize(getHtmlValueString());
    }

    String sanitize(String html) {
        return org.jsoup.Jsoup.clean(html,
                org.jsoup.safety.Whitelist.basic()
                        .addTags("img", "h1", "h2", "h3", "s")
                        .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                        .addAttributes(":all", "style")
                        .addProtocols("img", "src", "data"));
    }

    /**
     * The internationalization properties for {@link RichTextEditor}.
     */
    public static class RichTextEditorI18n implements Serializable {
        private String undo;
        private String redo;
        private String bold;
        private String italic;
        private String underline;
        private String strike;
        private String h1;
        private String h2;
        private String h3;
        private String subscript;
        private String superscript;
        private String listOrdered;
        private String listBullet;
        private String alignLeft;
        private String alignCenter;
        private String alignRight;
        private String image;
        private String link;
        private String blockquote;
        private String codeBlock;
        private String clean;

        /**
         * Gets the translated word for {@code undo}
         *
         * @return the translated word for undo
         */
        public String getUndo() {
            return undo;
        }

        /**
         * Sets the translated word for {@code undo}.
         *
         * @param undo
         *            the translated word for undo
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setUndo(String undo) {
            this.undo = undo;
            return this;
        }

        /**
         * Gets the translated word for {@code redo}
         *
         * @return the translated word for redo
         */
        public String getRedo() {
            return redo;
        }

        /**
         * Sets the translated word for {@code redo}.
         *
         * @param redo
         *            the translated word for redo
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setRedo(String redo) {
            this.redo = redo;
            return this;
        }

        /**
         * Gets the translated word for {@code bold}
         *
         * @return the translated word for bold
         */
        public String getBold() {
            return bold;
        }

        /**
         * Sets the translated word for {@code bold}.
         *
         * @param bold
         *            the translated word for bold
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setBold(String bold) {
            this.bold = bold;
            return this;
        }

        /**
         * Gets the translated word for {@code italic}
         *
         * @return the translated word for italic
         */
        public String getItalic() {
            return italic;
        }

        /**
         * Sets the translated word for {@code italic}.
         *
         * @param italic
         *            the translated word for italic
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setItalic(String italic) {
            this.italic = italic;
            return this;
        }

        /**
         * Gets the translated word for {@code underline}
         *
         * @return the translated word for underline
         */
        public String getUnderline() {
            return underline;
        }

        /**
         * Sets the translated word for {@code underline}.
         *
         * @param underline
         *            the translated word for underline
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setUnderline(String underline) {
            this.underline = underline;
            return this;
        }

        /**
         * Gets the translated word for {@code strike}
         *
         * @return the translated word for strike
         */
        public String getStrike() {
            return strike;
        }

        /**
         * Sets the translated word for {@code strike}.
         *
         * @param strike
         *            the translated word for strike
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setStrike(String strike) {
            this.strike = strike;
            return this;
        }

        /**
         * Gets the translated word for {@code h1}
         *
         * @return the translated word for h1
         */
        public String getH1() {
            return h1;
        }

        /**
         * Sets the translated word for {@code h1}.
         *
         * @param h1
         *            the translated word for h1
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH1(String h1) {
            this.h1 = h1;
            return this;
        }

        /**
         * Gets the translated word for {@code h2}
         *
         * @return the translated word for h2
         */
        public String getH2() {
            return h2;
        }

        /**
         * Sets the translated word for {@code h2}.
         *
         * @param h2
         *            the translated word for h2
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH2(String h2) {
            this.h2 = h2;
            return this;
        }

        /**
         * Gets the translated word for {@code h3}
         *
         * @return the translated word for h3
         */
        public String getH3() {
            return h3;
        }

        /**
         * Sets the translated word for {@code h3}.
         *
         * @param h3
         *            the translated word for h3
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH3(String h3) {
            this.h3 = h3;
            return this;
        }

        /**
         * Gets the translated word for {@code subscript}
         *
         * @return the translated word for subscript
         */
        public String getSubscript() {
            return subscript;
        }

        /**
         * Sets the translated word for {@code subscript}.
         *
         * @param subscript
         *            the translated word for subscript
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setSubscript(String subscript) {
            this.subscript = subscript;
            return this;
        }

        /**
         * Gets the translated word for {@code superscript}
         *
         * @return the translated word for superscript
         */
        public String getSuperscript() {
            return superscript;
        }

        /**
         * Sets the translated word for {@code superscript}.
         *
         * @param superscript
         *            the translated word for superscript
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setSuperscript(String superscript) {
            this.superscript = superscript;
            return this;
        }

        /**
         * Gets the translated word for {@code listOrdered}
         *
         * @return the translated word for listOrdered
         */
        public String getListOrdered() {
            return listOrdered;
        }

        /**
         * Sets the translated word for {@code listOrdered}.
         *
         * @param listOrdered
         *            the translated word for listOrdered
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setListOrdered(String listOrdered) {
            this.listOrdered = listOrdered;
            return this;
        }

        /**
         * Gets the translated word for {@code listBullet}
         *
         * @return the translated word for listBullet
         */
        public String getListBullet() {
            return listBullet;
        }

        /**
         * Sets the translated word for {@code listBullet}.
         *
         * @param listBullet
         *            the translated word for listBullet
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setListBullet(String listBullet) {
            this.listBullet = listBullet;
            return this;
        }

        /**
         * Gets the translated word for {@code alignLeft}
         *
         * @return the translated word for alignLeft
         */
        public String getAlignLeft() {
            return alignLeft;
        }

        /**
         * Sets the translated word for {@code alignLeft}.
         *
         * @param alignLeft
         *            the translated word for alignLeft
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignLeft(String alignLeft) {
            this.alignLeft = alignLeft;
            return this;
        }

        /**
         * Gets the translated word for {@code alignCenter}
         *
         * @return the translated word for alignCenter
         */
        public String getAlignCenter() {
            return alignCenter;
        }

        /**
         * Sets the translated word for {@code alignCenter}.
         *
         * @param alignCenter
         *            the translated word for alignCenter
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignCenter(String alignCenter) {
            this.alignCenter = alignCenter;
            return this;
        }

        /**
         * Gets the translated word for {@code alignRight}
         *
         * @return the translated word for alignRight
         */
        public String getAlignRight() {
            return alignRight;
        }

        /**
         * Sets the translated word for {@code alignRight}.
         *
         * @param alignRight
         *            the translated word for alignRight
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignRight(String alignRight) {
            this.alignRight = alignRight;
            return this;
        }

        /**
         * Gets the translated word for {@code image}
         *
         * @return the translated word for image
         */
        public String getImage() {
            return image;
        }

        /**
         * Sets the translated word for {@code image}.
         *
         * @param image
         *            the translated word for image
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setImage(String image) {
            this.image = image;
            return this;
        }

        /**
         * Gets the translated word for {@code link}
         *
         * @return the translated word for link
         */
        public String getLink() {
            return link;
        }

        /**
         * Sets the translated word for {@code link}.
         *
         * @param link
         *            the translated word for link
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setLink(String link) {
            this.link = link;
            return this;
        }

        /**
         * Gets the translated word for {@code blockquote}
         *
         * @return the translated word for blockquote
         */
        public String getBlockquote() {
            return blockquote;
        }

        /**
         * Sets the translated word for {@code blockquote}.
         *
         * @param blockquote
         *            the translated word for blockquote
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setBlockquote(String blockquote) {
            this.blockquote = blockquote;
            return this;
        }

        /**
         * Gets the translated word for {@code codeBlock}
         *
         * @return the translated word for codeBlock
         */
        public String getCodeBlock() {
            return codeBlock;
        }

        /**
         * Sets the translated word for {@code codeBlock}.
         *
         * @param codeBlock
         *            the translated word for codeBlock
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setCodeBlock(String codeBlock) {
            this.codeBlock = codeBlock;
            return this;
        }

        /**
         * Gets the translated word for {@code clean}
         *
         * @return the translated word for clean
         */
        public String getClean() {
            return clean;
        }

        /**
         * Sets the translated word for {@code clean}.
         *
         * @param clean
         *            the translated word for clean
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setClean(String clean) {
            this.clean = clean;
            return this;
        }

        /**
         * Gets the stringified values of the tooltips.
         *
         * @return stringified values of the tooltips
         */
        @Override
        public String toString() {
            return  "[" +
                    undo + ", " +
                    redo + ", " +
                    bold + ", " +
                    italic + ", " +
                    underline + ", " +
                    strike + ", " +
                    h1 + ", " +
                    h2 + ", " +
                    h3 + ", " +
                    subscript + ", " +
                    superscript + ", " +
                    listOrdered + ", " +
                    listBullet + ", " +
                    alignLeft + ", " +
                    alignCenter + ", " +
                    alignRight + ", " +
                    image + ", " +
                    link + ", " +
                    blockquote + ", " +
                    codeBlock + ", " +
                    clean + "]";
        }
    }

    /**
     * Gets an instance of {@code HasValue} for binding the
     * html value of the editor with {@code Binder}.
     *
     * @return an instance of {@code HasValue}
     */
    public HasValue<ValueChangeEvent<String>, String> asHtml() {
        return new AsHtml(this);
    }

    /**
     * Use this rich text editor as an editor with html value in {@link Binder}.
     */
    private class AsHtml implements HasValue<ValueChangeEvent<String>, String> {

        private String oldValue;
        private String value;
        private RichTextEditor rte;

        AsHtml(RichTextEditor rte) {
            this.rte = rte;
            rte.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                   setValue(getHtmlValue(), false);
                }
            });
        }

        /**
         * Sets the value of the editor presented as HTML string.
         * Also updates the old value which is provided in
         * {@code ValueChangeEvent}.
         *
         * @param value
         *            the HTML string
         */
        @Override
        public void setValue(String value) {
            this.oldValue = getValue();
            this.value = value;
            setHtmlValueAsynchronously(value).then(result -> {
                if (oldValue != null && !oldValue.equals(value)) {
                    fireEvent(createValueChange(oldValue, false));
                }
            });
        }

        private void setValue(String value, boolean fireEvent) {
            if (fireEvent) {
                setValue(value);
            } else {
                this.oldValue = getValue();
                this.value = value;
            }
        }

        private ComponentValueChangeEvent<RichTextEditor, String> createValueChange(String oldValue,
                                                                  boolean fromClient) {
            return new ComponentValueChangeEvent<>(rte, this, oldValue,
                    fromClient);
        }

        /**
         * Gets the value of the editor presented as HTML string.
         *
         * @return the sanitized HTML string
         */
        @Override
        public String getValue() {
            return value;
        }

        /**
         * Adds a value change listener. The listener is called when the value of
         * this {@code HasValue} is changed either by the user or programmatically.
         *
         * @param listener
         *            the value change listener, not null
         * @return a registration for the listener
         */
        @Override
        public Registration addValueChangeListener(ValueChangeListener listener) {
            return
                    rte.addValueChangeListener(originalEvent -> {
                        ValueChangeEvent event = new ValueChangeEvent<String>() {
                            @Override
                            public HasValue<ValueChangeEvent<String>, String> getHasValue() {
                                return AsHtml.this;
                            }

                            @Override
                            public boolean isFromClient() {
                                return originalEvent.isFromClient();
                            }

                            @Override
                            public String getOldValue() {
                                return oldValue;
                            }

                            @Override
                            public String getValue() {
                                return AsHtml.this.getValue();
                            }
                        };
                        listener.valueChanged(event);
                    });
        }

        /**
         * Sets the editor to be read only.
         *
         * @param readOnly
         *            {@code true} to make the editor read only,
         *            {@code false} to make the editor not read only
         */
        @Override
        public void setReadOnly(boolean readOnly) {
            rte.setReadOnly(readOnly);
        }

        /**
         * Gets whether the editor is read only.
         *
         * @return {@code true} if the editor is read only,
         *         {@code false} if it is not read only
         */
        @Override
        public boolean isReadOnly() {
            return rte.isReadOnly();
        }

        /**
         * Sets the editor's required indicator visibility.
         *
         * @param requiredIndicatorVisible
         *            {@code true} to make the indicator visible,
         *            {@code false} to hide the indicator
         */
        @Override
        public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
            rte.setRequiredIndicatorVisible(requiredIndicatorVisible);
        }

        /**
         * Gets whether editor's required indicator is visible.
         *
         * @return {@code true} if the required indicator is visible,
         *         {@code false} if it is hidden.
         */
        @Override
        public boolean isRequiredIndicatorVisible() {
            return rte.isRequiredIndicatorVisible();
        }
    }
}
