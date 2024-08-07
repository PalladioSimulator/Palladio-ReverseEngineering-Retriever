package org.palladiosimulator.retriever.extraction.rules

import java.nio.file.Path
import java.util.Optional
import java.util.Set
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler
import java.net.URLClassLoader
import java.io.File
import java.util.HashSet
import org.eclipse.xtend.core.XtendInjectorSingleton
import javax.tools.ToolProvider
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.CommonPlugin
import java.io.IOException
import org.palladiosimulator.retriever.services.RetrieverConfiguration
import org.palladiosimulator.retriever.services.blackboard.RetrieverBlackboard
import org.palladiosimulator.retriever.services.Rule

class ProjectSpecificRulesProxy implements Rule {

	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.project_specific"
	public static final String LOADED_CLASS_NAME = "org.palladiosimulator.retriever.extraction.rules.ProjectSpecificRules"
	public static final String RULE_PATH_KEY = "xtend_dir_path"

	Optional<Rule> innerRule = Optional.empty;

	override create(RetrieverConfiguration config, RetrieverBlackboard blackboard) {
		val rulesDirectory = getConfiguredRulesDirectory(config)

		val xtendGenDirectory = new File(rulesDirectory.toString + "-xtend-gen")
		if (!xtendGenDirectory.exists && !xtendGenDirectory.mkdirs) {
			throw new IOException("Could not create intermediate compilation directory at " + xtendGenDirectory);
		}

		compileXtend(rulesDirectory, xtendGenDirectory)
		compileJava(xtendGenDirectory)

		val classDirectoryURL = xtendGenDirectory.toURI.toURL
		val classLoader = new URLClassLoader(#[classDirectoryURL], class.classLoader)
		val ruleInstance = try {
				val loadedClass = classLoader.loadClass(LOADED_CLASS_NAME)
				loadedClass.getConstructor().newInstance()
			} catch (ClassNotFoundException exception) {
				throw new IllegalArgumentException(
					"Could not find project-specific rule. It must have the fully qualified name " + LOADED_CLASS_NAME,
					exception)
			}

		innerRule = Optional.of(ruleInstance as Rule)
		innerRule.get().create(config, blackboard)
	}

	def getConfiguredRulesDirectory(RetrieverConfiguration config) {
		val configuredValue = config.getConfig(Rule).getConfig(RULE_ID, RULE_PATH_KEY)
		if (configuredValue !== null && !configuredValue.blank) {
			return new File(configuredValue)
		} else if (config.rulesFolder !== null) {
			val localURI = CommonPlugin.asLocalURI(config.rulesFolder)
			return new File(localURI.devicePath())
		}
		throw new IllegalArgumentException("No path for project-specific rules is specified");
	}

	def compileXtend(File inputDirectory, File outputDirectory) {
		val compiler = XtendInjectorSingleton.INJECTOR.getInstance(XtendBatchCompiler)
		compiler.sourcePath = inputDirectory.toString
		compiler.outputPath = outputDirectory.toString
		compiler.currentClassLoader = class.classLoader
		compiler.useCurrentClassLoaderAsParent = true
		if (!compiler.compile()) {
			throw new IllegalArgumentException("Could not compile xtend files located in " + inputDirectory)
		}
	}

	def compileJava(File inOutDirectory) {
		val sourcePath = inOutDirectory.toPath.resolve("org").resolve("palladiosimulator").resolve("retriever").resolve(
			"extraction").resolve("rules").resolve("ProjectSpecificRules.java")

		val workingDirectory = new File(Platform.installLocation.URL.path).toPath
		val jarpath = workingDirectory.resolve("plugins").resolve("*")
		val classpath = buildClassPath(jarpath.toString)

		val compiler = ToolProvider.systemJavaCompiler
		val fileManager = compiler.getStandardFileManager(null, null, null)
		val compilerOptions = #["-classpath", classpath]
		val sourceFile = fileManager.getJavaFileObjects(sourcePath)
		val compilationTask = compiler.getTask(null, fileManager, null, compilerOptions, null, sourceFile)

		// This call saves the compiled class files into the source directory.
		if (!compilationTask.call()) {
			throw new IllegalArgumentException("Could not compile java files generated from xtend files located in " +
				inOutDirectory)
		}
	}

	/**
	 * This function builds a classpath from the passed Strings
	 * 
	 * @param paths classpath elements
	 * @return returns the complete classpath with wildcards expanded
	 */
	def buildClassPath(String... paths) {
		// Adapted from https://stackoverflow.com/a/22989029
		val sb = new StringBuilder()
		for (path : paths) {
			if (path.endsWith("*")) {
				val subPath = path.substring(0, path.length - 1)
				val pathFile = new File(subPath)
				for (file : pathFile.listFiles) {
					if (file.isFile && file.name.endsWith(".jar")) {
						sb.append(subPath)
						sb.append(file.name)
						sb.append(System.getProperty("path.separator"))
					}
				}
			} else {
				sb.append(path)
				sb.append(System.getProperty("path.separator"))
			}
		}
		return sb.toString
	}

	override isBuildRule() {
		// project-specific rules may not be build rules
		false
	}

	override processRules(RetrieverBlackboard blackboard, Path path) {
		innerRule.ifPresent[x|x.processRules(blackboard, path)]
	}

	override getConfigurationKeys() {
		Set.of(RULE_PATH_KEY)
	}

	override getDependentServices() {
		// project-specific rules may (currently) not be a requirement for any other services
		Set.of
	}

	override getID() {
		RULE_ID
	}

	override getName() {
		"Project-Specific Rules"
	}

	override getRequiredServices() {
		// special value signaling dependency on all other rules
		val requirements = new HashSet()
		requirements.add(null)
		requirements
	}
}
