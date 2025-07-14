package com.amaris.auditspringaiollama.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.text.MessageFormat;


@Configuration
@PropertySource("classpath:prompts.properties")
public class PromptConfiguration {

    @Value("${audit.prompt.infinitive.verb}")
    private String infinitiveVerbPrompt;

    @Value("${audit.prompt.past.tense.word}")
    private String pastTenseWordPrompt;

    @Value("${audit.prompt.past.tense.expression}")
    private String pastTenseExpressionPrompt;

    @Value("${audit.prompt.abbreviation.detection}")
    private String abbreviationDetectionPrompt;

    @Value("${audit.prompt.abbreviation.simple}")
    private String abbreviationSimplePrompt;


    public String getInfinitiveVerbPrompt(String word) {
        return MessageFormat.format(infinitiveVerbPrompt, word);
    }

    public String getPastTenseWordPrompt(String word) {
        return MessageFormat.format(pastTenseWordPrompt, word);
    }

    public String getPastTenseExpressionPrompt(String expression) {
        return MessageFormat.format(pastTenseExpressionPrompt, expression);
    }

    public String getAbbreviationDetectionPrompt(String text) {
        return MessageFormat.format(abbreviationDetectionPrompt, text);
    }

    public String getAbbreviationSimplePrompt(String expression) {
        return MessageFormat.format(abbreviationSimplePrompt, expression);
    }
}
