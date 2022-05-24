(function () {
  function copyClassName(consent) {
    const popup = consent._getPopup();
    if (popup) {
      popup.className = consent.className;
    }
  }

  const observer = new MutationObserver((records) => {
    records.forEach((mutation) => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        copyClassName(mutation.target);
      }
    });
  });

  window.Vaadin.Flow.cookieConsentConnector = {
    initLazy: function (consent) {
      if (consent.$connector) {
        return;
      }

      consent.$connector = {};

      observer.observe(consent, {
        attributes: true,
        attributeFilter: ['class']
      });

      copyClassName(consent);
    }
  };
})();
