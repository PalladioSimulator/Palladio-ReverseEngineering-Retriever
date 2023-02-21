package org.palladiosimulator.somox.analyzer.rules.trace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.capra.generic.artifactmodel.ArtifactWrapper;
import org.eclipse.capra.generic.artifactmodel.ArtifactWrapperContainer;
import org.eclipse.capra.generic.artifactmodel.ArtifactmodelFactory;
import org.eclipse.capra.generic.tracemodel.GenericTraceModel;
import org.eclipse.capra.generic.tracemodel.RelatedTo;
import org.eclipse.capra.generic.tracemodel.TracemodelFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ArtifactTraceCreator {
	
	private static String valueOf(Object object) {
		if (object == null) {
			return "null";
		} else if (object instanceof URI uri) {
			return Paths.get(uri.path()).toAbsolutePath().normalize().toString();
	    } else if (object instanceof Path path) {
			return path.toAbsolutePath().normalize().toString();
	    } else if (object instanceof File file) {
	    	return Paths.get(file.getPath()).toAbsolutePath().normalize().toString();
	    }
		
		// TODO Add AST, components, interfaces, CVEs and much more...
		
		return String.valueOf(object);	
	}

	private static void save(Resource resource) {
		((XMIResource) resource).setEncoding("UTF-8");
		final Map<Object, Object> saveOptions = ((XMIResource) resource).getDefaultSaveOptions();
		saveOptions.put(XMIResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

		try {
			resource.save(saveOptions);
			// ((XMIResource) resource).save(java.lang.System.out, ((XMIResource) resource).getDefaultSaveOptions());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private final ResourceSet resources;
	private final Resource artifactResource;
	private final Resource traceResource;

	private final ArtifactmodelFactory artifactFactory = ArtifactmodelFactory.eINSTANCE;
	private final ArtifactWrapperContainer artifactContainer = this.artifactFactory.createArtifactWrapperContainer();

	private final TracemodelFactory traceFactory = TracemodelFactory.eINSTANCE;
	private final GenericTraceModel traceContainer = this.traceFactory.createGenericTraceModel();

	public ArtifactTraceCreator() {
		this.resources = new ResourceSetImpl();
		this.resources.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		this.artifactResource = this.resources.createResource(URI.createFileURI("./artifacts.xmi"));
		this.artifactResource.getContents().add(this.artifactContainer);

		this.traceResource = this.resources.createResource(URI.createFileURI("./traces.xmi"));
		this.traceResource.getContents().add(this.traceContainer);
	}

	public void save() {
		save(this.artifactResource);
		save(this.traceResource);
	}

	public RelatedTo addTrace(String from, String... to) {
		final ArtifactWrapper origin = this.addWrapper(from);
		final List<ArtifactWrapper> targets = Arrays.stream(to).map(this::addWrapper).toList();

		for (final RelatedTo trace : this.traceContainer.getTraces()) {
			if (trace.getOrigin().equals(origin)) {
				for (final ArtifactWrapper artifact : targets) {
					if (!trace.getTargets().contains(artifact)) {
						trace.getTargets().add(artifact);
					}
				}
				return trace;
			}
		}

		final RelatedTo trace = this.traceFactory.createRelatedTo();
		trace.setName(String.valueOf(from));
		trace.setOrigin(origin);
		trace.getTargets().addAll(targets);
		this.traceContainer.getTraces().add(trace);
		return trace;
	}

	public ArtifactWrapper addWrapper(String uri) {
		final String value = String.valueOf(uri);

		for (final ArtifactWrapper artifact : this.artifactContainer.getArtifacts()) {
			if (artifact.getUri().equals(value)) {
				return artifact;
			}
		}

		final ArtifactWrapper wrapper = this.artifactFactory.createArtifactWrapper();
		wrapper.setName(value);
		wrapper.setUri(value);
		this.artifactContainer.getArtifacts().add(wrapper);
		return wrapper;
	}

}
