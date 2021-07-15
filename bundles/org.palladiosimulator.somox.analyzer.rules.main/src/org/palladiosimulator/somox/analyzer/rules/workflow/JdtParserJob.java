package org.palladiosimulator.somox.analyzer.rules.workflow;

import static org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.ContainersFactory;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineAnalyzerConfiguration;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import jamopp.parser.jdt.JaMoPPJDTParser;
import jamopp.resource.JavaResource2Factory;

public class JdtParserJob extends AbstractBlackboardInteractingJob<RuleEngineBlackboard> {

	public static final String JOB_NAME = JdtParserJob.class.getSimpleName();

	public static final String MODEL_EXTENSION = "jdt";

	public static Resource getResource(URI uri) throws IllegalArgumentException {
		if(uri == null || !uri.isFile() || !uri.fileExtension().equals(MODEL_EXTENSION)) {
			throw new IllegalArgumentException("No valid JDT model");
		}
		return new ResourceSetImpl().getResource(uri, true);
	}

	private final Path inputDirectory;

	public JdtParserJob(RuleEngineAnalyzerConfiguration configuration, RuleEngineBlackboard blackboard) {
		this(configuration.getMoxConfiguration(), blackboard);
	}

	public JdtParserJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
		inputDirectory = Paths.get(configuration.getInputFolder().devicePath()).toAbsolutePath().normalize();
		setBlackboard(blackboard);
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// TODO Auto-generated method stub
	}

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		logger.info("Begin " + toString());
		monitor.beginTask(toString(), IProgressMonitor.UNKNOWN);

		try {
			final String uuid = EcoreUtil.generateUUID();
			final URI r = saveResource(parseDirectory(), uuid);
			final String partitionId = JOB_NAME + uuid;
			assert !getBlackboard().hasPartition(partitionId);
			getBlackboard().addPartition(partitionId, r);
			logger.info("Add partition to " + r);
		} catch (final Exception e) {
			logger.warn(e.getLocalizedMessage());
			throw new JobFailedException(toString(), e);
		} finally {
			logger.info("Done " + toString());
			monitor.done();
		}
	}

	@Override
	public String getName() {
		return JOB_NAME;
	}

	private ResourceSet parseDirectory() {
		ContainersFactory.eINSTANCE.createEmptyModel();
		INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		final JaMoPPJDTParser parser = new JaMoPPJDTParser();
		final ResourceSet resources = parser.parseDirectory(inputDirectory);
		EcoreUtil.resolveAll(resources);
		return resources;
	}

	private URI saveResource(ResourceSet resourceSet, String uuid) throws IOException {
		INSTANCE.getExtensionToFactoryMap().put(MODEL_EXTENSION, new XMIResourceFactoryImpl());
		final Path path = Paths.get(inputDirectory.getParent().toString(), uuid + "." + MODEL_EXTENSION);
		final URI jdtFileURI = URI.createFileURI(path.toString());
		final Resource jdtResource = resourceSet.createResource(jdtFileURI);

		for (final Resource javaResource : new ArrayList<>(resourceSet.getResources())) {
			if (javaResource.getContents().isEmpty()) {
				continue;
			}
			if (!javaResource.getURI().scheme().equals("file")) {
				continue;
			}
			if (javaResource instanceof CompilationUnit) {
				final CompilationUnit unit = (CompilationUnit) javaResource;
				if (((unit.getClassifiers().size() <= 0) || (unit.getClassifiers().get(0).getName() == null)
						|| unit.getNamespacesAsString().isEmpty())) {
					continue;
				}
			}
			jdtResource.getContents().addAll(javaResource.getContents());
		}

		jdtResource.save(resourceSet.getLoadOptions());
		return jdtFileURI;
	}

	@Override
	public String toString() {
		return JOB_NAME + " for directory: " + inputDirectory;
	}

}
