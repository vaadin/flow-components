(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Text Field', 'vaadin-text-field');
    };

    window.Vaadin.Flow.fieldConnector = {
        initLazy: (field) => tryCatchWrapper(function (field) {
            // Check whether the connector was already initialized for the field
            if (field.$connector) {
                return;
            }

            field.$connector = {};

            field.$connector.forwardBlur = tryCatchWrapper(function (){
                if (field.inputElement) {
                    field.inputElement.addEventListener('blur', (event) => {
                        event.stopPropagation();
                        field.dispatchEvent(new Event('blur'));
                    });
                }
                else {
                    setTimeout(() => {
                        field.$connector.forwardBlur();
                    }, 10);
                }
            });

            field.$connector.forwardBlur();
        })(field)
    }
})();
