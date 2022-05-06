package com.vaadin.flow.component.richtexteditor;

import java.io.Serializable;
import java.util.Objects;

/*
 * #%L
 * Vaadin Rich Text Editor for Vaadin 10
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

/**
 * Rich Text Editor is an input field for entering rich text. It allows you to
 * format and style your text using boldface, italics, headings, lists, images,
 * links etc.
 * <p>
 * The value of the rich text editor is in
 * <a href="https://github.com/quilljs/delta">Delta</a> format. The
 * {@link #setValue(String) setValue} and {@link #getValue() getValue} methods
 * deal with the default Delta format, but it is also possible to get and set
 * the value as an HTML string using
 * <code>rte.{@link #asHtml()}.{@link AsHtml#getValue() getValue()}</code>,
 * <code>rte.{@link #asHtml()}.{@link AsHtml#setValue(String) setValue()}</code>
 * and {@link #getHtmlValue()}.
 *
 * @author Vaadin Ltd
 *
 */
@Tag("vaadin-rich-text-editor")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
public class RichTextEditor
        extends GeneratedVaadinRichTextEditor<RichTextEditor, String>
        implements HasSize, HasValueChangeMode, InputNotifier, KeyNotifier,
        CompositionNotifier, HasLabel {

    private ValueChangeMode currentMode;
    private RichTextEditorI18n i18n;
    private AsHtml asHtml;
    private HtmlSetRequest htmlSetRequest;

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
                    getElement().executeJs("this.set('i18n." + key + "', $0)",
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
        super("", "", false, true);
        setValueChangeMode(ValueChangeMode.ON_CHANGE);
    }

    /**
     * Constructs a {@code RichTextEditor} with the initial value
     *
     * @param initialValue
     *            the initial value in Delta format, not {@code null}
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
     * Constructs an empty {@code RichTextEditor} with a value change listener
     * and an initial value.
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
     * Sets the value of this editor. Should be in
     * <a href="https://github.com/quilljs/delta">Delta</a> format. If the new
     * value is not equal to {@code getValue()}, fires a value change event.
     * Throws {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with the editor, as long as no new converter is defined.
     *
     * @see #asHtml()
     * @see AsHtml#setValue(String)
     * @param value
     *            the new value in Delta format, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @ClientCallable
    private void updateValue(String value) {
        setValue(value);
        if (this.asHtml != null) {
            this.asHtml.value.clear();
        }
    }

    /**
     * Returns the current value of the text editor in
     * <a href="https://github.com/quilljs/delta">Delta</a> format. By default,
     * the empty editor will return an empty string.
     *
     * @see #getHtmlValue()
     * @see #asHtml()
     * @see AsHtml#getValue()
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }

    /**
     * The value of the editor presented as an HTML string.
     * <p>
     * This represents the value currently set on the client side. If you have
     * just set the value on the server side using {@link #setValue(String)} or
     * {@link AsHtml#setValue(String)} then the value returned from this method
     * will not yet correspond to the newly set value until the next server
     * round trip.
     *
     * @see #getValue()
     * @see #asHtml()
     * @see AsHtml#getValue()
     * @return the sanitized {@code htmlValue} property from the web component
     *         or {@code null} if it is not available.
     */
    public String getHtmlValue() {
        String htmlValueString = getHtmlValueString();
        if (htmlValueString == null) {
            return null;
        }

        // Using basic whitelist and adding img tag with data protocol enabled.
        return sanitize(htmlValueString);
    }

    String sanitize(String html) {
        return org.jsoup.Jsoup.clean(html,
                org.jsoup.safety.Whitelist.basic()
                        .addTags("img", "h1", "h2", "h3", "s")
                        .addAttributes("img", "align", "alt", "height", "src",
                                "title", "width")
                        .addAttributes(":all", "style")
                        .addProtocols("img", "src", "data"));
    }

    private class HtmlSetRequest implements Serializable {
        private String html;
        private boolean pending;

        void requestUpdate(String htmlValueString) {
            this.html = htmlValueString != null ? sanitize(htmlValueString)
                    : null;
            if (!pending) {
                runBeforeClientResponse(ui -> this.execute());
                pending = true;
            }
        }

        void execute() {
            if (getValueChangeMode() != ValueChangeMode.EAGER) {
                // Add a one-time listener if we are not in eager mode.
                final String JS = "var listener = e => {"
                        + "  this.$server.updateValue(e.detail.value);"
                        + "  this.removeEventListener('value-changed', listener);"
                        + "  listener = null; };"
                        + "this.addEventListener('value-changed', listener);";
                getElement().executeJs(JS);
            }
            getElement().callJsFunction("dangerouslySetHtmlValue", this.html);
            pending = false;
        }
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
            return "[" + undo + ", " + redo + ", " + bold + ", " + italic + ", "
                    + underline + ", " + strike + ", " + h1 + ", " + h2 + ", "
                    + h3 + ", " + subscript + ", " + superscript + ", "
                    + listOrdered + ", " + listBullet + ", " + alignLeft + ", "
                    + alignCenter + ", " + alignRight + ", " + image + ", "
                    + link + ", " + blockquote + ", " + codeBlock + ", " + clean
                    + "]";
        }
    }

    /**
     * Gets an instance of {@code HasValue} for binding the html value of the
     * editor with {@code Binder}.
     *
     * @return an instance of {@code HasValue}
     */
    public HasValue<ValueChangeEvent<String>, String> asHtml() {
        if (asHtml == null) {
            asHtml = new AsHtml();
        }
        return asHtml;
    }

    /**
     * Use this rich text editor as an editor with html value in {@link Binder}.
     */
    private class AsHtml implements HasValue<ValueChangeEvent<String>, String> {

        private String oldValue;
        private final HtmlValue value;

        AsHtml() {
            this.value = new HtmlValue();
            RichTextEditor.this.addValueChangeListener(e -> this.value.clear());
        }

        /**
         * Sets the value of the editor presented as an HTML string. Also
         * updates the old value which is provided in {@code ValueChangeEvent}.
         * <p>
         * On the client side the newly set HTML snippet is interpreted by
         * <a href=
         * "https://quilljs.com/docs/modules/clipboard/#matchers">Quill's
         * Clipboard matchers</a>, which may not produce the exactly same HTML
         * that was set. The server side value will be updated to reflect the
         * new state after the round trip.
         *
         * @see RichTextEditor#setValue(String)
         * @param value
         *            the HTML string
         */
        @Override
        public void setValue(String value) {
            this.oldValue = getValue();
            this.value.setValue(value);
            setHtmlValueAsynchronously(value);
        }

        /**
         * Sets content represented by sanitized HTML string into the editor.
         * The HTML string is interpreted by
         * <a href="http://quilljs.com/docs/modules/clipboard/#matchers">Quill's
         * Clipboard matchers</a> on the client side, which may not produce the
         * exactly input HTML.
         * <p>
         * Note: The value will be set asynchronously with client-server
         * roundtrip.
         *
         * @param htmlValueString
         *            the HTML string
         */
        private void setHtmlValueAsynchronously(String htmlValueString) {
            if (htmlSetRequest == null) {
                htmlSetRequest = new HtmlSetRequest();
            }
            htmlSetRequest.requestUpdate(htmlValueString);
        }

        /**
         * Gets the value of the editor presented as an HTML string.
         * <p>
         * If you have just set the value on the server side using the
         * {@link #setValue(String) AsHtml.setValue()} method then his method
         * will give you back the exact same value until the next server round
         * trip. On the client side the newly set HTML snippet is interpreted by
         * <a href=
         * "https://quilljs.com/docs/modules/clipboard/#matchers">Quill's
         * Clipboard matchers</a>, which may not produce the exactly same HTML
         * that was set. The server side value will be updated to reflect the
         * new state after the round trip.
         *
         * @see RichTextEditor#getValue()
         * @see RichTextEditor#getHtmlValue()
         * @return the sanitized HTML string
         */
        @Override
        public String getValue() {
            return value.getValue();
        }

        /**
         * Adds a value change listener. The listener is called when the value
         * of this {@code HasValue} is changed either by the user or
         * programmatically.
         *
         * @param listener
         *            the value change listener, not null
         * @return a registration for the listener
         */
        @Override
        public Registration addValueChangeListener(
                ValueChangeListener listener) {
            return RichTextEditor.this
                    .addValueChangeListener(originalEvent -> listener
                            .valueChanged(this.createNewEvent(originalEvent)));
        }

        private ValueChangeEvent createNewEvent(
                ValueChangeEvent<String> originalEvent) {
            return new ValueChangeEvent<String>() {
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
        }

        /**
         * Sets the editor to be read only.
         *
         * @param readOnly
         *            {@code true} to make the editor read only, {@code false}
         *            to make the editor not read only
         */
        @Override
        public void setReadOnly(boolean readOnly) {
            RichTextEditor.this.setReadOnly(readOnly);
        }

        /**
         * Gets whether the editor is read only.
         *
         * @return {@code true} if the editor is read only, {@code false} if it
         *         is not read only
         */
        @Override
        public boolean isReadOnly() {
            return RichTextEditor.this.isReadOnly();
        }

        /**
         * Sets the editor's required indicator visibility.
         *
         * @param requiredIndicatorVisible
         *            {@code true} to make the indicator visible, {@code false}
         *            to hide the indicator
         */
        @Override
        public void setRequiredIndicatorVisible(
                boolean requiredIndicatorVisible) {
            RichTextEditor.this
                    .setRequiredIndicatorVisible(requiredIndicatorVisible);
        }

        /**
         * Gets whether editor's required indicator is visible.
         *
         * @return {@code true} if the required indicator is visible,
         *         {@code false} if it is hidden.
         */
        @Override
        public boolean isRequiredIndicatorVisible() {
            return RichTextEditor.this.isRequiredIndicatorVisible();
        }

        private class HtmlValue implements Serializable {
            private String value;
            private boolean present;

            private String getValue() {
                if (!present) {
                    this.value = generateHtmlValue();
                    this.present = true;
                }
                return value;
            }

            private void setValue(String value) {
                this.value = value;
                this.present = true;
            }

            private void clear() {
                this.value = null;
                this.present = false;
            }

            private String generateHtmlValue() {
                if (RichTextEditor.this.isEmpty()) {
                    return null;
                } else {
                    return RichTextEditor.this.getHtmlValue();
                }
            }
        }
    }

}
