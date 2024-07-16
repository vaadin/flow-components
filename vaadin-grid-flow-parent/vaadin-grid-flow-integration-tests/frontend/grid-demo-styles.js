/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<dom-module id="my-grid-theme" theme-for="vaadin-grid">
    <template>
        <style>
            [part~="cell"].subscriber {
                background: rgb(245, 245, 255);
            }
            .minor {
                color: red;
                font-weight: bold;
            }
        </style>
    </template>
</dom-module>`;

document.head.appendChild($_documentContainer.content);
