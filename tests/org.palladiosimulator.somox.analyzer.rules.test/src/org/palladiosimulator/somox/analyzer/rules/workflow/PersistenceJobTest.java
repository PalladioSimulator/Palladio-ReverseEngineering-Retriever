package org.palladiosimulator.somox.analyzer.rules.workflow;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.allocation.factory.FluentAllocationFactory;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.resourceenvironment.factory.FluentResourceEnvironmentFactory;
import org.palladiosimulator.generator.fluent.system.factory.FluentSystemFactory;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class PersistenceJobTest {
    private final static URI TEMPORARY_OUTPUT_FOLDER = URI.createFileURI("./TEMP_PersistenceJobTest/");
    private final static URI INPUT_FOLDER = URI.createFileURI("./input_folder");
    private final static String EXPECTED_FILE_PATH_PREFIX = "./TEMP_PersistenceJobTest/input_folder";
    private final static String BLACKBOARD_INPUT_REPOSITORY = "repository";
    private final static String BLACKBOARD_INPUT_SYSTEM = "system";
    private final static String BLACKBOARD_INPUT_ALLOCATION = "allocation";
    private final static String BLACKBOARD_INPUT_RESOURCEENVIRONMENT = "resource";

    @AfterEach
    public void cleanUpDirectories() throws IOException {
        FileUtils.deleteDirectory(new File(TEMPORARY_OUTPUT_FOLDER.path()));
    }

    @Test
    public void testConstructorWithValidInput() {
        Blackboard<Object> blackboard = new Blackboard<Object>();
        assertDoesNotThrow(
                () -> new PersistenceJob(blackboard, INPUT_FOLDER, TEMPORARY_OUTPUT_FOLDER, BLACKBOARD_INPUT_REPOSITORY,
                        BLACKBOARD_INPUT_SYSTEM, BLACKBOARD_INPUT_ALLOCATION, BLACKBOARD_INPUT_RESOURCEENVIRONMENT));
    }

    @Test
    public void testSaveEmptyModelsToCurrentDirectory() {
        Blackboard<Object> blackboard = new Blackboard<Object>();
        PersistenceJob job = new PersistenceJob(blackboard, INPUT_FOLDER, TEMPORARY_OUTPUT_FOLDER,
                BLACKBOARD_INPUT_REPOSITORY, BLACKBOARD_INPUT_SYSTEM, BLACKBOARD_INPUT_ALLOCATION,
                BLACKBOARD_INPUT_RESOURCEENVIRONMENT);

        // Initialize models
        Repository repository = new FluentRepositoryFactory().newRepository()
            .createRepositoryNow();
        System system = new FluentSystemFactory().newSystem()
            .addRepository(repository)
            .createSystemNow();
        ResourceEnvironment resource = new FluentResourceEnvironmentFactory().newResourceEnvironment()
            .createResourceEnvironmentNow();
        Allocation allocation = new FluentAllocationFactory().newAllocation()
            .createAllocationNow();

        // Add models to blackboard
        blackboard.addPartition(BLACKBOARD_INPUT_REPOSITORY, repository);
        blackboard.addPartition(BLACKBOARD_INPUT_SYSTEM, system);
        blackboard.addPartition(BLACKBOARD_INPUT_ALLOCATION, allocation);
        blackboard.addPartition(BLACKBOARD_INPUT_RESOURCEENVIRONMENT, resource);

        // Execute persistence job
        assertDoesNotThrow(() -> job.execute(new NullProgressMonitor()));

        // Check files exist
        assertTrue(new File(EXPECTED_FILE_PATH_PREFIX + ".repository").exists());
        assertTrue(new File(EXPECTED_FILE_PATH_PREFIX + ".system").exists());
        assertTrue(new File(EXPECTED_FILE_PATH_PREFIX + ".resourceenvironment").exists());
        assertTrue(new File(EXPECTED_FILE_PATH_PREFIX + ".allocation").exists());
    }
}
