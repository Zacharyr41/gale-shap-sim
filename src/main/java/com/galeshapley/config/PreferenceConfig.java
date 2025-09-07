package com.galeshapley.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.galeshapley.config.distribution.DistributionConfig;

import java.util.List;

/**
 * Configuration for agent preferences that can be either explicit or generated.
 * Supports both explicit preference lists and generation using various distributions.
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
     * Configuration for preference generation using distributions.
     */
    public static class GeneratorConfig {
        private final DistributionConfig distribution;
        
        @JsonCreator
        public GeneratorConfig(
                @JsonProperty("distribution") DistributionConfig distribution) {
            if (distribution == null) {
                throw new IllegalArgumentException("Generator config must specify a distribution");
            }
            this.distribution = distribution;
        }
        
        public DistributionConfig getDistribution() {
            return distribution;
        }
    }
    
    /**
     * Create explicit preference config from list.
     */
    public static PreferenceConfig explicit(List<String> preferences) {
        return new PreferenceConfig(preferences, null);
    }
    
    /**
     * Create preference config with specified distribution.
     */
    public static PreferenceConfig withDistribution(DistributionConfig distribution) {
        GeneratorConfig generator = new GeneratorConfig(distribution);
        return new PreferenceConfig(null, generator);
    }
}