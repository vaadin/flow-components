window.Vaadin ||= {};
const Flow = {
  tryCatchWrapper: function (originalFunction) {
    return function () {
      return originalFunction.apply(this, arguments);
    };
  }
};
// @ts-expect-error
window.Vaadin.Flow = Flow;
