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
import org.palladiosimulator.retriever.core.configuration.RetrieverConfigurationImpl;
import org.palladiosimulator.retriever.core.service.Analyst;
import org.palladiosimulator.retriever.core.service.AnalystCollection;
import org.palladiosimulator.retriever.core.service.DiscovererCollection;
import org.palladiosimulator.retriever.core.service.EmptyCollection;
import org.palladiosimulator.retriever.core.service.RuleCollection;
import org.palladiosimulator.retriever.extraction.engine.Discoverer;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.ServiceCollection;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.ImageRegistryHelper;
import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

public class RetrieverTab extends AbstractLaunchConfigurationTab {

    public static final String NAME = "Retriever";
    public static final String PLUGIN_ID = "org.palladiosimulator.retriever.core.gui.LaunchRetriever";
    private static final String FILENAME_TAB_IMAGE_PATH = "icons/Retriever_16x16.gif";

    private final String defaultPath;
    private final ModifyListener modifyListener;

    private Text in;
    private Text out;
    private final ServiceConfigurationManager<Discoverer> discovererConfigManager;
    private final ServiceConfigurationView<Rule> ruleConfigView;
    private final ServiceConfigurationView<Analyst> analystConfigView;

    public RetrieverTab() {
        // Create the default path of this Eclipse application
        this.defaultPath = Paths.get(".")
            .toAbsolutePath()
            .normalize()
            .toString();

        // Create a listener for GUI modification events
        this.modifyListener = e -> {
            // e may be null here!
            this.setDirty(true);
            this.updateLaunchConfigurationDialog();
        };

        ServiceCollection<Discoverer> discovererCollection = null;
        try {
            discovererCollection = new DiscovererCollection();
        } catch (final CoreException e) {
            Logger.getLogger(RetrieverTab.class)
                .error("Exception occurred while discovering discoverers!");
            discovererCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Discoverer> discovererConfig = new ServiceConfiguration<>(discovererCollection,
                RetrieverConfigurationImpl.RULE_ENGINE_SELECTED_DISCOVERERS,
                RetrieverConfigurationImpl.RULE_ENGINE_DISCOVERER_CONFIG_PREFIX);
        this.discovererConfigManager = new ServiceConfigurationManager<>(discovererConfig);

        ServiceCollection<Rule> ruleCollection = null;
        try {
            ruleCollection = new RuleCollection();
        } catch (final CoreException e) {
            Logger.getLogger(RetrieverTab.class)
                .error("Exception occurred while discovering rules!");
            ruleCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Rule> ruleConfig = new ServiceConfiguration<>(ruleCollection,
                RetrieverConfigurationImpl.RULE_ENGINE_SELECTED_RULES,
                RetrieverConfigurationImpl.RULE_ENGINE_RULE_CONFIG_PREFIX);
        ruleConfig.addDependencyProvider(discovererConfig);
        this.ruleConfigView = new ServiceConfigurationView<>(ruleConfig, this.modifyListener);

        ServiceCollection<Analyst> analystCollection = null;
        try {
            analystCollection = new AnalystCollection();
        } catch (final CoreException e) {
            Logger.getLogger(RetrieverTab.class)
                .error("Exception occurred while discovering analysts!");
            analystCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Analyst> analystConfig = new ServiceConfiguration<>(analystCollection,
                RetrieverConfigurationImpl.RULE_ENGINE_SELECTED_ANALYSTS,
                RetrieverConfigurationImpl.RULE_ENGINE_ANALYST_CONFIG_PREFIX);
        analystConfig.addDependencyProvider(discovererConfig);
        analystConfig.addDependencyProvider(ruleConfig);
        this.analystConfigView = new ServiceConfigurationView<>(analystConfig, this.modifyListener);

    }

    @Override
    public Image getImage() {
        // TODO create an image
        return ImageRegistryHelper.getTabImage(PLUGIN_ID, FILENAME_TAB_IMAGE_PATH);
    }

    @Override
    public void createControl(final Composite parent) {
        // Create a new Composite to hold the page's controls
        final Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());

        // Create file input area for input
        this.in = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFolderInputSection(container, this.modifyListener, "File In", this.in, "File In",
                this.getShell(), this.defaultPath);

        // Create file input area for output
        this.out = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFolderInputSection(container, this.modifyListener, "File Out", this.out, "File Out",
                this.getShell(), this.defaultPath);

        // Create tree view for rule and analyst configuration
        // Do not create a view for discoverers, they can always be selected automatically.
        // If a discoverer is added that requires configuration, this view has to be added back.
        this.ruleConfigView.createControl(container);
        this.analystConfigView.createControl(container);
    }

    private boolean validateFolderInput(final Text widget) {
        if ((widget == null) || (widget.getText() == null) || widget.getText()
            .isBlank()) {
            return this.error("Blank input.");
        }

        try {
            final URI uri = getURI(widget);
            final Path path = Paths.get(CommonPlugin.asLocalURI(uri)
                .devicePath());

            if (!Files.exists(path)) {
                return this.error("The file located by '" + uri + "'does not exist.");
            }
        } catch (final Exception e) {
            return this.error(e.getLocalizedMessage());
        }
        return this.error(null);
    }

    private boolean error(final String message) {
        this.setErrorMessage(message);
        return message == null;
    }

    private static URI getURI(final Text widget) {
        final String text = URI.decode(widget.getText());
        final URI uri = URI.createURI(text);
        if (uri.isPlatform() || uri.isFile()) {
            return uri;
        }
        // This is necessary since paths may start with e.g. "C:" which would
        // then be interpreted as a URI scheme
        return URI.createFileURI(text);
    }

    @Override
    public boolean isValid(final ILaunchConfiguration launchConfig) {
        return this.validateFolderInput(this.in) && this.validateFolderInput(this.out);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        this.setText(configuration, this.in, RetrieverConfigurationImpl.RULE_ENGINE_INPUT_PATH);
        this.setText(configuration, this.out, RetrieverConfigurationImpl.RULE_ENGINE_OUTPUT_PATH);

        this.discovererConfigManager.initializeFrom(configuration);
        this.ruleConfigView.initializeFrom(configuration);
        this.analystConfigView.initializeFrom(configuration);
    }

    private void setText(final ILaunchConfiguration configuration, final Text textWidget, final String attributeName) {
        try {
            textWidget.setText(configuration.getAttribute(attributeName, ""));
        } catch (final Exception e) {
            LaunchConfigPlugin.errorLogger(this.getName(), attributeName, e.getMessage());
            this.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        this.setAttribute(configuration, RetrieverConfigurationImpl.RULE_ENGINE_INPUT_PATH, this.in);
        this.setAttribute(configuration, RetrieverConfigurationImpl.RULE_ENGINE_OUTPUT_PATH, this.out);
        this.discovererConfigManager.performApply(configuration);
        this.ruleConfigView.performApply(configuration);
        this.analystConfigView.performApply(configuration);
    }

    private void setAttribute(final ILaunchConfigurationWorkingCopy configuration, final String attributeName,
            final Text textWidget) {
        try {
            if (textWidget.getText()
                .isEmpty()) {
                configuration.setAttribute(attributeName, "");
            } else {
                configuration.setAttribute(attributeName, getURI(textWidget).toString());
            }
        } catch (final Exception e) {
            this.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        this.setText(this.in, this.defaultPath);
        this.setAttribute(configuration, RetrieverConfigurationImpl.RULE_ENGINE_INPUT_PATH, this.in);

        this.setText(this.out, this.defaultPath);
        this.setAttribute(configuration, RetrieverConfigurationImpl.RULE_ENGINE_OUTPUT_PATH, this.out);

        this.discovererConfigManager.setDefaults(configuration);
        this.ruleConfigView.setDefaults(configuration);
        this.analystConfigView.setDefaults(configuration);
    }

    private void setText(final Text textWidget, final String attributeName) {
        try {
            textWidget.setText(attributeName);
        } catch (final Exception e) {
            this.error(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
