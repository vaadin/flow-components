import { utility } from '@vaadin/vaadin-lumo-styles/utility.js';

const tpl = document.createElement('template');
tpl.innerHTML = `<style>${utility.cssText}</style>`;
document.head.appendChild(tpl.content);
