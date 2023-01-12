(function () {
  function copyClassName(dialog) {
    const overlay = dialog.$.overlay;
    if (overlay) {
      overlay.className = dialog.className;
    }
  }

  const observer = new MutationObserver((records) => {
    records.forEach((mutation) => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        copyClassName(mutation.target);
      }
    });
  });

  window.Vaadin.Flow.dialogConnector = {
    initLazy: function (dialog) {
      if (dialog.$connector) {
        return;
      }
      dialog.$connector = {};

      dialog.addEventListener('opened-changed', (e) => {
        if (e.detail.value) {
          copyClassName(dialog);
        }
      });

      observer.observe(dialog, {
        attributes: true,
        attributeFilter: ['class']
      });

      // On connector init, the <vaadin-dialog> may not yet be ready,
      // so we need to check if it has the $ property which gets accessed
      // in copyClassName.
      // Also, the class name does not need to be copied if the dialog
      // is not initially opened.
      if (dialog.opened && dialog.$) {
        copyClassName(dialog);
      }
    }
  };
})();
