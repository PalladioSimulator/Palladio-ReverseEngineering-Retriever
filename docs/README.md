# Palladio Reverseengineering Retriever GitHub Action

## Overview

This GitHub Action, named "Run Retriever," is designed to reverse-engineer a project's source code into a Palladio Component Model (PCM) and upload the PCM as an artifact named `retriever`. Specifically, it targets GitHub repositories, making it an essential tool for developers looking to analyze and understand the architecture of their projects in a model-driven manner. The action outputs the PCM to the specified `source_path`, allowing for a detailed examination of the project structure.

## How It Works

The action performs several steps to generate the PCM:

1. **Set Up JDK 17:** Ensures Java Development Kit 17 is available for the action to run.
2. **Create Temporary Directory:** Prepares a workspace for the retriever's output and intermediate files.
3. **Gather Retriever Info:** Collects and records information about the Retriever tool's version and execution date.
4. **Gather Git Repository Info:** Retrieves and logs details about the Git repository, including the URL, branch, and latest commit.
5. **System Information:** Installs `neofetch` to fetch and log detailed system information where the action is executed.
6. **Code Analysis:** Uses `cloc` to analyze the source code, providing a summary of languages used, lines of code, and comments.
7. **Execution Timing:** Optionally benchmarks the Retriever execution using `hyperfine` or reports the execution time using `time`.
8. **Combine and Clean Up Files:** Aggregates all collected information into a single README.md file and cleans up intermediate files.
9. **Upload Analysis Results:** The final PCM and analysis report are packaged and uploaded as an artifact.

## Inputs

The action requires the following inputs:

- **source_path:** The project location to reverse-engineer. It's required and defaults to the root directory.
- **rules:** A comma-separated list of rules for reverse-engineering. Required.
- **rules_path:** Location of additional project-specific rules. Optional.
- **benchmark:** Whether to use Hyperfine for benchmarking the retriever's execution. Optional and defaults to false.

## Output

The output artifact, named `retriever`, contains the Palladio Component Model (PCM) at the specified `source_path`. For example, if `source_path=MyProject/Repository`, the PCM will be located at `[retriever]/MyProject/Repository/repository.pcm`.

## Example Usage

```yaml
jobs:
  analyze_project:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: your-username/run-retriever@v1
        with:
          source_path: "src/my_project"
          rules: "org.palladiosimulator.retriever.extraction.rules.maven,org.palladiosimulator.retriever.extraction.rules.spring"
          rules_path: "config/rules"
          benchmark: "true"
```

This example checks out a project and runs the Retriever action on the `src/my_project` directory with specified rules and a custom rules path. Benchmarking is enabled to provide detailed execution timing.