window.Vaadin.Flow.button = {
  initDisableOnClick: (button) => {
    if (!button.__hasDisableOnClickListener) {
      button.addEventListener('click', disableOnClickListener(button));
      button.__hasDisableOnClickListener = true;
    }
  }
}

function disableOnClickListener({currentTarget: button}) {
  button.disabled = button.hasAttribute('disableOnClick');
}
