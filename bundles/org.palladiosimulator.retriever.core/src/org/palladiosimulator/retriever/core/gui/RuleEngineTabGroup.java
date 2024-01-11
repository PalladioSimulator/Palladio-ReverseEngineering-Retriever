package org.palladiosimulator.retriever.core.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.DebugEnabledCommonTab;

public class RuleEngineTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
        final List<ILaunchConfigurationTab> tabs = new ArrayList<>();

        tabs.add(new RuleEngineIoTab());
        tabs.add(new DebugEnabledCommonTab());

        setTabs(tabs.toArray(new ILaunchConfigurationTab[0]));
    }
}
