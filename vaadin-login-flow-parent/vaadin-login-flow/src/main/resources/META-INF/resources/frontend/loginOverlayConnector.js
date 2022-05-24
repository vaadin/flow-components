(function () {
  function copyClassName(loginOverlay) {
    const overlayWrapper = loginOverlay.$.vaadinLoginOverlayWrapper;
    if (overlayWrapper) {
      overlayWrapper.className = loginOverlay.className;
    }
  }

  const observer = new MutationObserver((records) => {
    records.forEach((mutation) => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        copyClassName(mutation.target);
      }
    });
  });

  window.Vaadin.Flow.loginOverlayConnector = {
    initLazy: function (loginOverlay) {
      if (loginOverlay.$connector) {
        return;
      }

      loginOverlay.$connector = {};

      loginOverlay.addEventListener('opened-changed', (e) => {
        if (e.detail.value) {
          copyClassName(loginOverlay);
        }
      });

      observer.observe(loginOverlay, {
        attributes: true,
        attributeFilter: ['class']
      });

      // Copy initial class
      copyClassName(loginOverlay);
    }
  };
})();
