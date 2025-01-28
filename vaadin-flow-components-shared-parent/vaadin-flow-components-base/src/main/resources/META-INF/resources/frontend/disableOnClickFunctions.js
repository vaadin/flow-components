document.addEventListener('click', (event) => {
  const target = event.composedPath().find((node) => node.hasAttribute && node.hasAttribute('disableOnClick'));
  if (target) {
    target.disabled = true;
  }
});
