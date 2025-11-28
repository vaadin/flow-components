package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class Helpers {

    public static LLMProvider createLlmProvider() {
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);
        return provider;
    }
    
}
