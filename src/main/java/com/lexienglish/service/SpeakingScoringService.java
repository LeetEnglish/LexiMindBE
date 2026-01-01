package com.lexienglish.service;

import com.lexienglish.entity.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI Speaking Scoring Service
 * 
 * Evaluates speaking responses on multiple dimensions:
 * - Pronunciation
 * - Fluency & Coherence
 * - Vocabulary (Lexical Resource)
 * - Grammar (Grammatical Range & Accuracy)
 * 
 * Note: In a real implementation, this would:
 * 1. Accept audio file URL
 * 2. Transcribe audio to text using Speech-to-Text API
 * 3. Analyze pronunciation using audio analysis
 * 4. Evaluate fluency based on pauses, hesitations
 * 
 * TODO: Replace mock scoring with actual AI integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakingScoringService {

    // IELTS-style band scoring weights
    private static final double PRONUNCIATION_WEIGHT = 0.25;
    private static final double FLUENCY_WEIGHT = 0.25;
    private static final double VOCABULARY_WEIGHT = 0.25;
    private static final double GRAMMAR_WEIGHT = 0.25;

    /**
     * Score a speaking response
     * 
     * For now, this expects either:
     * - A transcript in userAnswer field
     * - An audio URL in audioResponseUrl field (for future audio processing)
     * 
     * @param response The user's response containing the speaking submission
     * @return The updated response with scores and feedback
     */
    public UserResponse scoreSpeakingResponse(UserResponse response) {
        String transcript = response.getUserAnswer();
        String audioUrl = response.getAudioResponseUrl();

        // If no transcript, try to transcribe from audio (mock for now)
        if ((transcript == null || transcript.trim().isEmpty()) && audioUrl != null) {
            transcript = transcribeAudio(audioUrl);
            response.setUserAnswer(transcript);
        }

        if (transcript == null || transcript.trim().isEmpty()) {
            response.setScore(java.math.BigDecimal.ZERO);
            response.setAiFeedback("No speaking response provided.");
            return response;
        }

        log.info("Scoring speaking response for question: {}", response.getQuestion().getId());

        // Calculate individual scores
        double pronunciationScore = evaluatePronunciation(transcript, audioUrl);
        double fluencyScore = evaluateFluency(transcript);
        double vocabularyScore = evaluateVocabulary(transcript);
        double grammarScore = evaluateGrammar(transcript);

        // Set component scores
        response.setPronunciationScore(java.math.BigDecimal.valueOf(pronunciationScore));
        response.setFluencyScore(java.math.BigDecimal.valueOf(fluencyScore));
        response.setVocabularyScore(java.math.BigDecimal.valueOf(vocabularyScore));
        response.setGrammarScore(java.math.BigDecimal.valueOf(grammarScore));

        // Calculate overall score (0-10 scale, similar to IELTS 9-band)
        double overallScore = (pronunciationScore * PRONUNCIATION_WEIGHT +
                fluencyScore * FLUENCY_WEIGHT +
                vocabularyScore * VOCABULARY_WEIGHT +
                grammarScore * GRAMMAR_WEIGHT);

        // Convert to points based on question points value
        double maxPoints = response.getQuestion().getPoints().doubleValue();
        response.setScore(java.math.BigDecimal.valueOf((overallScore / 10.0) * maxPoints));

        // Generate feedback
        response.setAiFeedback(generateFeedback(pronunciationScore, fluencyScore,
                vocabularyScore, grammarScore, transcript));

        response.setIsCorrect(overallScore >= 6.0); // Passing threshold (Band 6+)

        log.info("Speaking score: {} / {} (overall: {})", response.getScore(), maxPoints, overallScore);

        return response;
    }

    /**
     * Transcribe audio to text
     * Mock implementation - TODO: Replace with Speech-to-Text API (e.g., Google,
     * Whisper)
     */
    private String transcribeAudio(String audioUrl) {
        log.info("Transcribing audio from: {}", audioUrl);
        // In production, this would call a Speech-to-Text API
        return "[Audio transcription would appear here]";
    }

    /**
     * Evaluate pronunciation
     * Mock implementation - TODO: Replace with audio analysis AI
     */
    private double evaluatePronunciation(String transcript, String audioUrl) {
        // Without actual audio analysis, we approximate based on text
        double score = 6.0; // Base score

        if (audioUrl != null) {
            // With audio URL, we could do real pronunciation analysis
            // For now, give a slight bonus for providing audio
            score += 1.0;
        }

        // Check for phonetically challenging words being used
        String[] challengingWords = { "particularly", "characteristic", "pronunciation",
                "circumstances", "vocabulary", "communication",
                "differentiate", "specifically", "unfortunately" };

        for (String word : challengingWords) {
            if (transcript.toLowerCase().contains(word)) {
                score += 0.3;
            }
        }

        return Math.min(10.0, score);
    }

    /**
     * Evaluate fluency and coherence
     * Mock implementation - TODO: Replace with AI analysis
     */
    private double evaluateFluency(String transcript) {
        double score = 5.0; // Base score

        int wordCount = transcript.split("\\s+").length;
        int sentenceCount = transcript.split("[.!?]+").length;

        // Longer responses generally indicate fluency
        if (wordCount >= 150) {
            score += 2.0;
        } else if (wordCount >= 100) {
            score += 1.0;
        }

        // Check for filler words (too many = disfluency)
        String[] fillers = { "um", "uh", "like", "you know", "basically", "actually" };
        int fillerCount = 0;
        for (String filler : fillers) {
            if (transcript.toLowerCase().contains(filler)) {
                fillerCount++;
            }
        }

        if (fillerCount <= 2) {
            score += 1.5; // Minimal fillers
        } else if (fillerCount >= 5) {
            score -= 1.0; // Too many fillers
        }

        // Check for coherence markers
        String[] coherenceMarkers = { "firstly", "secondly", "on the other hand",
                "in my opinion", "for example", "in conclusion" };
        for (String marker : coherenceMarkers) {
            if (transcript.toLowerCase().contains(marker)) {
                score += 0.4;
            }
        }

        return Math.min(10.0, Math.max(0.0, score));
    }

    /**
     * Evaluate vocabulary (lexical resource)
     * Mock implementation - TODO: Replace with AI
     */
    private double evaluateVocabulary(String transcript) {
        String[] words = transcript.toLowerCase().split("\\s+");
        int wordCount = words.length;

        // Calculate lexical diversity
        long uniqueWords = java.util.Arrays.stream(words).distinct().count();
        double lexicalDiversity = wordCount > 0 ? (double) uniqueWords / wordCount : 0;

        double score = 4.0 + (lexicalDiversity * 4.0); // Base + diversity bonus

        // Check for topic-specific vocabulary
        String[] academicWords = { "significant", "influence", "aspect", "factor",
                "perspective", "approach", "concept", "issue",
                "benefit", "challenge", "opportunity", "impact" };

        for (String word : academicWords) {
            if (transcript.toLowerCase().contains(word)) {
                score += 0.25;
            }
        }

        // Check for collocations and idiomatic expressions
        String[] idioms = { "on the whole", "by and large", "in terms of",
                "at the end of the day", "to be honest", "as far as I know" };

        for (String idiom : idioms) {
            if (transcript.toLowerCase().contains(idiom)) {
                score += 0.5;
            }
        }

        return Math.min(10.0, score);
    }

    /**
     * Evaluate grammar
     * Mock implementation - TODO: Replace with NLP analysis
     */
    private double evaluateGrammar(String transcript) {
        double score = 6.0; // Base score

        int sentenceCount = transcript.split("[.!?]+").length;
        int wordCount = transcript.split("\\s+").length;

        // Check sentence variety
        double avgSentenceLength = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0;
        if (avgSentenceLength >= 8 && avgSentenceLength <= 20) {
            score += 1.5; // Good sentence variety
        }

        // Check for complex structures
        String[] complexStructures = { "if", "although", "because", "while",
                "when", "which", "that", "who",
                "would have", "could have", "might have" };

        int complexCount = 0;
        for (String structure : complexStructures) {
            if (transcript.toLowerCase().contains(structure)) {
                complexCount++;
            }
        }

        if (complexCount >= 5) {
            score += 2.0; // Good grammatical range
        } else if (complexCount >= 3) {
            score += 1.0;
        }

        return Math.min(10.0, score);
    }

    /**
     * Generate detailed feedback for speaking response
     */
    private String generateFeedback(double pronunciation, double fluency,
            double vocabulary, double grammar, String transcript) {
        StringBuilder feedback = new StringBuilder();

        // Overall summary
        double avg = (pronunciation + fluency + vocabulary + grammar) / 4.0;
        double ieltsEquivalent = (avg / 10.0) * 9.0; // Convert to IELTS band

        feedback.append("**Estimated Band Score: ").append(String.format("%.1f", ieltsEquivalent)).append("**\n\n");

        if (avg >= 8) {
            feedback.append("Outstanding performance! You demonstrate excellent speaking ability. ");
        } else if (avg >= 6) {
            feedback.append("Good performance. You communicate effectively with some areas for improvement. ");
        } else {
            feedback.append("Keep practicing! Focus on the areas highlighted below. ");
        }
        feedback.append("\n\n");

        // Pronunciation feedback
        feedback.append("**Pronunciation** (").append(String.format("%.1f", pronunciation)).append("/10): ");
        if (pronunciation >= 8) {
            feedback.append("Clear pronunciation with natural intonation.");
        } else if (pronunciation >= 6) {
            feedback.append("Generally clear. Work on stress and intonation patterns.");
        } else {
            feedback.append("Practice pronunciation of challenging sounds.");
        }
        feedback.append("\n\n");

        // Fluency feedback
        feedback.append("**Fluency & Coherence** (").append(String.format("%.1f", fluency)).append("/10): ");
        if (fluency >= 8) {
            feedback.append("Speaks fluently with well-organized ideas.");
        } else if (fluency >= 6) {
            feedback.append("Good flow. Reduce hesitations and filler words.");
        } else {
            feedback.append("Practice speaking without long pauses. Use discourse markers.");
        }
        feedback.append("\n\n");

        // Vocabulary feedback
        feedback.append("**Lexical Resource** (").append(String.format("%.1f", vocabulary)).append("/10): ");
        if (vocabulary >= 8) {
            feedback.append("Excellent range of vocabulary used naturally.");
        } else if (vocabulary >= 6) {
            feedback.append("Good vocabulary. Try using more idiomatic expressions.");
        } else {
            feedback.append("Expand your vocabulary with topic-specific words.");
        }
        feedback.append("\n\n");

        // Grammar feedback
        feedback.append("**Grammatical Range** (").append(String.format("%.1f", grammar)).append("/10): ");
        if (grammar >= 8) {
            feedback.append("Wide range of structures used accurately.");
        } else if (grammar >= 6) {
            feedback.append("Good grammar. Include more complex sentence structures.");
        } else {
            feedback.append("Focus on using conditionals and relative clauses.");
        }

        // Word count
        int wordCount = transcript.split("\\s+").length;
        feedback.append("\n\n**Response Length**: ").append(wordCount).append(" words");

        return feedback.toString();
    }
}
