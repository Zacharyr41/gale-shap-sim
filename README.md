# Gale-Shapley Algorithm Simulator

A Java-based simulator for the Gale-Shapley stable matching algorithm with support for configuration via YAML files and comprehensive observation capabilities.

## Features

- **Object-Oriented Design**: Clean separation of concerns with models for Proposers, Proposees, Preferences, and Matchings
- **YAML Configuration**: Easy configuration of simulation scenarios using YAML files
- **Observer Pattern**: Monitor algorithm execution with built-in console and statistics observers
- **Test-Driven Development**: Comprehensive unit tests using JUnit 5
- **Extensible Architecture**: Easy to add constraints and edge cases in future iterations

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/galeshapley/
│   │       ├── Main.java                 # Entry point
│   │       ├── model/                    # Domain models
│   │       │   ├── Agent.java
│   │       │   ├── Proposer.java
│   │       │   ├── Proposee.java
│   │       │   ├── PreferenceList.java
│   │       │   └── Matching.java
│   │       ├── algorithm/                # Algorithm implementation
│   │       │   └── GaleShapleyAlgorithm.java
│   │       ├── config/                   # Configuration handling
│   │       │   ├── SimulationConfig.java
│   │       │   ├── SimulationConfigLoader.java
│   │       │   └── YamlConfig.java
│   │       └── observer/                 # Algorithm observers
│   │           ├── AlgorithmObserver.java
│   │           ├── ConsoleObserver.java
│   │           └── StatisticsObserver.java
│   └── resources/
│       └── example-config.yaml           # Example configuration
└── test/
    └── java/
        └── com/galeshapley/              # Unit tests
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Running the Simulator

### Basic Usage

#### Using the example configuration:

```bash
mvn exec:java -Dexec.mainClass="com.galeshapley.Main"
```

#### Using a custom YAML configuration:

```bash
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="path/to/your/config.yaml"
```

### CLI Output and Capturing Results

The simulator provides detailed console output during execution and comprehensive statistics upon completion.

#### Example Output

When running with verbose output enabled, you'll see:

```
Loading configuration from: src/main/resources/example-config.yaml

=== Gale-Shapley Algorithm Started ===
Proposers: 4
Proposees: 4

--- Iteration 1 ---
  Alice proposes to Eve
  ✓ Eve accepts Alice
  Bob proposes to Fiona
  ✓ Fiona accepts Bob
  Charlie proposes to Grace
  ✓ Grace accepts Charlie
  David proposes to Helen
  ✓ Helen accepts David

=== Algorithm Complete ===
Total iterations: 1

Final Matching:
  Alice ↔ Eve
  Bob ↔ Fiona
  Charlie ↔ Grace
  David ↔ Helen

=== Statistics ===
Statistics{
  Total Proposals: 4
  Total Acceptances: 4
  Total Rejections: 0
  Avg Proposals/Proposer: 1.00
  Avg Rejections/Proposer: 0.00
  Avg Proposals Received/Proposee: 1.00
  Execution Time: 12 ms
}
```

#### Capturing Output to File

To capture the complete simulation output to a file:

```bash
# Capture all output (stdout and stderr)
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" > simulation_results.txt 2>&1

# Capture only the main output
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" > simulation_results.txt

# Capture output while still seeing it in console
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" | tee simulation_results.txt
```

#### Automated Analysis

You can extract specific metrics from the output using standard Unix tools:

```bash
# Extract just the final statistics
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" 2>/dev/null | grep -A 10 "=== Statistics ==="

# Extract total iterations
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" 2>/dev/null | grep "Total iterations:" | awk '{print $3}'

# Extract execution time
mvn exec:java -Dexec.mainClass="com.galeshapley.Main" -Dexec.args="config.yaml" 2>/dev/null | grep "Execution Time:" | awk '{print $3, $4}'
```

#### Integration Testing

To run the complete integration test suite that validates YAML-to-results functionality:

```bash
# Run all integration tests
mvn test -Dtest=EndToEndIntegrationTest

# Run specific integration test scenario
mvn test -Dtest=EndToEndIntegrationTest#shouldRunCompleteSimulationFromYamlToMetrics
```

## YAML Configuration Format

```yaml
simulation:
  proposers:
    - id: m1
      name: Alice
    - id: m2
      name: Bob
  proposees:
    - id: w1
      name: Eve
    - id: w2
      name: Fiona
  proposerPreferences:
    m1: [w1, w2]
    m2: [w2, w1]
  proposeePreferences:
    w1: [m2, m1]
    w2: [m1, m2]
```

## Key Components

### Models
- **Agent**: Base class for participants in the matching
- **Proposer**: Agents who propose in the algorithm
- **Proposee**: Agents who receive proposals
- **PreferenceList**: Ordered preferences for each agent
- **Matching**: Represents the current state of matchings

### Algorithm
- **GaleShapleyAlgorithm**: Core implementation of the stable matching algorithm
- Supports observers for monitoring execution
- Returns results including final matching and iteration count

### Configuration
- **SimulationConfig**: Internal configuration representation
- **SimulationConfigLoader**: Loads configuration from YAML files
- **YamlConfig**: YAML-specific data structures

### Observers
- **AlgorithmObserver**: Interface for observing algorithm events
- **ConsoleObserver**: Prints algorithm progress to console
- **StatisticsObserver**: Collects statistics about the execution

## Future Enhancements

- Support for incomplete preference lists
- Constraints on matching (e.g., capacity constraints)
- Multiple stable matching exploration
- Visualization of the matching process
- Performance optimization for large-scale simulations
- REST API for remote simulation execution

## Dependencies

- Jackson (for YAML parsing)
- JUnit 5 (for testing)
- AssertJ (for fluent assertions)
- Mockito (for mocking in tests)
