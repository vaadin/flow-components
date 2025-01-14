document.addEventListener('click', (event) => {
  const target = event.composedPath().find((node) => node.disableOnClick);
  if (target) {
    target.disabled = true;
  }
});
