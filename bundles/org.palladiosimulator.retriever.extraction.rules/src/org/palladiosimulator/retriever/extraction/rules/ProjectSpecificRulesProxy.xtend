package org.palladiosimulator.retriever.extraction.rules

import org.palladiosimulator.retriever.extraction.engine.Rule
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard
import java.nio.file.Path
import java.util.Optional
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration
import java.util.Set
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler
import java.net.URLClassLoader
import java.io.File
import java.util.HashSet
import org.eclipse.xtend.core.XtendInjectorSingleton
import javax.tools.ToolProvider
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaFileManager
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.CommonPlugin
import org.eclipse.emf.ecore.plugin.EcorePlugin
import org.eclipse.core.runtime.FileLocator

class ProjectSpecificRulesProxy implements Rule {

	public static final String RULE_ID = "org.palladiosimulator.retriever.extraction.rules.project_specific"
	public static final String LOADED_CLASS_NAME = "org.palladiosimulator.retriever.extraction.rules.ProjectSpecificRules"
	public static final String RULE_PATH_KEY = "xtend_dir_path"

	Optional<Rule> innerRule = Optional.empty;

	override create(RetrieverConfiguration config, RetrieverBlackboard blackboard) {
		val rulePath = new File(config.getConfig(Rule).getConfig(RULE_ID, RULE_PATH_KEY))
		val outputPath = new File(rulePath.parentFile.toPath.resolve("xtend-gen").toString)
		outputPath.mkdirs
		
		val compiler = XtendInjectorSingleton.INJECTOR.getInstance(XtendBatchCompiler)
		compiler.sourcePath = rulePath.toString
		compiler.outputPath = outputPath.toString
		compiler.currentClassLoader = class.classLoader
		compiler.useCurrentClassLoaderAsParent = true
		if (compiler.compile()) {
			val javaFile = outputPath.toPath.resolve("org").resolve("palladiosimulator").resolve("retriever").resolve(
				"extraction").resolve("rules").resolve("ProjectSpecificRules.java")
			val javaCompiler = ToolProvider.systemJavaCompiler
			val standardFileManager = javaCompiler.getStandardFileManager(null, null, null)
			val workingDirectory = new File(Platform.installLocation.URL.path).toPath
			val jarpath = workingDirectory.resolve("plugins").resolve("*")
			val classpath = buildClassPath(jarpath.toString)
			val javaCompilerOptions = #["-classpath", classpath]
			val javaFileObject = standardFileManager.getJavaFileObjects(javaFile)
			val compilationTask = javaCompiler.getTask(null, standardFileManager, null, javaCompilerOptions, null, javaFileObject)
			compilationTask.call
			val outputUrl = outputPath.toURI.toURL
			val classLoader = new URLClassLoader(#[outputUrl], class.classLoader)
			val loadedClass = classLoader.loadClass(LOADED_CLASS_NAME)
			val ruleInstance = loadedClass.getConstructor().newInstance()
			// TODO: handle failures
			innerRule = Optional.of(Rule.cast(ruleInstance))
		}

		if (innerRule.isPresent()) {
			innerRule.get().create(config, blackboard)
		} else {
			Rule.super.create(config, blackboard)
		// TODO: Fail or log error
		}
	}
	
	/**
	 * This function builds a classpath from the passed Strings
	 * 
	 * @param paths classpath elements
	 * @return returns the complete classpath with wildcards expanded
	 */
	def buildClassPath(String... paths) {
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
