package org.palladiosimulator.somox.discoverer;

import static org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.ContainersFactory;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import jamopp.parser.jdt.JaMoPPJDTParser;
import jamopp.resource.JavaResource2Factory;

public class JdtParserJob implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.jdtparser";

    public static Resource getResource(final URI uri) throws IllegalArgumentException {
        if (uri == null || !uri.isFile() || !"jdt".equals(uri.fileExtension())) {
            throw new IllegalArgumentException("No valid JDT model");
        }
        return new ResourceSetImpl().getResource(uri, true);
    }

    @Override
    public IBlackboardInteractingJob<RuleEngineBlackboard> create(final RuleEngineConfiguration configuration,
            final RuleEngineBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
            }

            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                final Path root = Paths.get(CommonPlugin.asLocalURI(configuration.getInputFolder())
                    .devicePath());
                setBlackboard(Objects.requireNonNull(blackboard));
                logger.info("Begin " + toString());
                monitor.beginTask(toString(), IProgressMonitor.UNKNOWN);

                try {
                    final String uuid = EcoreUtil.generateUUID();
                    final URI r = saveResource(parseDirectory(root), uuid, root);
                    final String partitionId = getName() + uuid;
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
                return "JDT Parser Job";
            }

            private ResourceSet parseDirectory(final Path root) {
                ContainersFactory.eINSTANCE.createEmptyModel();
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("java", new JavaResource2Factory());
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("xmi", new XMIResourceFactoryImpl());
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("xml", new XMLResourceFactoryImpl());
                JavaClasspath.get()
                    .clear();
                final JaMoPPJDTParser parser = new JaMoPPJDTParser();
                final ResourceSet resources = parser.parseDirectory(root);
                EcoreUtil.resolveAll(resources);
                return resources;
            }

            private URI saveResource(final ResourceSet resourceSet, final String uuid, final Path root)
                    throws IOException {
                INSTANCE.getExtensionToFactoryMap()
                    .put("jdt", new XMIResourceFactoryImpl());
                final Path path = Paths.get(root.getParent()
                    .toString(), uuid + "." + "jdt");
                final URI jdtFileURI = URI.createFileURI(path.toString());
                final Resource jdtResource = resourceSet.createResource(jdtFileURI);

                for (final Resource javaResource : new ArrayList<>(resourceSet.getResources())) {
                    if (javaResource.getContents()
                        .isEmpty()
                            || !"file".equals(javaResource.getURI()
                                .scheme())) {
                        continue;
                    }
                    if (javaResource instanceof CompilationUnit) {
                        final CompilationUnit unit = (CompilationUnit) javaResource;
                        if (unit.getClassifiers()
                            .size() <= 0
                                || unit.getClassifiers()
                                    .get(0)
                                    .getName() == null
                                || unit.getNamespacesAsString()
                                    .isEmpty()) {
                            continue;
                        }
                    }
                    jdtResource.getContents()
                        .addAll(javaResource.getContents());
                }

                jdtResource.save(resourceSet.getLoadOptions());
                return jdtFileURI;
            }
        };
    }

    @Override
    public Set<String> getConfigurationKeys() {
        return Collections.emptySet();
    }

    @Override
    public String getID() {
        return DISCOVERER_ID;
    }

    @Override
    public String getName() {
        return "JDT Parser";
    }
}