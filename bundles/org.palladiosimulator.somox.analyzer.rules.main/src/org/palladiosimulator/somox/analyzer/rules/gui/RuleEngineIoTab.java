package org.palladiosimulator.somox.analyzer.rules.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.ImageRegistryHelper;
import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;

public class RuleEngineIoTab extends AbstractLaunchConfigurationTab {

    public static final String NAME = "Rule Engine IO";
    public static final String PLUGIN_ID = "org.palladiosimulator.somox.analyzer.rules.runconfig.LaunchRuleEngineAnalyzer";
    private static final String FILENAME_TAB_IMAGE_PATH = "icons/RuleEngine_16x16.gif";

    private String defaultPath;
    private Composite container;
    private ModifyListener modifyListener;

    private Text in;
    private Text out;

    public RuleEngineIoTab() {
        // Create the default path of this Eclipse application
        defaultPath = Paths.get(".").toAbsolutePath().normalize().toString();

        // Create a listener for GUI modification events
        modifyListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        };
    }

    @Override
    public Image getImage() {
        // TODO
        return ImageRegistryHelper.getTabImage(PLUGIN_ID, FILENAME_TAB_IMAGE_PATH);
    }

    @Override
    public void createControl(Composite parent) {
        // Create a new Composite to hold the page's controls
        container = new Composite(parent, SWT.NONE);
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
    }

    private boolean validateFolderInput(Text widget) {
        if (widget == null || widget.getText() == null || widget.getText().isBlank()) {
            return error("Blank input.");
        }

        try {
            URI uri = getURI(widget);
            Path path = Paths.get(URI.decode(CommonPlugin.asLocalURI(uri).path()));
            
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

    private URI getURI(Text widget) {
        String text = widget.getText();
        URI uri = URI.createURI(text);
        if (uri.isPlatform())
            return uri;
        else
            return URI.createFileURI(new File(text).getAbsolutePath());
    }

    public boolean isValid(ILaunchConfiguration launchConfig) {
        return validateFolderInput(in) && validateFolderInput(out);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        setText(configuration, in, RuleEngineConfiguration.RULE_ENGINE_INPUT_PATH);
        setText(configuration, out, RuleEngineConfiguration.RULE_ENGINE_OUTPUT_PATH);
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
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_INPUT_PATH, in);
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_OUTPUT_PATH, out);
    }

    private void setAttribute(ILaunchConfigurationWorkingCopy configuration, String attributeName, Text textWidget) {
        try {
            if (textWidget.getText().isEmpty()) {
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
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_INPUT_PATH, in);

        setText(out, defaultPath);
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_OUTPUT_PATH, out);
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