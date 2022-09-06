package org.palladiosimulator.somox.discoverer;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.containers.ContainersFactory;
import org.emftext.language.java.containers.JavaRoot;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import jamopp.parser.jdt.JaMoPPJDTParser;
import jamopp.resource.JavaResource2Factory;

public class EmfTextDiscoverer implements Discoverer {

    public static final String DISCOVERER_ID = "org.palladiosimulator.somox.discoverer.emftext";
    public static final String ROOTS_ID = DISCOVERER_ID + ".roots";
    public static final String RESOURCES_ID = DISCOVERER_ID + ".resources";

    private static final String JAVA_TIMEOUT = "JAVA_TIMEOUT";
    private static final String JAVA_VERSION = "JAVA_VERSION";

    @Override
    public IBlackboardInteractingJob<RuleEngineBlackboard> create(final RuleEngineConfiguration configuration,
            final RuleEngineBlackboard blackboard) {
        return new AbstractBlackboardInteractingJob<>() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            private List convertCompilationUnits(final JaMoPPJDTParser api, final Map units) {
                final List<Resource> javaRoots = api.convertCompilationUnits(units);
                javaRoots.forEach(EcoreUtil::resolveAll);
                return javaRoots;
            }

            @Override
            public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
                JavaClasspath.get()
                    .clear();
                System.gc();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
                setBlackboard(Objects.requireNonNull(blackboard));
                ContainersFactory.eINSTANCE.createEmptyModel();
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("java", new JavaResource2Factory());
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("xmi", new XMIResourceFactoryImpl());
                Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                    .put("xml", new XMLResourceFactoryImpl());
                JavaClasspath.get()
                    .clear();

                final var api = new JaMoPPJDTParser();
                java.nio.file.Path p = Paths.get(CommonPlugin.asLocalURI(configuration.getInputFolder())
                        .devicePath());
                final var sourcepathEntries = api.getSourcepathEntries(p);
                final var encodings = new String[sourcepathEntries.length];
                Arrays.fill(encodings, JaMoPPJDTParser.DEFAULT_ENCODING);
                final var units = JaMoPPJDTParser.getCompilationUnits(
                        JaMoPPJDTParser.getJavaParser(configuration.getDiscovererConfig()
                            .getConfig(getID(), JAVA_VERSION)),
                        JaMoPPJDTParser.getClasspathEntries(Paths.get(configuration.getInputFolder()
                            .devicePath())
                            .toAbsolutePath()
                            .normalize()),
                        sourcepathEntries, encodings);
                blackboard.addPartition(DISCOVERER_ID, units);

                Optional<List<JavaRoot>> javaRoots;
                Optional<ResourceSet> javaResources;

                try {
                    javaRoots = Optional
                        .of(supplyAsync(() -> convertCompilationUnits(api, units)).get(getTimeout(), SECONDS));
                    javaResources = Optional.of(api.getResourceSet());
                } catch (final Throwable t) {
                    logger.error("Fatal error when converting compilation units!", t);
                    javaRoots = Optional.empty();
                    javaResources = Optional.empty();
                }

                if (javaRoots.isPresent() && javaResources.isPresent()) {
                    blackboard.addPartition(ROOTS_ID, javaRoots.get());
                    blackboard.addPartition(RESOURCES_ID, javaResources.get());
                }

            }

            @Override
            public String getName() {
                return "EMFText Discoverer Job";
            }

            private int getTimeout() {
                try {
                    return Integer.parseInt(configuration.getDiscovererConfig()
                        .getConfig(getID(), JAVA_TIMEOUT));
                } catch (final NumberFormatException e) {
                    logger.warn("The time limit could not be applied.", e);
                    return 10;
                }
            }
        };
    }

    @Override
    public Set<String> getConfigurationKeys() {
        return Set.of(JAVA_VERSION, JAVA_TIMEOUT);
    }

    @Override
    public String getID() {
        return DISCOVERER_ID;
    }

    @Override
    public String getName() {
        return "EMFText Discoverer";
    }

}
