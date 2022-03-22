(function () {
    const observer = new MutationObserver((records) => {
        records.forEach((mutation) => {
            if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                const dialog = mutation.target;
                const overlay = dialog.$.overlay.$.overlay;
                overlay.className = dialog.className;
            }
        });
    });

    window.Vaadin.Flow.dialogConnector = {
        initLazy: function (dialog) {
            if (dialog.$connector) {
                return;
            }
            dialog.$connector = {};

            observer.observe(dialog, { attributes: true, attributeFilter: ['class'] });
        }
    };
})();