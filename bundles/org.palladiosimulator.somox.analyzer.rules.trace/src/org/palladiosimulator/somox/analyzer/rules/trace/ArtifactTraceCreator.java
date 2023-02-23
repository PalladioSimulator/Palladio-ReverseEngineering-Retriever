package org.palladiosimulator.somox.analyzer.rules.trace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.capra.generic.artifactmodel.ArtifactWrapper;
import org.eclipse.capra.generic.artifactmodel.ArtifactWrapperContainer;
import org.eclipse.capra.generic.artifactmodel.ArtifactmodelFactory;
import org.eclipse.capra.generic.tracemodel.GenericTraceModel;
import org.eclipse.capra.generic.tracemodel.RelatedTo;
import org.eclipse.capra.generic.tracemodel.TracemodelFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.core.entity.NamedElement;

public class ArtifactTraceCreator {

	private static void save(Resource resource) {
		((XMIResource) resource).setEncoding("UTF-8");
		final Map<Object, Object> saveOptions = ((XMIResource) resource).getDefaultSaveOptions();
		saveOptions.put(XMIResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		try {
			resource.save(saveOptions);
			// ((XMIResource) resource).save(java.lang.System.out, ((XMIResource)
			// resource).getDefaultSaveOptions());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private final Path base;
	private final ArtifactWrapperContainer artifactContainer;
	private final ArtifactmodelFactory artifactFactory;
	private final GenericTraceModel traceContainer;
	private final TracemodelFactory traceFactory;

	public ArtifactTraceCreator(URI inputFolder) {
		base = Paths.get(inputFolder.devicePath()).toAbsolutePath().normalize();

		artifactFactory = ArtifactmodelFactory.eINSTANCE;
		artifactContainer = artifactFactory.createArtifactWrapperContainer();

		traceFactory = TracemodelFactory.eINSTANCE;
		traceContainer = traceFactory.createGenericTraceModel();
	}

	public RelatedTo addTrace(Object from, Object... to) {
		final EObject origin = valueOf(from);
		final List<EObject> targets = Arrays.stream(to).map(this::valueOf).toList();

		for (final RelatedTo trace : traceContainer.getTraces()) {
			if (trace.getOrigin().equals(origin)) {
				for (final EObject artifact : targets) {
					if (!trace.getTargets().contains(artifact)) {
						trace.getTargets().add(artifact);
					}
				}
				return trace;
			}
		}

		final RelatedTo trace = traceFactory.createRelatedTo();
		trace.setName(getName(from));
		trace.setOrigin(origin);
		trace.getTargets().addAll(targets);
		traceContainer.getTraces().add(trace);
		return trace;
	}

	public ArtifactWrapper addWrapper(Object object) {
		final String value = String.valueOf(object);

		for (final ArtifactWrapper artifact : artifactContainer.getArtifacts()) {
			if (artifact.getName().equals(value)) {
				return artifact;
			}
		}

		final ArtifactWrapper wrapper = artifactFactory.createArtifactWrapper();
		wrapper.setName(value);
		artifactContainer.getArtifacts().add(wrapper);
		return wrapper;
	}

	private String getName(Object object) {
		if (object == null) {
			return "null";
		}
		if (object instanceof final NamedElement element) {
			return String.valueOf(element.getEntityName());
		}
		if (object instanceof final ENamedElement element) {
			return String.valueOf(element.getName());
		}
		if (object instanceof final URI uri) {
			return base.relativize(Paths.get(uri.devicePath()).toAbsolutePath().normalize()).toString();
		}
		if (object instanceof final Path path) {
			return base.relativize(path.toAbsolutePath().normalize()).toString();
		}
		if (object instanceof final File file) {
			return base.relativize(Paths.get(file.getPath()).toAbsolutePath().normalize()).toString();
		}
		return object.toString();
	}

	public void save(Path path, String project) {
		final String out = Paths
				.get(Objects.requireNonNull(path).toAbsolutePath().normalize().toString(), String.valueOf(project))
				.toString();

		final ResourceSet resources = new ResourceSetImpl();
		resources.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		final Resource artifactResource = resources.createResource(URI.createFileURI(out + "_artifacts.xmi"));
		artifactResource.getContents().add(artifactContainer);
		save(artifactResource);

		final Resource traceResource = resources.createResource(URI.createFileURI(out + "_traces.xmi"));
		traceResource.getContents().add(traceContainer);
		save(traceResource);
	}

	private EObject valueOf(Object object) {
		if (object == null) {
			return addWrapper("null");
		}
		if (object instanceof final EObject eObject) {
			return eObject;
		}
		if (object instanceof final URI uri) {
			final Path normalized = Paths.get(uri.devicePath()).toAbsolutePath().normalize();
			final ArtifactWrapper wrapper = addWrapper(base.relativize(normalized));
			wrapper.setUri(URI.createURI(normalized.toString()).toString());
			return wrapper;
		}
		if (object instanceof final Path path) {
			final Path normalized = path.toAbsolutePath().normalize();
			final ArtifactWrapper wrapper = addWrapper(base.relativize(normalized));
			wrapper.setUri(URI.createURI(normalized.toString()).toString());
			return wrapper;
		}
		if (object instanceof final File file) {
			final Path normalized = Paths.get(file.getPath()).toAbsolutePath().normalize();
			final ArtifactWrapper wrapper = addWrapper(base.relativize(normalized));
			wrapper.setUri(URI.createURI(normalized.toString()).toString());
			return wrapper;
		}
		return addWrapper(object);
	}

}
