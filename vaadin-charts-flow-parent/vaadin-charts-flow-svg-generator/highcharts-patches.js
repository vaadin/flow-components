/**
 * Applies fixes to the bundled Highcharts version, same as in @vaadin/charts.
 * TODO: Remove when upgrading Highcharts, see vaadin/web-components#12213
 */

/* eslint-env node, es6 */

const RESERVED_KEYS = ['__proto__', 'constructor'];

const UNSUPPORTED_SCHEMES = ['javascript', 'vbscript', 'data'];

// Relative links are resolved against a placeholder, as the exporter has no page URL
const BASE_URL = 'https://localhost/';

const hasOwn = (object, key) => Object.prototype.hasOwnProperty.call(object, key);

const isReservedKey = (key) => RESERVED_KEYS.includes(key);

const isUnsupportedLink = (value) => {
    if (typeof value !== 'string') {
        return true;
    }
    try {
        // The URL parser normalizes case and whitespace the same way as navigation does
        return UNSUPPORTED_SCHEMES.includes(new URL(value, BASE_URL).protocol.slice(0, -1));
    } catch (_) {
        return true;
    }
};

function applyHighchartsPatches(Highcharts) {
    const { AST, Chart, Point, SVGElement } = Highcharts;

    const isAllowedReference = (value) =>
        typeof value === 'string' && AST.allowedReferences.some((ref) => value.startsWith(ref));

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

    // Skip reserved path segments in point keys
    Highcharts.wrap(Point.prototype, 'setNestedProperty', function (proceed, object, value, key) {
        if (String(key).split('.').some(isReservedKey)) {
            return object;
        }
        return proceed.call(this, object, value, key);
    });

    // Ignore credits links that use an unsupported URL scheme
    Highcharts.wrap(Chart.prototype, 'addCredits', function (proceed, credits) {
        const options = Highcharts.merge(true, this.options.credits, credits);
        if (options?.href && isUnsupportedLink(options.href)) {
            Highcharts.error(33, false, this, { 'Invalid attribute in config': 'credits.href' });
            delete options.href;
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

    // Remove unsupported xlink:href values from text markup
    Highcharts.wrap(AST, 'filterUserAttributes', function (proceed, attributes) {
        if (attributes && hasOwn(attributes, 'xlink:href') && !isAllowedReference(attributes['xlink:href'])) {
            delete attributes['xlink:href'];
        }
        return proceed.call(this, attributes);
    });
}

module.exports = applyHighchartsPatches;
