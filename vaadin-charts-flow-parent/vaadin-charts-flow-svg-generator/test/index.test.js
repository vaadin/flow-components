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