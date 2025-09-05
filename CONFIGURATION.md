# Configuration Guide

This application uses Spring Boot for configuration management, allowing you to configure runtime options through multiple methods.

## Configuration Methods

### 1. Application Properties File (application.yml)

The default configuration is located at `src/main/resources/application.yml`:

```yaml
galeshapley:
  maxIterations: 2147483647
  enableDetailedLogging: false
  trackIterationMetrics: true
  globalSeed: null
```

### 2. Environment Variables

You can override any configuration property using environment variables with the `GALESHAPLEY_` prefix:

```bash
export GALESHAPLEY_MAXITERATIONS=1000
export GALESHAPLEY_ENABLEDETAILEDLOGGING=true
export GALESHAPLEY_TRACKITERATIONMETRICS=false
export GALESHAPLEY_GLOBALSEED=12345
```

### 3. Command Line Arguments

Spring Boot properties can be set via command line arguments:

```bash
# Using Maven exec plugin
mvn exec:java -Dexec.args="--galeshapley.maxIterations=1000 --galeshapley.enableDetailedLogging=true"

# Using the JAR file
java -jar target/gale-shapley.jar --galeshapley.maxIterations=1000 --galeshapley.enableDetailedLogging=true

# You can also still pass the config file as the first non-Spring argument
java -jar target/gale-shapley.jar config/my-simulation.yaml --galeshapley.maxIterations=1000
```

## Configuration Properties

| Property | Environment Variable | Type | Default | Description |
|----------|---------------------|------|---------|-------------|
| `galeshapley.maxIterations` | `GALESHAPLEY_MAXITERATIONS` | `int` | `Integer.MAX_VALUE` | Maximum number of algorithm iterations |
| `galeshapley.enableDetailedLogging` | `GALESHAPLEY_ENABLEDETAILEDLOGGING` | `boolean` | `false` | Enable detailed logging during execution |
| `galeshapley.trackIterationMetrics` | `GALESHAPLEY_TRACKITERATIONMETRICS` | `boolean` | `true` | Track and display iteration metrics |
| `galeshapley.globalSeed` | `GALESHAPLEY_GLOBALSEED` | `Long` | `null` | Seed for random number generation (null uses system time) |

## Priority Order

Spring Boot applies configuration properties in the following order (later sources override earlier ones):

1. Default values in `application.yml`
2. Environment variables
3. Command line arguments

## Examples

### Example 1: Running with Environment Variables
```bash
export GALESHAPLEY_MAXITERATIONS=500
export GALESHAPLEY_ENABLEDETAILEDLOGGING=true
mvn exec:java
```

### Example 2: Running with Command Line Arguments
```bash
mvn exec:java -Dexec.args="--galeshapley.maxIterations=1000 --galeshapley.globalSeed=42"
```

### Example 3: Custom Config File with Runtime Options
```bash
java -jar target/gale-shapley.jar my-config.yaml --galeshapley.enableDetailedLogging=true
```

### Example 4: Using Multiple Configuration Methods
```bash
# Environment variable (will be overridden by command line)
export GALESHAPLEY_MAXITERATIONS=1000

# Command line argument (takes precedence)
java -jar target/gale-shapley.jar --galeshapley.maxIterations=500 --galeshapley.trackIterationMetrics=false
```

The final configuration will be: `maxIterations=500`, `trackIterationMetrics=false`, other values from defaults or environment.