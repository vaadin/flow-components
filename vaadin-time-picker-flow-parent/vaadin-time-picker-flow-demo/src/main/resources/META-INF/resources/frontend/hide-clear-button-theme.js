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
