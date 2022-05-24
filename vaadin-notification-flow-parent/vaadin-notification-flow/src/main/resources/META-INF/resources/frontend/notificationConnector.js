(function () {
  function copyClassName(notification) {
    const card = notification._card;
    if (card) {
      card.className = notification.className;
    }
  }

  const observer = new MutationObserver((records) => {
    records.forEach((mutation) => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
        copyClassName(mutation.target);
      }
    });
  });

  window.Vaadin.Flow.notificationConnector = {
    initLazy: function (notification) {
      if (notification.$connector) {
        return;
      }
      notification.$connector = {};

      notification.addEventListener('opened-changed', (e) => {
        if (e.detail.value) {
          copyClassName(notification);
        }
      });

      observer.observe(notification, {
        attributes: true,
        attributeFilter: ['class']
      });

      copyClassName(notification);
    }
  };
})();
