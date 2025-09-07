package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.RuntimeOptions;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.ConsoleObserver;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

class CommandOutputIntegrationTest {
    
    @Test
    void shouldProduceExpectedOutputForAsymmetricMatchingConfig() throws IOException {
        // Given: Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Load the asymmetric matching configuration
            SimulationConfigLoader loader = new SimulationConfigLoader();
            SimulationConfig config = loader.loadFromFile("src/test/resources/asymmetric-matching-config.yaml");
            
            RuntimeOptions runtimeOptions = RuntimeOptions.builder()
                .maxIterations(10)
                .enableDetailedLogging(false)
                .trackIterationMetrics(true)
                .build();
            
            GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
                config.getProposerPreferences(),
                config.getProposeePreferences(),
                config.getEmptySetPreferences()
            );
            
            ConsoleObserver consoleObserver = new ConsoleObserver(runtimeOptions.isDetailedLoggingEnabled());
            StatisticsObserver statisticsObserver = new StatisticsObserver();
            
            algorithm.addObserver(consoleObserver);
            algorithm.addObserver(statisticsObserver);
            
            // When: Execute the algorithm
            GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute(runtimeOptions);
            
            // Print statistics
            if (runtimeOptions.isTrackIterationMetrics()) {
                System.out.println("\n=== Statistics ===");
                System.out.println(statisticsObserver.getStatistics());
            }
            
        } finally {
            System.setOut(originalOut);
        }
        
        String output = outputStream.toString();
        
        // Then: Verify expected output structure and content
        assertThat(output)
            .contains("=== Gale-Shapley Algorithm Started ===")
            .contains("Proposers: 4")
            .contains("Proposees: 2")  // Should show correct count (not 3)
            .contains("Anna accepts")
            .contains("Beth accepts")
            .contains("=== Algorithm Complete ===")
            .contains("Total iterations: 1")
            .contains("Final Matching:")
            .contains("Ben ↔ Anna")
            .contains("Carl ↔ Beth")
            .contains("Unmatched Proposers:")
            .contains("- Adam")
            .contains("- Dave")
            .contains("=== Statistics ===");
        
        // Verify new improved statistics section contains expected metrics
        assertThat(output)
            .contains("Algorithm Execution Statistics:")
            .contains("📊 PROPOSAL ACTIVITY:")
            .contains("Meaningful Proposals Made:")
            .contains("Proposals Accepted:")
            .contains("Total Rejections:")
            .contains("Broken Engagements:")
            .contains("Direct Rejections:")
            .contains("🔄 ALGORITHM MECHANICS:")
            .contains("Total Decision Points:")
            .contains("📈 COMPETITION METRICS:")
            .contains("Avg Proposals per Proposer:")
            .contains("Avg Rejections per Proposer:")
            .contains("Avg Proposals per Proposee:")
            .contains("⏱️  PERFORMANCE:")
            .contains("Execution Time:");
        
        // Verify no unmatched proposees (all 2 proposees should be matched)
        assertThat(output).doesNotContain("Unmatched Proposees:");
        
        // Verify specific expected values  
        assertThat(output).contains("Avg Proposals per Proposee: 2.00");
    }
}