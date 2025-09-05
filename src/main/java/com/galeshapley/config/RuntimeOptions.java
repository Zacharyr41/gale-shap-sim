package com.galeshapley.config;

/**
 * Runtime configuration options for algorithm execution and testing.
 * Provides configurable parameters that can be set during test execution.
 */
public class RuntimeOptions {
    private int maxIterations;
    private boolean enableDetailedLogging;
    private boolean trackIterationMetrics;
    
    private RuntimeOptions(Builder builder) {
        this.maxIterations = builder.maxIterations;
        this.enableDetailedLogging = builder.enableDetailedLogging;
        this.trackIterationMetrics = builder.trackIterationMetrics;
    }
    
    public int getMaxIterations() {
        return maxIterations;
    }
    
    public boolean isDetailedLoggingEnabled() {
        return enableDetailedLogging;
    }
    
    public boolean isTrackIterationMetrics() {
        return trackIterationMetrics;
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
        
        public RuntimeOptions build() {
            return new RuntimeOptions(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("RuntimeOptions{maxIterations=%d, detailedLogging=%s, trackIterations=%s}",
            maxIterations, enableDetailedLogging, trackIterationMetrics);
    }
}