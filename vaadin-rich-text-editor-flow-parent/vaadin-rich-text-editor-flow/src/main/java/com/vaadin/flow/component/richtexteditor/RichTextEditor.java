/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Document;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Rich Text Editor is an input field for entering rich text. It allows you to
 * format and style your text using boldface, italics, headings, lists, images,
 * links etc.
 * <p>
 * The value of the rich text editor is in the HTML format. The
 * {@link #setValue(String) setValue} and {@link #getValue() getValue} methods
 * use the HTML format by default.
 * <p>
 * To get and set the value in the
 * <a href="https://github.com/quilljs/delta">Quill Delta</a> format, use
 * {@link #asDelta()}, {@link AsDelta#getValue()} and
 * {@link AsDelta#setValue(String)}.
 *
 * @author Vaadin Ltd
 *
 */
@Tag("vaadin-rich-text-editor")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/rich-text-editor", version = "24.8.0-alpha18")
@JsModule("@vaadin/rich-text-editor/src/vaadin-rich-text-editor.js")
public class RichTextEditor
        extends AbstractSinglePropertyField<RichTextEditor, String>
        implements CompositionNotifier, InputNotifier, KeyNotifier, HasSize,
        HasStyle, HasValueChangeMode, HasThemeVariant<RichTextEditorVariant> {

    private ValueChangeMode currentMode;
    private RichTextEditorI18n i18n;
    private AsHtml asHtml;
    private AsDelta asDelta;

    private boolean pendingPresentationUpdate = false;

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(RichTextEditorI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public RichTextEditorI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(RichTextEditorI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
    }

    private void setI18nWithJS() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(this.i18n);

        // Remove properties with null values to prevent errors in web
        // component
        removeNullValuesFromJsonObject(i18nJson);

        // Assign new I18N object to WC, by merging the existing
        // WC I18N, and the values from the new RichTextEditorI18n instance,
        // into an empty object
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);",
                i18nJson);
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Constructs an empty {@code RichTextEditor}.
     */
    public RichTextEditor() {
        super("htmlValue", "", String.class,
                RichTextEditor::presentationToModel,
                RichTextEditor::modelToPresentation);

        setPresentationValue("");

        setValueChangeMode(ValueChangeMode.ON_CHANGE);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // htmlValue property is not writeable and will not be automatically
        // initialized on the client-side element. Instead, call set
        // presentation value to run the necessary JS for initializing the
        // client-side element
        setPresentationValue(getValue());

        // Element state is not persisted across attach/detach
        if (this.i18n != null) {
            setI18nWithJS();
        }
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
        setSynchronizedEvent(ValueChangeMode.eventForMode(valueChangeMode,
                "html-value-changed"));
    }

    /**
     * Sets the value of this editor in HTML format. If the new value is not
     * equal to {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with the editor, as long as no new converter is defined.
     * <p>
     * Since v24, this method only accepts values in the HTML format, whereas in
     * v23 and earlier this method would accept values in the Delta format. In
     * order to prevent data corruption, passing a value that starts with either
     * <code>[</code> or <code>{</code> will now throw an
     * {@link IllegalArgumentException}, as it might indicate that the value is
     * in the Delta format. In order to keep using the Delta format, use
     * {@link #asDelta()}, which allows setting, retrieving, and binding the
     * value using Binder, in the Delta format. In order to pass an HTML value
     * starting with either characters, either wrap the value in a valid HTML
     * tag, such as <code>&lt;p&gt;</code>, or use {@link #asHtml()} which does
     * not include this check.
     *
     * @see #asDelta()
     * @see AsDelta#setValue(String)
     * @param value
     *            the new value in HTML format, not {@code null}
     */
    @Override
    public void setValue(String value) {
        doSetValue(value, true);
    }

    private void doSetValue(String value, boolean withDeltaCheck) {
        Objects.requireNonNull(value, "Null value is not supported");
        if (withDeltaCheck) {
            checkForDeltaValue(value);
        }
        super.setValue(value);
    }

    private void checkForDeltaValue(String value) {
        value = value.trim();
        if (value.startsWith("[") || value.startsWith("{")) {
            throw new IllegalArgumentException(
                    "The value starts with either '[' or '{' which indicates that this might be a value in the Delta format. "
                            + "Since v24, RichTextEditor.setValue only accepts values in the HTML format. "
                            + "Please check the JavaDoc for RichTextEditor.setValue for more information.");
        }
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        String presentationValue = modelToPresentation(newPresentationValue);
        getElement().setProperty("htmlValue", presentationValue);
        // htmlValue property is not writeable, HTML value needs to be set using
        // method exposed by web component instead
        if (!pendingPresentationUpdate) {
            pendingPresentationUpdate = true;
            runBeforeClientResponse(ui -> {
                getElement().callJsFunction("dangerouslySetHtmlValue",
                        getElement().getProperty("htmlValue"));
                pendingPresentationUpdate = false;
            });
        }
    }

    private static String presentationToModel(String htmlValue) {
        // Sanitize HTML coming from client
        return sanitize(htmlValue);
    }

    private static String modelToPresentation(String htmlValue) {
        // Sanitize HTML sent to client
        return sanitize(htmlValue);
    }

    /**
     * Returns whether the value is considered to be empty.
     * <p>
     * As the editor's HTML value always contains a minimal markup, this does
     * not check if the value is an empty string. Instead, this method considers
     * the value to not be empty if the user has added some content, which can
     * be:
     * <ul>
     * <li>Text, whitespaces or line breaks</li>
     * <li>An image</li>
     * </ul>
     * <p>
     * Note that a single empty HTML tag, such as a heading, blockquote, etc.,
     * is not considered as content.
     *
     * @return {@code true} if considered empty; {@code false} if not
     */
    @Override
    public boolean isEmpty() {
        Document document = org.jsoup.Jsoup.parse(getValue());

        // Get non-normalized text including spaces and newlines
        // Note that <br>s count as newlines
        String text = document.body().wholeText();

        // Remove first newline occurrence as Quill editor adds a single <br> in
        // every element even without the user having typed anything
        text = text.replaceFirst("\n", "");

        boolean hasText = !text.isEmpty();
        boolean hasImages = document.selectFirst("img") != null;

        return !hasText && !hasImages;
    }

    /**
     * Returns the current value of the text editor in HTML format. By default,
     * the empty editor will return an empty string.
     *
     * @see #asDelta()
     * @see AsDelta#getValue()
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }

    /**
     * The value of the editor in HTML format.
     *
     * @see #getValue()
     * @return the editor value in HTML format
     * @deprecated since v24 the RichTextEditor uses the HTML value by default.
     *             Use {@link #getValue()} instead.
     */
    @Deprecated
    public String getHtmlValue() {
        return getValue();
    }

    /**
     * The value of the editor in Delta format.
     * <p>
     * This property only exists to force synchronization of the {@code value}
     * property.
     *
     * @return the value of the editor in Delta format
     */
    @Synchronize(property = "value", value = "value-changed")
    private String getDeltaValue() {
        return getElement().getProperty("value");
    }

    /**
     * Gets an unmodifiable list of colors in HEX format used by the text color
     * picker and background color picker controls of the text editor.
     * <p>
     * Returns {@code null} by default, which means the web component shows a
     * default color palette.
     *
     * @since 24.5
     * @return an unmodifiable list of colors options
     */
    public List<String> getColorOptions() {
        List options = JsonSerializer.toObjects(String.class,
                (JsonArray) getElement().getPropertyRaw("colorOptions"));
        return Collections.unmodifiableList(options);
    }

    /**
     * Sets the list of colors in HEX format to use by the text color picker and
     * background color picker controls of the text editor.
     *
     * @since 24.5
     * @param colorOptions
     *            the list of colors to set, not null
     */
    public void setColorOptions(List<String> colorOptions) {
        Objects.requireNonNull(colorOptions, "Color options must not be null");
        getElement().setPropertyJson("colorOptions",
                JsonSerializer.toJson(colorOptions));
    }

    static String sanitize(String html) {
        var settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.prettyPrint(false);
        var safeHtml = org.jsoup.Jsoup.clean(html, "",
                org.jsoup.safety.Safelist.basic()
                        .addTags("img", "h1", "h2", "h3", "s")
                        .addAttributes("img", "align", "alt", "height", "src",
                                "title", "width")
                        .addAttributes(":all", "style")
                        .addProtocols("img", "src", "data"),
                settings);
        return safeHtml;
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
        private String color;
        private String background;
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
         * Gets the translated word for {@code color}
         *
         * @return the translated word for color
         */
        public String getColor() {
            return color;
        }

        /**
         * Sets the translated word for {@code color}.
         *
         * @param color
         *            the translated word for color
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setColor(String color) {
            this.color = color;
            return this;
        }

        /**
         * Gets the translated word for {@code background}
         *
         * @return the translated word for background
         */
        public String getBackground() {
            return background;
        }

        /**
         * Sets the translated word for {@code background}.
         *
         * @param background
         *            the translated word for background
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setBackground(String background) {
            this.background = background;
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
                    + underline + ", " + strike + ", " + color + ", "
                    + background + ", " + h1 + ", " + h2 + ", " + h3 + ", "
                    + subscript + ", " + superscript + ", " + listOrdered + ", "
                    + listBullet + ", " + alignLeft + ", " + alignCenter + ", "
                    + alignRight + ", " + image + ", " + link + ", "
                    + blockquote + ", " + codeBlock + ", " + clean + "]";
        }
    }

    /**
     * Gets an instance of {@code HasValue} for the editor in the HTML format.
     * Can be used for binding the value with {@link Binder}.
     * <p>
     * Note that since v24, the RichTextEditor uses the HTML value by default.
     * Instead of using this wrapper, {@link #getValue()} and
     * {@link #setValue(String)} can be used directly, and
     * {@link RichTextEditor} can be used for binding the HTML value using
     * Binder. This method is not intended to be deprecated as it keeps the
     * legacy behavior that allows passing values starting with either
     * <code>[</code> or <code>{</code>, which is not allowed when using
     * {@link #setValue(String)}.
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
     * Gets an instance of {@code HasValue} for the editor in the
     * <a href="https://github.com/quilljs/delta">Quill Delta</a> format. Can be
     * used for binding the value with {@link Binder}.
     *
     * @return an instance of {@code HasValue}
     */
    public HasValue<ValueChangeEvent<String>, String> asDelta() {
        if (asDelta == null) {
            asDelta = new AsDelta();
        }
        return asDelta;
    }

    /**
     * Use this rich text editor as an editor with html value in {@link Binder}.
     */
    private class AsHtml implements HasValue<ValueChangeEvent<String>, String> {
        /**
         * Sets the value of the editor in HTML format.
         *
         * @see RichTextEditor#setValue(String)
         * @param value
         *            the HTML string
         */
        @Override
        public void setValue(String value) {
            RichTextEditor.this.doSetValue(value, false);
        }

        /**
         * Gets the value of the editor in HTML format.
         *
         * @see RichTextEditor#getValue()
         * @return the current editor value in HTML
         */
        @Override
        public String getValue() {
            return RichTextEditor.this.getValue();
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
            return RichTextEditor.this.addValueChangeListener(listener);
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

        @Override
        public String getEmptyValue() {
            return "";
        }

        /**
         * Returns whether the value is considered to be empty.
         * <p>
         * As the editor's HTML value always contains a minimal markup, this
         * does not check if the value is an empty string. Instead, this method
         * considers the value to not be empty if the user has added some
         * content, which can be:
         * <ul>
         * <li>Text, whitespaces or line breaks</li>
         * <li>An image</li>
         * </ul>
         * <p>
         * Note that a single empty HTML tag, such as a heading, blockquote,
         * etc., is not considered as content.
         *
         * @return {@code true} if considered empty; {@code false} if not
         */
        @Override
        public boolean isEmpty() {
            return RichTextEditor.this.isEmpty();
        }
    }

    private class AsDelta
            implements HasValue<ValueChangeEvent<String>, String> {

        private static class DeltaValueChangeEvent
                extends ComponentEvent<RichTextEditor>
                implements ValueChangeEvent<String> {

            private final HasValue<?, String> hasValue;
            private final String oldValue;

            public DeltaValueChangeEvent(RichTextEditor source,
                    HasValue<?, String> hasValue, String oldValue,
                    boolean fromClient) {
                super(source, fromClient);
                this.hasValue = hasValue;
                this.oldValue = oldValue;
            }

            @Override
            public HasValue<?, String> getHasValue() {
                return hasValue;
            }

            @Override
            public boolean isFromClient() {
                return super.isFromClient();
            }

            @Override
            public String getOldValue() {
                return oldValue;
            }

            @Override
            public String getValue() {
                return hasValue.getValue();
            }
        }

        private boolean isHtmlValueSync;
        private Registration deltaValueSyncRegistration;
        private String oldValue = "";

        public AsDelta() {
            // Initialize empty value
            RichTextEditor.this.getElement().setProperty("value", "");

            // Fire delta value change event when HTML value changes
            RichTextEditor.this.addValueChangeListener(event -> {
                // If the component's value changes due to syncing the HTML
                // value back from the client, after setting the delta value on
                // the server, then we don't need to do anything.
                // We already have an up-to-date delta value, and have already
                // dispatched a server-side delta value change event.
                if (isHtmlValueSync) {
                    return;
                }
                // When the HTML value is set from server-side, then do not
                // dispatch the delta value changed event immediately, as we
                // don't have an updated delta value yet.
                // Instead, wait for delta value to sync back from client and
                // dispatch delta value change event afterwards
                if (!event.isFromClient()) {
                    // Clear previous registration in case it is still active
                    if (deltaValueSyncRegistration != null) {
                        deltaValueSyncRegistration.remove();
                    }
                    // Listen for delta value property to update, then fire
                    // delta value change event as coming from server (as the
                    // original HTML value change event did)
                    PropertyChangeListener valueChangeListener = syncEvent -> {
                        // Sanity check: We are expecting a property change
                        // event from the client here
                        if (syncEvent.isUserOriginated()) {
                            deltaValueSyncRegistration.remove();
                            fireChangeEvent(false);
                            // Update old value after all change
                            // listeners have been processed
                            oldValue = getValue();
                        }
                    };
                    deltaValueSyncRegistration = RichTextEditor.this
                            .getElement().addPropertyChangeListener("value",
                                    "value-changed", valueChangeListener);
                } else {
                    // If the HTML value change event comes from the client,
                    // then we can immediately dispatch the delta value change
                    // event as well, as both properties have been synced to the
                    // server in the same roundtrip.
                    // See the synchronized `getDeltaValue` property on the
                    // component.
                    fireChangeEvent(event.isFromClient());
                    // Update old value after all change listeners have been
                    // processed
                    oldValue = getValue();
                }
            });
        }

        /**
         * Sets the value of this editor in the
         * <a href="https://github.com/quilljs/delta">Quill Delta</a> format. If
         * the new value is not equal to {@code getValue()}, fires a value
         * change event. Throws {@code NullPointerException}, if the value is
         * null.
         * <p>
         * Note: {@link Binder} will take care of the {@code null} conversion
         * when integrates with the editor, as long as no new converter is
         * defined.
         *
         * @param value
         *            the new value in Delta format, not {@code null}
         */
        @Override
        public void setValue(String value) {
            Objects.requireNonNull(value, "Delta value must not be null");

            if (!valueEquals(value, getValue())) {
                RichTextEditor.this.getElement().setProperty("value", value);
                // After setting delta value, manually sync back the updated
                // HTML value, which will eventually trigger a server-side value
                // change event on the component
                RichTextEditor.this.getElement()
                        .executeJs("return this.htmlValue").then(jsonValue -> {
                            isHtmlValueSync = true;
                            RichTextEditor.this.setValue(jsonValue.asString());
                            isHtmlValueSync = false;
                        });

                fireChangeEvent(false);
                oldValue = value;
            }
        }

        /**
         * Returns the current value of this editor in the
         * <a href="https://github.com/quilljs/delta">Quill Delta</a> format. By
         * default, the empty editor will return an empty string.
         *
         * @return the current value.
         */
        @Override
        public String getValue() {
            return RichTextEditor.this.getElement().getProperty("value");
        }

        @Override
        public Registration addValueChangeListener(
                ValueChangeListener<? super ValueChangeEvent<String>> valueChangeListener) {
            return ComponentUtil.addListener(RichTextEditor.this,
                    DeltaValueChangeEvent.class,
                    valueChangeListener::valueChanged);
        }

        private void fireChangeEvent(boolean isFromClient) {
            DeltaValueChangeEvent changeEvent = new DeltaValueChangeEvent(
                    RichTextEditor.this, this, oldValue, isFromClient);
            ComponentUtil.fireEvent(RichTextEditor.this, changeEvent);
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            RichTextEditor.this.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() {
            return RichTextEditor.this.isReadOnly();
        }

        @Override
        public void setRequiredIndicatorVisible(
                boolean requiredIndicatorVisible) {
            RichTextEditor.this
                    .setRequiredIndicatorVisible(requiredIndicatorVisible);
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            return RichTextEditor.this.isRequiredIndicatorVisible();
        }

        @Override
        public String getEmptyValue() {
            return "";
        }

        @Override
        public void clear() {
            RichTextEditor.this.clear();
        }
    }
}
