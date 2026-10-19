const { expect } = require('chai')
const rewire = require('rewire');
const { JSDOM } = require('jsdom');
const mock = require('mock-fs')
const pixelWidth = require('string-pixel-width');

const jsdomExporter = rewire('../jsdom-exporter.js')

const exporterDom = jsdomExporter.__get__("dom");
const widthsMap = jsdomExporter.__get__("widthsMap");

/**
 *
 * @param {string} svgString
 * @returns Document
 */
function parseSVG(svgString) {
  const dom = new JSDOM(`<body>${svgString}</body>`);
  return dom.window.document
}

describe('jsdom-exporter', () => {

  beforeEach(() => mock());

  afterEach(() => mock.restore());

  it('should render based on Highchart options', async () => {
    const result = await jsdomExporter({ chartConfiguration: { title: { text: 'TITLE' } } });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('svg')).to.be.not.null;
    expect(document.querySelector('.highcharts-title').textContent).to.be.equal('TITLE');
  });

  it('should use default filename to write svg file', async () => {
    const result = await jsdomExporter({ chartConfiguration: {} });

    expect(result.outFile).to.contain('chart.svg');
  });

  it('should accept outfile name to write svg file', async () => {
    const result = await jsdomExporter({ chartConfiguration: {}, outFile: 'custom-file.svg' });

    expect(result.outFile).to.contain('custom-file.svg');
  });

  it('should accept width/height as exporting options', async () => {
    const configuration = { chartConfiguration: {}, exportOptions: { width: 100, height: 100 } };
    const result = await jsdomExporter(configuration);

    const document = parseSVG(result.svgString);
    const svgElement = document.querySelector('svg');

    expect(svgElement.getAttribute('width')).to.be.equal('100');
    expect(svgElement.getAttribute('height')).to.be.equal('100');
  });

  it('should accept theme as exporting options', async () => {
    const configuration = {
      chartConfiguration: {}, exportOptions: {
        theme: {
          chart: {
            backgroundColor: "red"
          }
        }
      }
    };
    const result = await jsdomExporter(configuration);
    const document = parseSVG(result.svgString);

    const backgroundColor = document.querySelector('.highcharts-background').getAttribute('fill');
    expect(backgroundColor).to.be.equal('red');
  });

  it('should accept lang as exporting options', async () => {
    const configuration = { chartConfiguration: {}, exportOptions: { lang: { noData: 'custom message' } } };
    const result = await jsdomExporter(configuration);
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-no-data').textContent).to.be.equal('custom message');
  });

  it('should not inflate functions if "executeFunctions" is not enabled', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        xAxis: {
          min: 0,
          max: 360,
          labels: {
            _fn_formatter: `function () { return this.value + 'CUSTOM_LABEL'; }`
          },
          tickInterval: 45
        },
        series: [1]
      }
    });
    const document = parseSVG(result.svgString);
    expect(document.querySelector('.highcharts-xaxis-labels text').textContent).to.not.contain('CUSTOM_LABEL');
  });

  it('should inflate functions if "executeFunctions" is enabled', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        xAxis: {
          min: 0,
          max: 360,
          labels: {
            _fn_formatter: `function () { return this.value + 'CUSTOM_LABEL'; }`
          },
          tickInterval: 45
        },
        series: [1]
      }, exportOptions: { executeFunctions: true }
    });
    const document = parseSVG(result.svgString);
    expect(document.querySelector('.highcharts-xaxis-labels text').textContent).to.contain('CUSTOM_LABEL');
  });

  it('should inflate js expression if "executeFunctions" is enabled', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        xAxis: {
          min: 0,
          max: 360,
          labels: {
            _fn_formatter: `this.value + 'CUSTOM_LABEL'`
          },
          tickInterval: 45
        },
        series: [1]
      }, exportOptions: { executeFunctions: true }
    });
    const document = parseSVG(result.svgString);
    expect(document.querySelector('.highcharts-xaxis-labels text').textContent).to.contain('CUSTOM_LABEL');
  });

  it('should not have credits on generated SVG', async () => {
    const result = await jsdomExporter({ chartConfiguration: { title: { text: 'TITLE' } } });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-credits')).to.be.null;
  });

  it('should not have exporting menu on generated SVG', async () => {
    const result = await jsdomExporter({ chartConfiguration: { title: { text: 'TITLE' } } });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-exporting-group')).to.be.null;
  });
});

describe('option values', () => {
  // eslint-disable-next-line no-script-url
  const UNSUPPORTED_URL = 'javascript:void(0)';

  const Highcharts = jsdomExporter.__get__('Highcharts');

  beforeEach(() => mock());

  afterEach(() => {
    mock.restore();
    delete Object.prototype.custom;
  });

  function createChart(options) {
    const container = exporterDom.window.document.createElement('div');
    return Highcharts.chart(container, options);
  }

  it('should set nested point keys', async () => {
    const chart = createChart({ series: [{ keys: ['y', 'custom.value'], data: [[1, 'a']] }] });
    expect(chart.series[0].points[0].custom.value).to.equal('a');
  });

  it('should ignore reserved segments in point keys', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        series: [{ keys: ['y', '__proto__.custom', 'constructor.prototype.custom'], data: [[1, 'a', 'b']] }]
      }
    });
    expect({}.custom).to.be.undefined;
    expect(parseSVG(result.svgString).querySelector('.highcharts-series')).to.be.not.null;
  });

  it('should ignore reserved option names in point objects', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        series: [{ data: JSON.parse('[{ "y": 1, "__proto__": { "custom": "a" }, "constructor": { "custom": "b" } }]') }]
      }
    });
    expect(parseSVG(result.svgString).querySelectorAll('.highcharts-series-group .highcharts-point')).to.have.lengthOf(1);
  });

  ['https://vaadin.com', 'about.html', 'tel:+123'].forEach((href) => {
    it(`should keep supported credits links: ${href}`, () => {
      const chart = createChart({ credits: { enabled: true, href } });
      expect(chart.options.credits.href).to.equal(href);
    });
  });

  [UNSUPPORTED_URL, ` JAVASCRIPT:void(0)`, 'data:text/html,Text'].forEach((href) => {
    it(`should ignore credits links that use an unsupported URL scheme: ${JSON.stringify(href)}`, () => {
      const chart = createChart({ credits: { enabled: true, href } });
      expect(chart.options.credits.href).to.be.undefined;
    });
  });

  it('should not apply event attribute names to SVG elements', () => {
    const chart = createChart({});
    const rect = chart.renderer.rect(0, 0, 10, 10).attr({ onmouseover: 'void(0)', fill: 'red' }).add();
    expect(rect.element.hasAttribute('onmouseover')).to.be.false;
    expect(rect.element.getAttribute('fill')).to.equal('red');
  });

  it('should not export event attribute names from breadcrumbs style options', async () => {
    const result = await jsdomExporter({
      chartConfiguration: {
        chart: { events: { _fn_load: "function () { this.series[0].setRootNode('a'); }" } },
        series: [{
          type: 'treemap',
          allowTraversingTree: true,
          breadcrumbs: { style: { onmouseover: 'void(0)' } },
          data: [
            { id: 'a', name: 'A' },
            { id: 'b', name: 'B', parent: 'a', value: 1 }
          ]
        }]
      },
      exportOptions: { executeFunctions: true }
    });
    const document = parseSVG(result.svgString);
    expect(document.querySelectorAll('.highcharts-breadcrumbs-button').length).to.be.above(0);
    expect(document.querySelector('[onmouseover]')).to.be.null;
  });

  it('should keep supported xlink:href values', async () => {
    const result = await jsdomExporter({
      chartConfiguration: { title: { text: '<a xlink:href="https://vaadin.com">Title</a>' } }
    });
    const link = parseSVG(result.svgString).querySelector('.highcharts-title a');
    expect(link.getAttribute('xlink:href')).to.equal('https://vaadin.com');
  });

  it('should drop unsupported xlink:href values', async () => {
    const result = await jsdomExporter({
      chartConfiguration: { title: { text: `<a xlink:href="${UNSUPPORTED_URL}">Title</a>` } }
    });
    expect(result.svgString).to.contain('Title');
    expect(result.svgString).to.not.contain(UNSUPPORTED_URL);
  });
});

describe('timeline', () => {
  beforeEach(() => mock());

  afterEach(() => mock.restore());

  it('should render stock chart if timeline is set to `true`', async () => {
    const result = await jsdomExporter({ chartConfiguration: {}, exportOptions: { timeline: true } });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-navigator')).to.be.not.null;
  });
});

describe('getSubStringLength', () => {
  it('should measure strings split across multiple elements', () => {
    let window = exporterDom.window;
    let document = window.document;
    let container = document.getElementById('container');
    container.innerHTML = `
    <svg>
      <text style="font-family: arial; font-size: 12px;">01234<tspan>56789</tspan></text>
    </svg>
    `;
    let text = container.querySelector('text');
    expect(text).to.be.not.null;
    expect(text.getSubStringLength(6, 3)).to.equal(pixelWidth("678", { size: 12, font: 'arial', map: widthsMap }))
    expect(text.getSubStringLength(1, 2)).to.equal(pixelWidth("12", { size: 12, font: 'arial', map: widthsMap }))
    expect(text.getSubStringLength(2, 6)).to.equal(pixelWidth("234567", { size: 12, font: 'arial', map: widthsMap }))
    expect(text.getSubStringLength(0, 20)).to.equal(pixelWidth("0123456789", { size: 12, font: 'arial', map: widthsMap }))
  });
});