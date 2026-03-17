# AI Dashboard Demo

## Running the demo

1. Clone the repo and check out the `proto/ai-dashboard` branch:

```
git clone -b proto/ai-dashboard https://github.com/vaadin/flow-components.git
cd flow-components
```

2. Set your OpenAI API key:

```
export OPENAI_API_KEY=your-key-here
```

3. Start the server:

```
mvn -am -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-integration-tests -DskipTests package jetty:run
```

4. Open http://localhost:8080/vaadin-ai/ai-dashboard-demo

There's also an AI Charts prototype demo at http://localhost:8080/vaadin-ai/ai-chart-demo
