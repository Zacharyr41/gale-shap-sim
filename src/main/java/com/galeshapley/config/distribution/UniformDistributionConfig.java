package com.galeshapley.config.distribution;

/**
 * Configuration for uniform random preference distribution.
 * This is the simplest distribution where all permutations are equally likely.
 */
public class UniformDistributionConfig extends DistributionConfig {
    
    @Override
    public String getType() {
        return "uniform";
    }
    
    @Override
    public String toString() {
        return "UniformDistributionConfig{" +
                "emptySetProbability=" + emptySetProbability +
                ", seed=" + seed +
                '}';
    }
}