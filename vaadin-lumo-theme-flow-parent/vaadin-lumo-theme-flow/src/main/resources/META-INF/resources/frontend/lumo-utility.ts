import { utility } from '@vaadin/vaadin-lumo-styles/utility.js';

var hasStyles = false;
const styleTags = document.head.getElementsByTagName('style');
for (var i = 0; i < styleTags.length; i++) {
  if (styleTags[i].sheet === utility.styleSheet) {
    hasStyles = true;
    break;
  }
}
if (!hasStyles) {
  const tpl = document.createElement('template');
  tpl.innerHTML = `<style>${utility.cssText}</style>`;
  document.head.appendChild(tpl.content);
}
