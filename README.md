# Rule Engine for Reverse Engineering
Reverse engineering of software component models from [heterogeneous artifacts](#extractors) of the software development process. For this purpose, [technology-specific rules](#rules) are used within the rule engine to automatically extract static components of a system. For this purpose, the heterogeneous artifacts of a software project are first transformed into a model representation. Based on the model, the rule engine applies mappings specified in rule artifacts to each model instance to identify components for the final software architecture model.

This component extraction supports base components, composite structures, interfaces, ports, and connectors. The candidate components are then used to generate the elements of the target software architecture model, which can then be used for quality prediction purposes in the [Palladio](https://www.palladio-simulator.com/) context. The extracted models are suitable for improving the understanding of existing software and enabling further quality analyses. Software performance, reliability, and maintenance analyses are already available as part of a complementary tool chain.

## Status
The rules engine is currently under active development. If you are interested in further information or would like to contribute your personal thoughts or requirements, please do not hesitate to contact us.

## Extractors
* [JDT-Based Java Extractor](https://github.com/PalladioSimulator/Palladio-Supporting-EclipseJavaDevelopmentTools)
* [Docker File Extractor](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-Docker)

## Rules
* [Spring Boot and Framework](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-SoMoX-RuleEngine/tree/master/bundles/org.palladiosimulator.somox.analyzer.rules.spring)
* [Jakarta RESTful Web Services](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-SoMoX-RuleEngine/tree/master/bundles/org.palladiosimulator.somox.analyzer.rules.jax_rs)

## Getting Started
### Preconditions
* [Installation of PCM Nightly](https://sdqweb.ipd.kit.edu/wiki/PCM_Installation#PCM_Nightly)
  * Requires [Java Development Kit 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) and [Eclipse 2020-12 Modeling Tools](https://www.eclipse.org/downloads/packages/release/2020-12/r/eclipse-modeling-tools)
  * [Install](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-124.htm) PCM Nightly from the [update site](https://updatesite.palladio-simulator.com/palladio-build-updatesite/nightly/)
  * Do not install the *deprecated* categories

### Installation
#### For Development
* Install the [Fluent Api Model Generator](https://github.com/PalladioSimulator/Palladio-Addons-FluentApiModelGenerator) from the [update site](https://updatesite.palladio-simulator.com/palladio-addons-fluentapimodelgenerator/nightly/)
* Install the [JDT-Based Java Extractor](https://github.com/PalladioSimulator/Palladio-Supporting-EclipseJavaDevelopmentTools) from the [update site](https://updatesite.palladio-simulator.com/palladio-supporting-eclipsejavadevelopmenttools/nightly/)
* Install the [SoMoX Core Feature](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-SoMoX-JaMoPP) from the [update site](https://updatesite.palladio-simulator.com/palladio-reverseengineering-somox-jamopp/nightly/)
* Check out [this repository](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-SoMoX-RuleEngine.git) and [import the existing projects](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-importproject.htm) into Eclipse
* Make changes to the imported code in Eclipse
* Locally verify all projects in the repository with [Maven](https://maven.apache.org/): `mvn clean verify`
* Launch a new [Eclipse runtime application](https://help.eclipse.org/latest/topic/org.eclipse.pde.doc.user/guide/tools/launchers/eclipse_application_launcher.htm)

#### For Usage
* [Adding these update sites](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm), without any further installation:
  * The [update site](https://updatesite.palladio-simulator.com/palladio-addons-fluentapimodelgenerator/nightly/) for the [Fluent Api Model Generator](https://github.com/PalladioSimulator/Palladio-Addons-FluentApiModelGenerator)
  * The [update site](https://updatesite.palladio-simulator.com/palladio-supporting-eclipsejavadevelopmenttools/nightly/) for the [JDT-Based Java Extractor](https://github.com/PalladioSimulator/Palladio-Supporting-EclipseJavaDevelopmentTools)
  * The [update site](https://updatesite.palladio-simulator.com/palladio-reverseengineering-somox-jamopp/nightly/) for the [SoMoX Core Feature](https://github.com/PalladioSimulator/Palladio-ReverseEngineering-SoMoX-JaMoPP)
* Install this Rule Engine from the [update site](https://updatesite.palladio-simulator.com/palladio-reverseengineering-somox-ruleengine/nightly/)
  * This will automatically install all the required dependencies

### Execution
* Create a new run configuration to launch the rule engine
  * Specify the root directory of the project to be analyzed
  * Select the technology used there
  * Specify the directory for saving the PCM instances
* If the PCM instances were not saved in the workspace, they can be imported later
