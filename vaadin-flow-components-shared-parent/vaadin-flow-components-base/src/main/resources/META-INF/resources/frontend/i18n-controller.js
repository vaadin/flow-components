function deepMerge(target, source) {
  function merge(target, source) {
    for (const key in source) {
      if (typeof source[key] === 'object' && !Array.isArray(source[key])) {
        if (!target[key]) {
          target[key] = {};
        }
        merge(target[key], source[key]);
      } else {
        target[key] = source[key];
      }
    }
  }

  merge(target, source);
  return target;
}

window.Vaadin.Flow.i18nController = {
  updateI18n: function(element, i18n) {
    element.i18n = deepMerge({ ...element.i18n }, i18n);
  }
};
