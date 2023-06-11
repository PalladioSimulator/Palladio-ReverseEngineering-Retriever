package org.palladiosimulator.somox.analyzer.rules.workflow;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.shared.util.ModelLoader;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public abstract class RuleEngineWorkflowTest {
    protected static final URI DIRECTORY_CASESTUDY = CommonPlugin
            .asLocalURI(URI.createFileURI(URI.decode(new File("res").getAbsolutePath())));
    protected static final URI DIRECTORY_OUT = DIRECTORY_CASESTUDY.appendSegment("out_workflow");

    private final Repository repository;
    private final System system;
    private final ResourceEnvironment resourceEnvironment;
    private final Allocation allocation;

    protected RuleEngineWorkflowTest(String casestudyPath, DefaultRule... rules)
            throws JobFailedException, UserCanceledException, IOException {
        // Create config instance for rule engine job
        RuleEngineConfiguration ruleEngineConfiguration = new RuleEngineConfiguration();
        ruleEngineConfiguration.setInputFolder(DIRECTORY_CASESTUDY.appendSegments(casestudyPath.split("/")));
        ruleEngineConfiguration.setOutputFolder(DIRECTORY_OUT.appendSegment(casestudyPath.replace("/", "_")));
        ruleEngineConfiguration.setSelectedRules(Set.of(rules));

        // Create & run rule engine job
        RuleEngineJob ruleEngineJob = new RuleEngineJob(ruleEngineConfiguration);
        ruleEngineJob.execute(new NullProgressMonitor());

        // Get paths of persisted models
        Path outputDirectory = Path.of(ruleEngineConfiguration.getOutputFolder().toString());
        List<Path> paths = Files.walk(outputDirectory).toList();
        Path repositoryPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("repository")).findFirst().orElseThrow();
        Path systemPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("system")).findFirst().orElseThrow();
        Path resourceEnvironmentPath = paths.stream()
                .filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                        .equals("resourceenvironment"))
                .findFirst().orElseThrow();
        Path allocationPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("allocation")).findFirst().orElseThrow();

        // Load persisted models
        this.repository = ModelLoader.loadRepository(repositoryPath.toString());
        this.system = ModelLoader.loadSystem(systemPath.toString());
        this.resourceEnvironment = ModelLoader.loadResourceEnvironment(resourceEnvironmentPath.toString());
        this.allocation = ModelLoader.loadAllocation(allocationPath.toString());
    }

    @Test
    public void checkPersistedRepository() {
        // Assert persisted repository contains at least one component
        Repository repository = this.getRepository();
        assertFalse(repository.getComponents__Repository().isEmpty());
    }

    @Test
    public void checkPersistedSystem() {
        // Assert persisted system contains at least one assembly context
        System system = this.getSystem();
        assertFalse(system.getAssemblyContexts__ComposedStructure().isEmpty());
    }

    @Test
    public void checkPersistedResourceEnvironment() {
        // Assert persisted resource environment contains at least one container
        ResourceEnvironment resourceEnvironment = this.getResourceEnvironment();
        assertFalse(resourceEnvironment.getResourceContainer_ResourceEnvironment().isEmpty());
    }

    @Test
    public void checkPersistedAllocation() {
        // Assert persisted allocation contains at least one allocation context
        Allocation allocation = this.getAllocation();
        assertFalse(allocation.getAllocationContexts_Allocation().isEmpty());
    }

    public Repository getRepository() {
        return this.repository;
    }

    public System getSystem() {
        return this.system;
    }

    public ResourceEnvironment getResourceEnvironment() {
        return this.resourceEnvironment;
    }

    public Allocation getAllocation() {
        return this.allocation;
    }
}
