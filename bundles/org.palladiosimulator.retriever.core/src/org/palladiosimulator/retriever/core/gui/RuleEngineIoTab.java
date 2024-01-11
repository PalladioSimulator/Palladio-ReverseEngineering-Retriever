package org.palladiosimulator.retriever.core.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.palladiosimulator.retriever.core.configuration.RuleEngineConfigurationImpl;
import org.palladiosimulator.retriever.core.service.Analyst;
import org.palladiosimulator.retriever.core.service.AnalystCollection;
import org.palladiosimulator.retriever.core.service.EmptyCollection;
import org.palladiosimulator.retriever.core.service.RuleCollection;
import org.palladiosimulator.retriever.extraction.discoverers.Discoverer;
import org.palladiosimulator.retriever.extraction.discoverers.DiscovererCollection;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.ServiceCollection;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.ImageRegistryHelper;
import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

public class RuleEngineIoTab extends AbstractLaunchConfigurationTab {

    public static final String NAME = "Rule Engine IO";
    public static final String PLUGIN_ID = "org.palladiosimulator.retriever.core.gui.LaunchRuleEngineAnalyzer";
    private static final String FILENAME_TAB_IMAGE_PATH = "icons/RuleEngine_16x16.gif";

    private final String defaultPath;
    private final ModifyListener modifyListener;

    private Text in;
    private Text out;
    private final ServiceConfigurationManager<Discoverer> discovererConfigManager;
    private final ServiceConfigurationView<Rule> ruleConfigView;
    private final ServiceConfigurationView<Analyst> analystConfigView;

    public RuleEngineIoTab() {
        // Create the default path of this Eclipse application
        defaultPath = Paths.get(".")
            .toAbsolutePath()
            .normalize()
            .toString();

        // Create a listener for GUI modification events
        modifyListener = e -> {
            // e may be null here!
            setDirty(true);
            updateLaunchConfigurationDialog();
        };

        ServiceCollection<Discoverer> discovererCollection = null;
        try {
            discovererCollection = new DiscovererCollection();
        } catch (CoreException e) {
            Logger.getLogger(RuleEngineIoTab.class)
                .error("Exception occurred while discovering discoverers!");
            discovererCollection = new EmptyCollection<>();
        }
        ServiceConfiguration<Discoverer> discovererConfig = new ServiceConfiguration<>(discovererCollection,
                RuleEngineConfigurationImpl.RULE_ENGINE_SELECTED_DISCOVERERS,
                RuleEngineConfigurationImpl.RULE_ENGINE_DISCOVERER_CONFIG_PREFIX);
        discovererConfigManager = new ServiceConfigurationManager<>(discovererConfig);

        ServiceCollection<Rule> ruleCollection = null;
        try {
            ruleCollection = new RuleCollection();
        } catch (CoreException e) {
            Logger.getLogger(RuleEngineIoTab.class)
                .error("Exception occurred while discovering rules!");
            ruleCollection = new EmptyCollection<>();
        }
        ServiceConfiguration<Rule> ruleConfig = new ServiceConfiguration<>(ruleCollection,
                RuleEngineConfigurationImpl.RULE_ENGINE_SELECTED_RULES,
                RuleEngineConfigurationImpl.RULE_ENGINE_RULE_CONFIG_PREFIX);
        ruleConfig.addDependencyProvider(discovererConfig);
        ruleConfigView = new ServiceConfigurationView<>(ruleConfig, modifyListener);

        ServiceCollection<Analyst> analystCollection = null;
        try {
            analystCollection = new AnalystCollection();
        } catch (CoreException e) {
            Logger.getLogger(RuleEngineIoTab.class)
                .error("Exception occurred while discovering analysts!");
            analystCollection = new EmptyCollection<>();
        }
        ServiceConfiguration<Analyst> analystConfig = new ServiceConfiguration<>(analystCollection,
                RuleEngineConfigurationImpl.RULE_ENGINE_SELECTED_ANALYSTS,
                RuleEngineConfigurationImpl.RULE_ENGINE_ANALYST_CONFIG_PREFIX);
        analystConfig.addDependencyProvider(discovererConfig);
        analystConfig.addDependencyProvider(ruleConfig);
        analystConfigView = new ServiceConfigurationView<>(analystConfig, modifyListener);

    }

    @Override
    public Image getImage() {
        // TODO create an image
        return ImageRegistryHelper.getTabImage(PLUGIN_ID, FILENAME_TAB_IMAGE_PATH);
    }

    @Override
    public void createControl(Composite parent) {
        // Create a new Composite to hold the page's controls
        Composite container = new Composite(parent, SWT.NONE);
        setControl(container);
        container.setLayout(new GridLayout());

        // Create file input area for input
        in = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFolderInputSection(container, modifyListener, "File In", in, "File In", getShell(),
                defaultPath);

        // Create file input area for output
        out = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFolderInputSection(container, modifyListener, "File Out", out, "File Out", getShell(),
                defaultPath);

        // Create tree view for rule and analyst configuration
        // Do not create a view for discoverers, they can always be selected automatically.
        // If a discoverer is added that requires configuration, this view has to be added back.
        ruleConfigView.createControl(container);
        analystConfigView.createControl(container);
    }

    private boolean validateFolderInput(Text widget) {
        if ((widget == null) || (widget.getText() == null) || widget.getText()
            .isBlank()) {
            return error("Blank input.");
        }

        try {
            URI uri = getURI(widget);
            Path path = Paths.get(CommonPlugin.asLocalURI(uri)
                .devicePath());

            if (!Files.exists(path)) {
                return error("The file located by '" + uri + "'does not exist.");
            }
        } catch (Exception e) {
            return error(e.getLocalizedMessage());
        }
        return error(null);
    }

    private boolean error(final String message) {
        setErrorMessage(message);
        return message == null;
    }

    private static URI getURI(Text widget) {
        String text = URI.decode(widget.getText());
        URI uri = URI.createURI(text);
        if (uri.isPlatform() || uri.isFile()) {
            return uri;
        }
        // This is necessary since paths may start with e.g. "C:" which would
        // then be interpreted as a URI scheme
        return URI.createFileURI(text);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        return validateFolderInput(in) && validateFolderInput(out);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        setText(configuration, in, RuleEngineConfigurationImpl.RULE_ENGINE_INPUT_PATH);
        setText(configuration, out, RuleEngineConfigurationImpl.RULE_ENGINE_OUTPUT_PATH);

        discovererConfigManager.initializeFrom(configuration);
        ruleConfigView.initializeFrom(configuration);
        analystConfigView.initializeFrom(configuration);
    }

    private void setText(ILaunchConfiguration configuration, Text textWidget, String attributeName) {
        try {
            textWidget.setText(configuration.getAttribute(attributeName, ""));
        } catch (final Exception e) {
            LaunchConfigPlugin.errorLogger(getName(), attributeName, e.getMessage());
            error(e.getLocalizedMessage());
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        setAttribute(configuration, RuleEngineConfigurationImpl.RULE_ENGINE_INPUT_PATH, in);
        setAttribute(configuration, RuleEngineConfigurationImpl.RULE_ENGINE_OUTPUT_PATH, out);
        discovererConfigManager.performApply(configuration);
        ruleConfigView.performApply(configuration);
        analystConfigView.performApply(configuration);
    }

    private void setAttribute(ILaunchConfigurationWorkingCopy configuration, String attributeName, Text textWidget) {
        try {
            if (textWidget.getText()
                .isEmpty()) {
                configuration.setAttribute(attributeName, "");
            } else {
                configuration.setAttribute(attributeName, getURI(textWidget).toString());
            }
        } catch (final Exception e) {
            error(e.getLocalizedMessage());
        }
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        setText(in, defaultPath);
        setAttribute(configuration, RuleEngineConfigurationImpl.RULE_ENGINE_INPUT_PATH, in);

        setText(out, defaultPath);
        setAttribute(configuration, RuleEngineConfigurationImpl.RULE_ENGINE_OUTPUT_PATH, out);

        discovererConfigManager.setDefaults(configuration);
        ruleConfigView.setDefaults(configuration);
        analystConfigView.setDefaults(configuration);
    }

    private void setText(final Text textWidget, final String attributeName) {
        try {
            textWidget.setText(attributeName);
        } catch (final Exception e) {
            error(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
