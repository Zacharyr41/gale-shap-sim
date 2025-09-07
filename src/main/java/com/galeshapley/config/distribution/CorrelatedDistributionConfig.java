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
                bias.validate();
            }
        }
    }
    
    /**
     * Represents a popularity bias for specific agents or agent ranges.
     */
    public static class PopularityBias {
        @JsonProperty("agent")
        private String agent;
        
        @JsonProperty("agentRange")
        private AgentRange agentRange;
        
        @JsonProperty("weight")
        private Double weight = 1.0;
        
        public String getAgent() {
            return agent;
        }
        
        public void setAgent(String agent) {
            this.agent = agent;
        }
        
        public AgentRange getAgentRange() {
            return agentRange;
        }
        
        public void setAgentRange(AgentRange agentRange) {
            this.agentRange = agentRange;
        }
        
        public Double getWeight() {
            return weight;
        }
        
        public void setWeight(Double weight) {
            this.weight = weight;
        }
        
        /**
         * Check if this bias applies to a specific agent ID.
         */
        public boolean appliesTo(String agentId) {
            if (agent != null) {
                return agent.equals(agentId);
            }
            if (agentRange != null) {
                return agentRange.contains(agentId);
            }
            return false;
        }
        
        public void validate() {
            if (agent != null && agentRange != null) {
                throw new IllegalArgumentException("PopularityBias cannot specify both 'agent' and 'agentRange'");
            }
            if (agent == null && agentRange == null) {
                throw new IllegalArgumentException("PopularityBias must specify either 'agent' or 'agentRange'");
            }
            if (weight <= 0) {
                throw new IllegalArgumentException("PopularityBias weight must be positive");
            }
        }
        
        @Override
        public String toString() {
            if (agent != null) {
                return "PopularityBias{agent='" + agent + "', weight=" + weight + '}';
            } else if (agentRange != null) {
                return "PopularityBias{agentRange=" + agentRange + ", weight=" + weight + '}';
            }
            return "PopularityBias{weight=" + weight + '}';
        }
    }
    
    /**
     * Represents a range of agent IDs.
     */
    public static class AgentRange {
        @JsonProperty("start")
        private String start;
        
        @JsonProperty("end")
        private String end;
        
        public String getStart() {
            return start;
        }
        
        public void setStart(String start) {
            this.start = start;
        }
        
        public String getEnd() {
            return end;
        }
        
        public void setEnd(String end) {
            this.end = end;
        }
        
        /**
         * Check if an agent ID falls within this range.
         * Assumes agent IDs have a consistent format (e.g., e1, e2, ..., e10).
         */
        public boolean contains(String agentId) {
            if (start == null || end == null) {
                return false;
            }
            
            // Extract numeric parts for comparison
            // Assumes format like "e1", "p10", etc. where prefix is consistent
            try {
                String startPrefix = extractPrefix(start);
                String endPrefix = extractPrefix(end);
                String agentPrefix = extractPrefix(agentId);
                
                // All must have the same prefix
                if (!startPrefix.equals(endPrefix) || !startPrefix.equals(agentPrefix)) {
                    return false;
                }
                
                int startNum = extractNumber(start);
                int endNum = extractNumber(end);
                int agentNum = extractNumber(agentId);
                
                return agentNum >= startNum && agentNum <= endNum;
                
            } catch (NumberFormatException e) {
                // Fallback to string comparison if numeric extraction fails
                return agentId.compareTo(start) >= 0 && agentId.compareTo(end) <= 0;
            }
        }
        
        private String extractPrefix(String agentId) {
            int i = 0;
            while (i < agentId.length() && !Character.isDigit(agentId.charAt(i))) {
                i++;
            }
            return i > 0 ? agentId.substring(0, i) : "";
        }
        
        private int extractNumber(String agentId) {
            int i = 0;
            while (i < agentId.length() && !Character.isDigit(agentId.charAt(i))) {
                i++;
            }
            if (i < agentId.length()) {
                return Integer.parseInt(agentId.substring(i));
            }
            throw new NumberFormatException("No number found in agent ID: " + agentId);
        }
        
        @Override
        public String toString() {
            return "AgentRange{start='" + start + "', end='" + end + "'}";
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