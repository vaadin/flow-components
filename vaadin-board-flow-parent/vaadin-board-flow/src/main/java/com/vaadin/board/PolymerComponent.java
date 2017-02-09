package com.vaadin.board;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractComponent;

// This is all because of
// https://github.com/webcomponents/webcomponentsjs/issues/638.
// When that is solved, only webcomponents-lite.js will be needed
// @JavaScript("vaadin://bower_components/webcomponentsjs/webcomponents-lite.js")

@JavaScript("vaadin://bower_components/webcomponents-platform/webcomponents-platform.js")
@JavaScript("vaadin://bower_components/URL/url.js")
@JavaScript("vaadin://bower_components/template/template.js")

// @JavaScript("vaadin://bower_components/html-imports/src/html-imports.js")
@JavaScript("vaadin://bower_components/html-imports/src/base.js")
@JavaScript("vaadin://bower_components/html-imports/src/module.js")
@JavaScript("vaadin://bower_components/html-imports/src/path.js")
@JavaScript("vaadin://bower_components/html-imports/src/xhr.js")
@JavaScript("vaadin://bower_components/html-imports/src/Loader.js")
@JavaScript("vaadin://bower_components/html-imports/src/Observer.js")
@JavaScript("vaadin://bower_components/html-imports/src/parser.js")
@JavaScript("vaadin://bower_components/html-imports/src/importer.js")
@JavaScript("vaadin://bower_components/html-imports/src/dynamic.js")
@JavaScript("vaadin://bower_components/html-imports/src/boot.js")

@JavaScript("vaadin://bower_components/es6-promise/dist/es6-promise.auto.min.js")
@JavaScript("vaadin://bower_components/webcomponentsjs/src/pre-polyfill.js")
@JavaScript("vaadin://bower_components/custom-elements/custom-elements.min.js")
@JavaScript("vaadin://bower_components/shadydom/shadydom.min.js")
@JavaScript("vaadin://bower_components/shadycss/shadycss.min.js")
@JavaScript("vaadin://bower_components/webcomponentsjs/src/post-polyfill.js")
@JavaScript("vaadin://bower_components/webcomponentsjs/src/unresolved.js")

@HtmlImport("vaadin://bower_components/polymer/polymer.html")
public class PolymerComponent extends AbstractComponent {

}
