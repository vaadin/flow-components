import '@polymer/polymer/lib/elements/custom-style.js';
const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<style>
/* Fixes zero width in flex layouts */
iron-list {
  flex: auto;
  align-self: stretch;
}
</style>`;

document.head.appendChild($_documentContainer.content);
