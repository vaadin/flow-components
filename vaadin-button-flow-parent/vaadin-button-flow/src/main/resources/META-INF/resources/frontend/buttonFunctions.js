window.Vaadin.Flow.button = {
  initDisableOnClick: (target) => {
    const disableEvent = function (target) {
      if (target.getAttribute('disableOnClick')) {
        target.setAttribute('disabled', 'true');
      }
    };
    target.addEventListener('click', disableEvent)
  }
}
