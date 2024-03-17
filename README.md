# Retriever
> Eclipse plugin for model-driven reverse engineering

[![Build Pipeline](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/build.yml/badge.svg)](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/build.yml) [![Continual Improvement Process](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/quality.yml/badge.svg)](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/quality.yml) [![Continuous Product Integration](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/product.yml/badge.svg)](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/actions/workflows/product.yml)

Reverse engineering of software component models from [heterogeneous artifacts](#discoverers) of the software development process. For this purpose, [technology-specific rules](#rules) are used within Retriever to automatically extract static components of a system. For this purpose, the heterogeneous artifacts of a software project are first transformed into a model representation. Based on the model, Retriever applies mappings specified in rule artifacts to each model instance to identify components for the final software architecture model.

This component extraction supports base components, composite structures, interfaces, ports, and connectors. The candidate components are then used to generate the elements of the target software architecture model, which can then be used for quality prediction purposes in the [Palladio](https://www.palladio-simulator.com/) context. The extracted models are suitable for improving the understanding of existing software and enabling further quality analyses. Software performance, reliability, and maintenance analyses are already available as part of a complementary tool chain.

## Status
The rules engine is currently under active development. If you are interested in further information or would like to contribute your personal thoughts or requirements, please do not hesitate to contact us.

## Built With
Retriever is implemented with the [Eclipse Modeling Framework (EMF)](https://www.eclipse.org/modeling/emf/). It is thus provided as a plug-in for the [Eclipse platform](https://www.eclipse.org/eclipse/).

Source code based on EMF metamodels is generated during the build process and is therefore not added to the repository. To generate the source code, either the Maven build or the [EMF generation workflow](https://www.eclipse.org/modeling/emf/docs/2.x/tutorials/clibmod/clibmod_emf2.0.html#step2) must be run.

The rules for the model-to-model transformations are implemented in [Xtend](https://www.eclipse.org/xtend/), which can be compiled into Java-compatible source code.

### Discoverers
* [CSV Discoverer](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/blob/master/bundles/org.palladiosimulator.retriever.extraction.discoverers/src/org/palladiosimulator/retriever/extraction/discoverers/CsvDiscoverer.java)
* [Java Discoverer](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/blob/master/bundles/org.palladiosimulator.retriever.extraction.discoverers/src/org/palladiosimulator/retriever/extraction/discoverers/JavaDiscoverer.java)
* [JSON Discoverer](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/blob/master/bundles/org.palladiosimulator.retriever.extraction.discoverers/src/org/palladiosimulator/retriever/extraction/discoverers/JsonDiscoverer.java)
* [XML Discoverer](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/blob/master/bundles/org.palladiosimulator.retriever.extraction.discoverers/src/org/palladiosimulator/retriever/extraction/discoverers/XmlDiscoverer.java)
* [YAML Discoverer](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/blob/master/bundles/org.palladiosimulator.retriever.extraction.discoverers/src/org/palladiosimulator/retriever/extraction/discoverers/YamlDiscoverer.java)

### Rules
* [Spring Boot and Framework](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/tree/master/bundles/org.palladiosimulator.retriever.extraction.rules/src/org/palladiosimulator/retriever/extraction/rules/SpringRules.xtend)
* [Jakarta RESTful Web Services](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/tree/master/bundles/org.palladiosimulator.retriever.extraction.rules/src/org/palladiosimulator/retriever/extraction/rules/JaxRSRules.xtend)
* [Maven Project Object Model](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever/tree/master/bundles/org.palladiosimulator.retriever.extraction.rules/src/org/palladiosimulator/retriever/extraction/rules/MavenRules.xtend)

### Analyzer
* [Docker Vulnerability](https://github.com/FluidTrust/Palladio-ReverseEngineering-Docker-Vulnerability)

## Getting Started
### Preconditions
* [Installation of PCM Nightly](https://sdqweb.ipd.kit.edu/wiki/PCM_Installation#PCM_Nightly)
  * Requires [Java Development Kit 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) and [Eclipse 2022-12 Modeling Tools](https://www.eclipse.org/downloads/packages/release/2022-12/r/eclipse-modeling-tools)
  * [Install](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-124.htm) PCM Nightly from the [update site](https://updatesite.palladio-simulator.com/palladio-build-updatesite/nightly/)
  * Do not install the *deprecated* categories

### Installation
#### For Development
* Install the [Fluent Api Model Generator](https://github.com/PalladioSimulator/Palladio-Addons-FluentApiModelGenerator) from the [update site](https://updatesite.palladio-simulator.com/palladio-addons-fluentapimodelgenerator/nightly/)
* Install the [Xtend IDE](https://www.eclipse.org/xtend/download.html) from the [update site](https://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/)
* Keep the current installation and, if necessary, change the items to be installed so that they are compatible
* Check out [this repository](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Retriever.git) and [import the existing projects](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-importproject.htm) into Eclipse
* Make changes to the imported code in Eclipse
* Locally verify all projects in the repository with [Maven](https://maven.apache.org/): `mvn clean verify`
* Launch a new [Eclipse runtime application](https://help.eclipse.org/latest/topic/org.eclipse.pde.doc.user/guide/tools/launchers/eclipse_application_launcher.htm)

#### For Direct Use
* [Adding these update sites](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm), without any further installation:
  * The [update site](https://updatesite.palladio-simulator.com/palladio-addons-fluentapimodelgenerator/nightly/) for the [Fluent Api Model Generator](https://github.com/PalladioSimulator/Palladio-Addons-FluentApiModelGenerator)
* Install Retriever from the [update site](https://updatesite.palladio-simulator.com/palladio-reverseengineering-retriever/nightly/)
  * This will automatically install all the required dependencies

### Usage
* Create a new run configuration to launch Retriever
  * Specify the root directory of the project to be analyzed
  * Select the technology used there
  * Specify the directory for saving the PCM instances
* If the PCM instances were not saved in the workspace, they can be imported later

## Contributing
To contribute, please follow these steps:

1. Fork this repository.
2. Create a branch: `git checkout -b <branch_name>`.
3. Make your changes and commit them: `git commit -ma '<commit_message>'`
4. Push into the original branch: `git push origin <project_name>/<location>`
5. Create a new pull request.

Alternatively, you can read the GitHub documentation on how to [create a pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

## Links
* Palladio homepage: https://www.palladio-simulator.com/
* Comprehensive documentation: https://sdqweb.ipd.kit.edu/wiki/Palladio_Component_Model
* Issue tracker: https://palladio-simulator.atlassian.net/jira/
* Update site: https://updatesite.palladio-simulator.com/palladio-reverseengineering-retriever/nightly/
* Javadoc: https://updatesite.palladio-simulator.com/palladio-reverseengineering-retriever/nightly/javadoc/
* Deprecated build server: https://build.palladio-simulator.com/job/PalladioSimulator/job/Palladio-ReverseEngineering-Retriever/

## Licensing
The code in this project is licensed under the [EPL-2.0 License](LICENSE).
