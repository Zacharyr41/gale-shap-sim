package com.galeshapley.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Runtime configuration options for algorithm execution and testing.
 * Provides configurable parameters that can be set during test execution.
 * 
 * Configuration can be set via:
 * - application.yml properties under 'galeshapley' prefix
 * - Environment variables: GALESHAPLEY_MAXITERATIONS, GALESHAPLEY_ENABLEDETAILEDLOGGING, etc.
 * - Command line arguments: --galeshapley.maxIterations=1000, --galeshapley.enableDetailedLogging=true
 */
@ConfigurationProperties(prefix = "galeshapley")
public class RuntimeOptions {
    private int maxIterations = Integer.MAX_VALUE;
    private boolean enableDetailedLogging = false;
    private boolean trackIterationMetrics = true;
    private Long globalSeed = null;
    
    public RuntimeOptions() {
    }
    
    private RuntimeOptions(Builder builder) {
        this.maxIterations = builder.maxIterations;
        this.enableDetailedLogging = builder.enableDetailedLogging;
        this.trackIterationMetrics = builder.trackIterationMetrics;
        this.globalSeed = builder.globalSeed;
    }
    
    public int getMaxIterations() {
        return maxIterations;
    }
    
    public void setMaxIterations(int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max iterations must be positive");
        }
        this.maxIterations = maxIterations;
    }
    
    public boolean isDetailedLoggingEnabled() {
        return enableDetailedLogging;
    }
    
    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
    }
    
    public boolean isTrackIterationMetrics() {
        return trackIterationMetrics;
    }
    
    public void setTrackIterationMetrics(boolean trackIterationMetrics) {
        this.trackIterationMetrics = trackIterationMetrics;
    }
    
    public Long getGlobalSeed() {
        return globalSeed;
    }
    
    public void setGlobalSeed(Long globalSeed) {
        this.globalSeed = globalSeed;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static RuntimeOptions defaultOptions() {
        return builder().build();
    }
    
    public static class Builder {
        private int maxIterations = Integer.MAX_VALUE;
        private boolean enableDetailedLogging = false;
        private boolean trackIterationMetrics = true;
        private Long globalSeed = null;
        
        public Builder maxIterations(int maxIterations) {
            if (maxIterations <= 0) {
                throw new IllegalArgumentException("Max iterations must be positive");
            }
            this.maxIterations = maxIterations;
            return this;
        }
        
        public Builder enableDetailedLogging(boolean enableDetailedLogging) {
            this.enableDetailedLogging = enableDetailedLogging;
            return this;
        }
        
        public Builder trackIterationMetrics(boolean trackIterationMetrics) {
            this.trackIterationMetrics = trackIterationMetrics;
            return this;
        }
        
        public Builder globalSeed(Long globalSeed) {
            this.globalSeed = globalSeed;
            return this;
        }
        
        public RuntimeOptions build() {
            return new RuntimeOptions(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("RuntimeOptions{maxIterations=%d, detailedLogging=%s, trackIterations=%s, globalSeed=%s}",
            maxIterations, enableDetailedLogging, trackIterationMetrics, globalSeed);
    }
}