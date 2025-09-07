package com.galeshapley.config.distribution;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

/**
 * Configuration for correlated preference distribution.
 * Allows certain agents to appear more frequently in top positions
 * of preference orderings, modeling real-world popularity bias.
 */
public class CorrelatedDistributionConfig extends DistributionConfig {
    
    @JsonProperty("popularityBias")
    private List<PopularityBias> popularityBias = new ArrayList<>();
    
    @JsonProperty("topPercentage")
    private Double topPercentage = 30.0; // Default to top 30% of preferences
    
    @Override
    public String getType() {
        return "correlated";
    }
    
    public List<PopularityBias> getPopularityBias() {
        return popularityBias;
    }
    
    public void setPopularityBias(List<PopularityBias> popularityBias) {
        this.popularityBias = popularityBias;
    }
    
    public Double getTopPercentage() {
        return topPercentage;
    }
    
    public void setTopPercentage(Double topPercentage) {
        this.topPercentage = topPercentage;
    }
    
    @Override
    public void validate() {
        super.validate();
        
        if (topPercentage != null && (topPercentage <= 0 || topPercentage > 100)) {
            throw new IllegalArgumentException("topPercentage must be between 0 and 100");
        }
        
        if (popularityBias != null) {
            for (PopularityBias bias : popularityBias) {
                if (bias.getWeight() <= 0) {
                    throw new IllegalArgumentException("Popularity bias weights must be positive");
                }
            }
        }
    }
    
    /**
     * Represents a popularity bias for a specific agent.
     */
    public static class PopularityBias {
        @JsonProperty("agent")
        private String agent;
        
        @JsonProperty("weight")
        private Double weight = 1.0;
        
        public String getAgent() {
            return agent;
        }
        
        public void setAgent(String agent) {
            this.agent = agent;
        }
        
        public Double getWeight() {
            return weight;
        }
        
        public void setWeight(Double weight) {
            this.weight = weight;
        }
        
        @Override
        public String toString() {
            return "PopularityBias{" +
                    "agent='" + agent + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "CorrelatedDistributionConfig{" +
                "popularityBias=" + popularityBias +
                ", topPercentage=" + topPercentage +
                ", emptySetProbability=" + emptySetProbability +
                ", seed=" + seed +
                '}';
    }
}