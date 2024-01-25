package org.palladiosimulator.retriever.core.workflow;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.view.plantuml.generator.PcmAllocationDiagramGenerator;
import org.palladiosimulator.view.plantuml.generator.PcmComponentDiagramGenerator;
import org.palladiosimulator.view.plantuml.generator.PcmSystemDiagramGenerator;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PlantUmlJob extends AbstractBlackboardInteractingJob<RetrieverBlackboard> {

    private static final String ALLOCATION_DIAGRAM_NAME = "allocationDiagram.puml";
    private static final String COMPONENT_DIAGRAM_NAME = "componentDiagram.puml";
    private static final String END_UML = "\n@enduml\n";
    private static final String JOB_NAME = "Retriever PlantUML Generation";
    private static final Logger LOGGER = Logger.getLogger(PlantUmlJob.class);
    private static final String START_UML = "@startuml\n";
    private static final String SYSTEM_DIAGRAM_NAME = "systemDiagram.puml";

    private final String allocationKey;
    private final URI outputFolder;
    private final String repositoryKey;
    private final String systemKey;

    public PlantUmlJob(final RetrieverBlackboard blackboard, final URI outputFolder, final String repositoryKey,
            final String systemKey, final String allocationKey) {
        super.setBlackboard(Objects.requireNonNull(blackboard));
        this.repositoryKey = Objects.requireNonNull(repositoryKey);
        this.systemKey = Objects.requireNonNull(systemKey);
        this.allocationKey = Objects.requireNonNull(allocationKey);
        this.outputFolder = Objects.requireNonNull(outputFolder);

    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
        // No cleanup required.
    }

    @Override
    public void execute(final IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
        final Repository repository = (Repository) getBlackboard().getPartition(repositoryKey);
        if ((repository != null) && !repository.eContents().isEmpty()) {
            writeFile(arg0, START_UML + new PcmComponentDiagramGenerator(repository).getDiagramText() + END_UML,
                    COMPONENT_DIAGRAM_NAME);
        }

        final System system = (System) getBlackboard().getPartition(systemKey);
        if ((system != null) && !system.eContents().isEmpty()) {
            writeFile(arg0, START_UML + new PcmSystemDiagramGenerator(system).getDiagramText() + END_UML,
                    SYSTEM_DIAGRAM_NAME);
        }

        final Allocation allocation = (Allocation) getBlackboard().getPartition(allocationKey);
        if ((allocation != null) && !allocation.eContents().isEmpty()) {
            writeFile(arg0, START_UML + new PcmAllocationDiagramGenerator(allocation).getDiagramText() + END_UML,
                    ALLOCATION_DIAGRAM_NAME);
        }
    }

    @Override
    public String getName() {
        return JOB_NAME;
    }

    private void writeFile(final IProgressMonitor monitor, final String plantUmlSource, final String fileName) {
        if (outputFolder.isPlatformResource()) {
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final IFile file = root.getFile(new Path(outputFolder.appendSegment(fileName).toPlatformString(true)));
            try {
                if (!file.exists()) {
                    file.create(new ByteArrayInputStream(new byte[0]), IResource.FORCE, monitor);
                }
                file.setContents(new ByteArrayInputStream(plantUmlSource.getBytes()), IResource.FORCE, monitor);
            } catch (final CoreException e) {
                LOGGER.error(e);
            }

        } else {
            final String path = outputFolder.appendSegment(fileName).devicePath();
            try (FileWriter writer = new FileWriter(path)) {
                writer.append(plantUmlSource);
            } catch (final IOException e) {
                LOGGER.error(e);
            }
        }
    }

}
