# Palladio Reverse Engineering Retriever GitHub Action

## Overview

This GitHub Action is designed to reverse-engineer a project's source code into a Palladio Component Model (PCM) and upload the PCM as an artifact named `retriever`.
In addition to that further useful information and measurements on the project are gathered and collected in a markdown report.
This action is intended to be used to analyze projects in a reusable and automatic manner.

## How It Works

The action performs several steps to generate the PCM and the report:

1. **Set Up JDK 17:**
   Ensures Java Development Kit 17 is available for the action to run.
   Required to execute the retriever eclipse plugin.
2. **Create Temporary Directory:**
   Prepares a workspace for the retriever's output and intermediate files of the report.
3. **Gather Retriever Information:**
   Collects and records information about the retriever version and execution date.
4. **Gather Git Repository Information:**
   Retrieves and logs details about the git repository, including the URL, branch, and latest commit.
   This is done for all repositories of the analyzed project in case it is a multi repository project.
5. **Gather System Information:**
   Installs `neofetch` to fetch and log detailed system information of the GitHub runner that executes the action.
6. **Perform Code Analysis:**
   Uses `cloc` to analyze the source code, providing a summary of languages used, lines of code and comments.
7. **Execution Timing:**
   This step executes the Retriever and measures the runtime of the reverse engineering process.
   Depending on the input value of the parameter `benchmark`, the runtime is either simply measured using `time` or benchmarked using `hyperfine`.
   In case of the benchmark, 3 warmup cycles are performed after which 10 runs are executed to measure the descriptive statistics of the runtime.  
8. **Combine and Clean Up Files:** 
   Aggregates all collected information into a single `README.md` file and cleans up intermediate files.
9. **Upload Analysis Results:** 
   The final PCM and analysis report are packaged and uploaded as an artifact called `retriever`.

## Inputs

The action requires the following inputs:

| Input     | Required/Optional | Description                                                                 | Default Value     |
|----------------|-------------------|-----------------------------------------------------------------------------|-------------------|
| source_path    | Required          | The project location to reverse-engineer.                                   | Root directory    |
| rules*          | Required          | A comma-separated list of rules for reverse-engineering.                    | Maven and Spring              |
| rules_path     | Optional          | Location of additional project-specific rules.                              | Root directory                |
| benchmark      | Optional          | Whether to use hyperfine for benchmarking the retriever's execution.        | false             |

*The following `rules` are currently supported:
- Maven Project Object Model: `org.palladiosimulator.retriever.extraction.rules.maven`
- Spring Boot and Framework: `org.palladiosimulator.retriever.extraction.rules.spring`
- Jakarta RESTful Web Services: `org.palladiosimulator.retriever.extraction.rules.jax_rs`

## Output

The output artifact, named `retriever` contains the Palladio Component Model (PCM) and the markdown report `README.md` containing the gathered information and measurements.

## Example Usage

This is a sample workflow that uses the retriever action. 
Add this `.yml` file to the workflow folder `./github/workflows/` of your GitHub project:

```yaml
name: Reverse Engineering

jobs:
  ReverseEngineering:
    name: Reverse Engineering
    runs-on: ubuntu-latest
    permissions:
      actions: read
      contents: read
    steps:
    - name: Checkout Repository
      uses: actions/checkout@v4

    - name: Run Rule Engine
      uses: PalladioSimulator/Palladio-ReverseEngineering-Retriever@main
      with:
        source_path: 'src/my_project'
        benchmark: 'true'
        rules: "org.palladiosimulator.retriever.extraction.rules.maven,org.palladiosimulator.retriever.extraction.rules.spring"
```

This example checks out the repository and runs the retriever action on the `src/my_project` directory with the specified rules.
Here, benchmarking is enabled to provide detailed information about the execution time.
