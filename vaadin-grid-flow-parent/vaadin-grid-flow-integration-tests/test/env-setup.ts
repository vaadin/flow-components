window.Vaadin ||= {};
window.Vaadin.Flow = {};
window.Vaadin.Flow.tryCatchWrapper = function (originalFunction) {
  return function () {
    return originalFunction.apply(this, arguments);
  };
};
