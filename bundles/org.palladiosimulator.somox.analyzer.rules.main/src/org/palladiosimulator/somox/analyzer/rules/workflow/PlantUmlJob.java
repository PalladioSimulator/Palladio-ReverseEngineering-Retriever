package org.palladiosimulator.somox.analyzer.rules.workflow;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.view.plantuml.generator.PcmComponentDiagramGenerator;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class PlantUmlJob extends AbstractBlackboardInteractingJob<RuleEngineBlackboard> {

    private static final Logger LOG = Logger.getLogger(PlantUmlJob.class);

    private static final String NAME = "Rule Engine PlantUML Generation";

    private final RuleEngineConfiguration configuration;

    public PlantUmlJob(RuleEngineConfiguration configuration, RuleEngineBlackboard blackboard) {
        super.setBlackboard(blackboard);
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public void cleanup(IProgressMonitor arg0) throws CleanupFailedException {
    }

    @Override
    public void execute(IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
        PcmComponentDiagramGenerator generator = new PcmComponentDiagramGenerator((Repository) getBlackboard()
            .getPartition(RuleEngineConfiguration.RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY));
        String plantUmlSource = generator.getDiagramText();

        if (configuration.getOutputFolder()
            .isPlatformResource()) {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
                .getRoot();
            IFile file = root.getFile(new Path(configuration.getOutputFolder()
                .appendSegment("componentDiagram.puml")
                .toPlatformString(true)));
            try {
                if (!file.exists()) {
                    file.create(new ByteArrayInputStream(new byte[0]), IFile.FORCE, null);
                }
                file.setContents(new ByteArrayInputStream(plantUmlSource.getBytes()), IFile.FORCE, null);
            } catch (CoreException e) {
                LOG.error(e);
            }

        } else {
            String path = configuration.getOutputFolder()
                .appendSegment("componentDiagram.puml")
                .devicePath();
            try (FileWriter writer = new FileWriter(path)) {
                writer.append(plantUmlSource);
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
