/**
 * Sample of serverside generation of Highcharts using an extension to jsdom in
 * node.js.
 */

/* eslint-env node, es6 */
/* eslint no-console: 0 */
const jsdom = require('jsdom');
const fs = require('fs');
const path = require('path');
const customWidthsMap = require('./customWidthsMap.js');
const defaultWidthsMap = require('string-pixel-width/lib/widthsMap.js');
const pixelWidth = require('string-pixel-width');

// Combine the default font widths map from string-pixel-width with our additions
const widthsMap = { ...defaultWidthsMap, ...customWidthsMap };

const { JSDOM } = jsdom;

// Get the document and window
const dom = new JSDOM(
    `<!doctype html>
    <html>
        <body>
            <div id="container"></div>
        </body>
    </html>`);
const win = dom.window;
const doc = win.document;
//workaround for highcharts#15913
global.Node = win.Node

// Require Highcharts with the window shim
const Highcharts = require('highcharts/highstock.src')(win);
require("highcharts/modules/accessibility")(Highcharts);
require("highcharts/highcharts-more")(Highcharts);
require("highcharts/highcharts-3d")(Highcharts);
require("highcharts/modules/data")(Highcharts);
require("highcharts/modules/drilldown")(Highcharts);
require("highcharts/modules/exporting")(Highcharts);
require("highcharts/modules/funnel")(Highcharts);
require("highcharts/modules/heatmap")(Highcharts);
require("highcharts/modules/solid-gauge")(Highcharts);
require("highcharts/modules/treemap")(Highcharts);
require("highcharts/modules/no-data-to-display")(Highcharts);
require("highcharts/modules/sankey")(Highcharts);
require("highcharts/modules/timeline")(Highcharts);
require("highcharts/modules/organization")(Highcharts);
require("highcharts/modules/xrange")(Highcharts);
require("highcharts/modules/bullet")(Highcharts);

win.Date = Date;

function processTextNodes(element, cb) {
    for (var childNode of element.childNodes) {
        if (childNode.nodeType === Node.ELEMENT_NODE) {
            processTextNodes(childNode, cb);
        } else if (childNode.nodeType === Node.TEXT_NODE) {
            cb(childNode, element);
        }
    }
}

/**
 * Search up parent chain for an element with the requested attribute set
 * 
 * @param {*} ele 
 * @param {*} attr 
 * @returns Attribute, possibly inherited, or undefined if not found
 */
function findStyleAttr(ele, attr) {
    while (ele) {
        if (ele.style[attr]) {
            return ele.style[attr];
        }
        ele = ele.parentElement;
    }
}

const sizableFonts = Object.keys(widthsMap);

/**
 * The string-pixel-width library supports a limited set of fonts. Search a font-family
 * list for a usable font, or find the next-best fallback
 */
function findSizableFont(fontFamily = "") {
    let fonts = fontFamily.split(",")
        .map(s => s.trim())
        .map(s => s.replace(/^"(.*)"$/, '$1')) // Un-quote any quoted entries
        .map(s => s.toLowerCase());

    // Search the font list for one in our list of sizable fonts
    let usableFont = fonts.find(f => sizableFonts.includes(f));

    if (!usableFont) {
        // None of those are sizable. Go with Highcharts default font
        usableFont = 'times new roman';
    }

    return usableFont;
}

function removeHighchartsTextOutlines(elem) {
    let children = [].slice.call(
        elem.children.length ? elem.children : [elem]
    );
    children.forEach(child => {
        if (child.getAttribute('class') === 'highcharts-text-outline') {
            child.parentNode.removeChild(child);
        }
    });

}

// Do some modifications to the jsdom document in order to get the SVG bounding
// boxes right.

/**
 * Pass Highcharts' test for SVG capabilities
 * @returns {undefined}
 */
win.SVGElement.prototype.createSVGRect = function() { };
/**
 * jsdom doesn't compute layout (see
 * https://github.com/tmpvar/jsdom/issues/135). This getBBox implementation
 * provides just enough information to get Highcharts to render text boxes
 * correctly, and is not intended to work like a general getBBox
 * implementation. The height of the boxes are computed from the sum of
 * tspans and their font sizes. The width is based on an average width for
 * each glyph. It could easily be improved to take font-weight into account.
 * For a more exact result we could to create a map over glyph widths for
 * several fonts and sizes, but it may not be necessary for the purpose.
 * If the width for the element is zero, then the height is also
 * set to zero, in order to not reserve any vertical space for elements
 * without content.
 * @returns {Object} The bounding box
 */
win.SVGElement.prototype.getBBox = function() {
    let lineWidth = 0,
        lineHeight = 0,
        width = 0,
        height = 0,
        newLine = false;

    removeHighchartsTextOutlines(this);

    processTextNodes(this, (textNode, child) => {
        if (child.tagName === 'title') {
            return;
        }
        let fontSize = findStyleAttr(child, 'fontSize'),
        fontFamily = findStyleAttr(child, 'fontFamily'),
        textLength;

        // The font size and lineHeight is based on empirical values,
        // copied from the SVGRenderer.fontMetrics function in
        // Highcharts.
        if (/px/.test(fontSize)) {
            fontSize = parseInt(fontSize, 10);
        } else {
            fontSize = /em/.test(fontSize) ?
                parseFloat(fontSize) * 12 :
                12;
        }
        let nodeHeight = fontSize < 24 ?
            fontSize + 3 :
            Math.round(fontSize * 1.2);
        lineHeight = Math.max(lineHeight, nodeHeight);
        let fontToUse = findSizableFont(fontFamily);
        textLength = pixelWidth(textNode.data, { size: fontSize, font: fontToUse, map: widthsMap });

        // In practice, dy is used to trigger a new line
        if (child.getAttribute('dy') !== null) {
            lineWidth = 0;
            newLine = true;
        }

        lineWidth += textLength;
        width = Math.max(width, lineWidth);

        if (newLine) {
            height += lineHeight;
            newLine = false;
            lineHeight = 0;
        }
    });

    // Add the height of the ongoing line
    height += lineHeight;

    // If the width of the text box is 0, always return a 0 height (since the element indeed consumes no space)
    // Returning a non-zero height causes Highcharts to allocate vertical space in the chart for text that doesn't
    // exist
    let retHeight = width == 0 ? 0 : height;
    return {
        x: 0,
        y: 0,
        width: width,
        height: retHeight
    };
};
/**
 * Estimate the rendered length of a substring of text. Uses a similar strategy to getBBox,
 * above.
 *
 * @param {integer} charnum Starting character position
 * @param {integer} numchars Number of characters to count
 * @returns Rendered length of the substring (estimated)
 */
win.SVGElement.prototype.getSubStringLength = function(charnum, numchars) {
    let offset = charnum,
        remaining = numchars,
        textLength = 0;

    removeHighchartsTextOutlines(this);

    processTextNodes(this, (textNode, child) => {
        if (child.tagName === 'title') {
            return;
        }

        if (remaining <= 0) {
            return;
        }
        let childLength = textNode.length;

        if (childLength <= offset) {
            offset -= childLength;
        } else {
            let usedLength = Math.min(childLength - offset, remaining);
            remaining -= usedLength;

            let fontSize = findStyleAttr(child, 'fontSize'),
            fontFamily = findStyleAttr(child, 'fontFamily');

            // The font size is based on empirical values,
            // copied from the SVGRenderer.fontMetrics function in
            // Highcharts.
            if (/px/.test(fontSize)) {
                fontSize = parseInt(fontSize, 10);
            } else {
                fontSize = /em/.test(fontSize) ?
                    parseFloat(fontSize) * 12 :
                    12;
            }
            let textToSize = textNode.data.substring(offset, offset + usedLength);
            let fontToUse = findSizableFont(fontFamily);
            let measuredWidth = pixelWidth(textToSize, { size: fontSize, font: fontToUse, map: widthsMap });
            textLength += measuredWidth;
        }
    });

    return textLength;
}

const inflateFunctions = (jsonConfiguration) => {
    Object.entries(jsonConfiguration).forEach(([attr, targetProperty]) => {
        if (attr.indexOf('_fn_') === 0 && (typeof targetProperty === 'string' || targetProperty instanceof String)) {
            const property = attr.replace('_fn_', '');
            const jsFunction = Function(`'use strict'; return ${targetProperty}`);
            if (targetProperty.trim().startsWith('function')) {
                jsonConfiguration[property] = jsFunction();
            } else {
                jsonConfiguration[property] = jsFunction;
            }
            delete jsonConfiguration[attr];
        } else if (targetProperty instanceof Object) {
            inflateFunctions(targetProperty);
        }
    });
}

/**
 * ExportOptions
 *
 * @typedef ExportOptions
 *
 * @property {object} theme
 * @property {object} lang
 * @property {string} width
 * @property {string} height
 * @property {boolean} timeline
 * @property {boolean} executeFunctions
 */

/**
 * ExportConfiguration
 *
 * @typedef ExportConfiguration
 *
 * @property {string} chartConfigurationFile A relative path to a file containing the configuration in JSON.
 * @property {object} chartConfiguration An object with the configuration. Only has effect when `chartConfigurationFile` is not provided.
 * @property {string} outFile
 * @property {ExportOptions} exportOptions
 */

/**
 * SVGResult
 *
 * @typedef SVGResult
 *
 * @property {string} svgString
 * @property {string} outFile
 */

/**
 * Function to export SVG a string containing a chart based
 * on the configuration provided.
 *
 * - The `chartConfiguration` property can be used to provide a configuration when the exporter is being called from another JS script.
 * - The `chartConfigurationFile` property can be used when the exporter is being called via CLI to get around the max argument length limit.
 *
 * The `chartConfigurationFile` property takes priority over `chartConfiguration`.
 *
 * @param {ExportConfiguration} configuration
 *
 * @returns {Promise<SVGResult>} Object with the result of the export
 */
const jsdomExporter = ({ chartConfigurationFile, chartConfiguration, outFile = 'chart.svg', exportOptions }) => {
    return new Promise((resolve, reject) => {
        if (chartConfigurationFile) {
            chartConfiguration = JSON.parse(
                fs.readFileSync(path.join(__dirname, chartConfigurationFile), 'utf8').toString()
            );
        }

        // Disable all animation and default title
        Highcharts.setOptions({
            plotOptions: {
                series: {
                    animation: false,
                    dataLabels: {
                        defer: false
                    }
                }
            },
            credits: { enabled: false },
            exporting: { enabled: false },
            title: { text : null }
        });

        let isTimeline = false;
        if (exportOptions) {
            if (exportOptions.theme) {
                Highcharts.setOptions(exportOptions.theme);
            }

            if (exportOptions.lang) {
                Highcharts.setOptions({ lang: exportOptions.lang })
            }

            if (exportOptions.height || exportOptions.width) {
                const chartOptions = {
                    ...exportOptions.height && { height: exportOptions.height },
                    ...exportOptions.width && { width: exportOptions.width },
                };
                chartConfiguration.chart = { ...chartConfiguration.chart, ...chartOptions };
            }

            isTimeline = exportOptions.timeline;

            if (exportOptions.executeFunctions) {
                inflateFunctions(chartConfiguration);
            }
        }

        let chart;

        // Generate the chart into the container
        try {
            const constr = isTimeline ? 'stockChart' : 'chart';
            chart = Highcharts[constr](
                'container',
                chartConfiguration
            );
        } catch (e) {
            reject(e);
        }

        let svg = chart.sanitizeSVG(
            chart.container.innerHTML
        );
        fs.writeFile(path.join(__dirname, outFile), svg, function (err) {
            if (err) {
                reject(err);
            }

            resolve({
                svgString: svg,
                outFile: __dirname + '/' + outFile
            });
        });
    });
};

module.exports = jsdomExporter;
