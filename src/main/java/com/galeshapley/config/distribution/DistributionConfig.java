package com.galeshapley.config.distribution;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for preference distribution configurations.
 * Supports different types of preference generation strategies.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UniformDistributionConfig.class, name = "uniform"),
    @JsonSubTypes.Type(value = CorrelatedDistributionConfig.class, name = "correlated")
})
public abstract class DistributionConfig {
    
    @JsonProperty("emptySetProbability")
    protected Double emptySetProbability = 0.0;
    
    @JsonProperty("seed")
    protected Long seed;
    
    public Double getEmptySetProbability() {
        return emptySetProbability != null ? emptySetProbability : 0.0;
    }
    
    public void setEmptySetProbability(Double emptySetProbability) {
        this.emptySetProbability = emptySetProbability;
    }
    
    public Long getSeed() {
        return seed;
    }
    
    public void setSeed(Long seed) {
        this.seed = seed;
    }
    
    /**
     * Get the type identifier for this distribution.
     */
    public abstract String getType();
    
    /**
     * Validate the configuration.
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (emptySetProbability != null && (emptySetProbability < 0 || emptySetProbability > 1)) {
            throw new IllegalArgumentException("emptySetProbability must be between 0 and 1");
        }
    }
}