package com.galeshapley.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration for agent preferences that can be either explicit or generated.
 * Supports both explicit preference lists and random generation configuration.
 */
public class PreferenceConfig {
    
    private final List<String> explicit;
    private final GeneratorConfig generator;
    
    @JsonCreator
    public PreferenceConfig(
            @JsonProperty("explicit") List<String> explicit,
            @JsonProperty("generator") GeneratorConfig generator) {
        
        if (explicit != null && generator != null) {
            throw new IllegalArgumentException("Cannot specify both explicit preferences and generator config");
        }
        if (explicit == null && generator == null) {
            throw new IllegalArgumentException("Must specify either explicit preferences or generator config");
        }
        
        this.explicit = explicit;
        this.generator = generator;
    }
    
    public boolean isExplicit() {
        return explicit != null;
    }
    
    public List<String> getExplicit() {
        return explicit;
    }
    
    public GeneratorConfig getGenerator() {
        return generator;
    }
    
    /**
     * Configuration for preference generation.
     */
    public static class GeneratorConfig {
        private final boolean random;
        private final Double emptySetProbability;
        private final Long seed;
        
        @JsonCreator
        public GeneratorConfig(
                @JsonProperty("random") Boolean random,
                @JsonProperty("emptySetProbability") Double emptySetProbability,
                @JsonProperty("seed") Long seed) {
            this.random = random != null ? random : false;
            this.emptySetProbability = emptySetProbability != null ? emptySetProbability : 0.0;
            this.seed = seed;
        }
        
        public boolean isRandom() {
            return random;
        }
        
        public double getEmptySetProbability() {
            return emptySetProbability;
        }
        
        public Long getSeed() {
            return seed;
        }
    }
    
    /**
     * Create explicit preference config from list.
     */
    public static PreferenceConfig explicit(List<String> preferences) {
        return new PreferenceConfig(preferences, null);
    }
    
    /**
     * Create random preference config.
     */
    public static PreferenceConfig random(double emptySetProbability, Long seed) {
        GeneratorConfig generator = new GeneratorConfig(true, emptySetProbability, seed);
        return new PreferenceConfig(null, generator);
    }
    
    /**
     * Create random preference config with no empty set.
     */
    public static PreferenceConfig random(Long seed) {
        return random(0.0, seed);
    }
}