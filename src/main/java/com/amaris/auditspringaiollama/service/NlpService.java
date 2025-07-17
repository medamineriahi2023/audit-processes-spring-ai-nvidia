package com.amaris.auditspringaiollama.service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
@Service
public class NlpService {
    static class TestCase {
        String word;
        boolean isInfinitive;

        TestCase(String word, boolean isInfinitive) {
            this.word = word;
            this.isInfinitive = isInfinitive;
        }
    }
    public void test(){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("tokenize.language", "fr");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/french-ud.tagger");
//        props.setProperty("lemma.model", "edu/stanford/nlp/models/lemma/french-lemma.txt");
        props.setProperty("log.consoleLevel", "INFO");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // Données de test
        List<NlpService.TestCase> testData = Arrays.asList(
                new NlpService.TestCase("manger", true),
                new NlpService.TestCase("parler", true),
                new NlpService.TestCase("courir", true),
                new NlpService.TestCase("chien", false),
                new NlpService.TestCase("marche", false),
                new NlpService.TestCase("chanté", false),
                new NlpService.TestCase("vu", false),
                new NlpService.TestCase("marcher", true),
                new NlpService.TestCase("voir", true),
                new NlpService.TestCase("dessin", false),
                new NlpService.TestCase("lire", true),
                new NlpService.TestCase("parle", false),
                new NlpService.TestCase("tomber", true),
                new NlpService.TestCase("tombe", false)
        );

        int tp = 0, fp = 0, fn = 0;

        for (NlpService.TestCase test : testData) {
            // Annoter le mot comme un document
            Annotation annotation = new Annotation(test.word);
            pipeline.annotate(annotation);

            boolean predictedAsInfinitive = false;

            List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                String word = token.word();
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                // Vérifier si c'est un verbe à l'infinitif (lemme = mot && pos commence par V)
                if (lemma != null && word.equalsIgnoreCase(lemma) && pos != null && pos.toLowerCase().startsWith("v")) {
                    predictedAsInfinitive = true;
                }

                System.out.printf("Mot: %-10s | POS: %-6s | Lemme: %-10s | Prédit: %-5s | Attendu: %-5s\n",
                        word, pos, lemma, predictedAsInfinitive, test.isInfinitive);
            }

            if (test.isInfinitive && predictedAsInfinitive) tp++;
            else if (!test.isInfinitive && predictedAsInfinitive) fp++;
            else if (test.isInfinitive && !predictedAsInfinitive) fn++;
        }

        // Résultats
        double precision = tp + fp > 0 ? (double) tp / (tp + fp) : 0;
        double recall = tp + fn > 0 ? (double) tp / (tp + fn) : 0;
        double f1 = precision + recall > 0 ? 2 * precision * recall / (precision + recall) : 0;

        System.out.println("\n--- Résultats globaux ---");
        System.out.println("✅ TP : " + tp);
        System.out.println("❌ FP : " + fp);
        System.out.println("🚫 FN : " + fn);
        System.out.printf("🎯 Précision : %.2f\n", precision);
        System.out.printf("📥 Rappel    : %.2f\n", recall);
        System.out.printf("📊 F1-Score  : %.2f\n", f1);
    }
    }
