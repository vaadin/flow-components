/**
 * Applies fixes to the bundled Highcharts version, same as in @vaadin/charts.
 * TODO: Remove when upgrading Highcharts, see vaadin/web-components#12213
 */

/* eslint-env node, es6 */

const RESERVED_KEYS = ['__proto__', 'constructor'];

const hasOwn = (object, key) => Object.prototype.hasOwnProperty.call(object, key);

const isReservedKey = (key) => RESERVED_KEYS.includes(key);

function applyHighchartsPatches(Highcharts) {
    const { Point, SVGElement } = Highcharts;

    // Skip reserved option names in point objects
    Highcharts.wrap(Point.prototype, 'optionsToObject', function (proceed, options) {
        if (
            options &&
            typeof options === 'object' &&
            !Array.isArray(options) &&
            RESERVED_KEYS.some((key) => hasOwn(options, key))
        ) {
            options = Object.fromEntries(Object.entries(options).filter(([key]) => !isReservedKey(key)));
        }
        return proceed.call(this, options);
    });

    // Ignore event attribute names, e.g. from style options that are applied as attributes.
    // Not using `Highcharts.wrap`, as this setter runs for most attributes on every render.
    const defaultSetter = SVGElement.prototype._defaultSetter;
    SVGElement.prototype._defaultSetter = function (value, key, element) {
        if (!/^on/iu.test(key)) {
            defaultSetter.call(this, value, key, element);
        }
    };
}

module.exports = applyHighchartsPatches;
