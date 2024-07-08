/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import '@polymer/polymer/lib/elements/custom-style.js';
const $_documentTemplate = document.createElement('template');

$_documentTemplate.innerHTML = `<dom-module theme-for="vaadin-combo-box" id="no-clear-button">
    <template>
        <style>
            :host(.no-clear-button) [part="clear-button"]{
                display:none
            }
        </style>
    </template>
</dom-module>`;

document.head.appendChild($_documentTemplate.content);
