package org.palladiosimulator.somox.analyzer.rules.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.somox.ui.runconfig.ModelAnalyzerTabGroup;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.DebugEnabledCommonTab;

public class RuleEngineTabGroup extends ModelAnalyzerTabGroup {

    @Override
    public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
        // TODO: Introduce a Config Tab extension point here with the latest Palladio Workflow engine
        // tabs.addAll(new ExtendableTabGroup() {
        // @Override
        // public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        // }
        // }.createExtensionTabs(dialog, mode, "TODO"));

        final List<ILaunchConfigurationTab> tabs = new ArrayList<ILaunchConfigurationTab>();

        tabs.add(new RuleEngineIoTab());
        // tabs.add(new RuleEngineTab());
        tabs.add(new DebugEnabledCommonTab());

        setTabs(tabs.toArray(new ILaunchConfigurationTab[0]));
    }

}
