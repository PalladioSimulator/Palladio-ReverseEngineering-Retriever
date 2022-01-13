package org.palladiosimulator.somox.analyzer.rules.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.configuration.RuleEngineConfiguration;
import org.palladiosimulator.somox.analyzer.rules.workflow.Analyst;
import org.palladiosimulator.somox.analyzer.rules.workflow.AnalystCollection;

import de.uka.ipd.sdq.workflow.launchconfig.ImageRegistryHelper;
import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
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
    private Set<DefaultRule> rules;
    private Set<Button> ruleButtons;
    private Text out;
    private Map<String, Map<String, TreeItem>> analystTreeItems;

    public RuleEngineIoTab() {
        // Create the default path of this Eclipse application
        defaultPath = Paths.get(".")
            .toAbsolutePath()
            .normalize()
            .toString();

        // Initialize the selected rules
        rules = new HashSet<>();
        analystTreeItems = new HashMap<>();

        // Create a listener for GUI modification events
        modifyListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                // e may be null here!
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        };
    }

    @Override
    public Image getImage() {
        // TODO create an image
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

        // Create rule selection area
        Group ruleSelection = new Group(container, SWT.NONE);
        ruleSelection.setText("Rules");
        ruleSelection.setLayout(new RowLayout());
        ruleButtons = new HashSet<>();
        for (DefaultRule rule : DefaultRule.values()) {
            final Button selectionButton = new Button(ruleSelection, SWT.CHECK);
            selectionButton.setText(rule.toString());
            selectionButton
                .addSelectionListener(new RuleSelectionListener(selectionButton, modifyListener, rules, rule));
            ruleButtons.add(selectionButton);
        }

        // Create file input area for output
        out = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFolderInputSection(container, modifyListener, "File Out", out, "File Out", getShell(),
                defaultPath);

        // Create tree view for analyst configuration
        List<Analyst> analysts;
        try {
            AnalystCollection analystCollection = new AnalystCollection();
            analysts = new ArrayList<Analyst>(analystCollection.getAnalysts());
        } catch (CoreException e) {
            Logger.getLogger(RuleEngineIoTab.class)
                .error("Exception occurred while discovering analysts!");
            analysts = new ArrayList<>();
        }

        Tree tree = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION);
        TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
        nameColumn.setWidth(200);
        TreeColumn valueColumn = new TreeColumn(tree, SWT.NONE);
        valueColumn.setWidth(200);

        tree.addListener(SWT.Selection, new TreeEditListener(tree, modifyListener));

        for (int i = 0; i < analysts.size(); i++) {
            TreeItem analystItem = new TreeItem(tree, SWT.NONE);
            analystItem.setText(0, analysts.get(i)
                .getClass()
                .getSimpleName());
            for (String configKey : analysts.get(i)
                .getConfigurationKeys()) {
                TreeItem propertyItem = new TreeItem(analystItem, SWT.NONE);
                propertyItem.setText(0, configKey);
                analystTreeItems.putIfAbsent(analysts.get(i)
                    .getID(), new HashMap<>());
                analystTreeItems.get(analysts.get(i)
                    .getID())
                    .put(configKey, propertyItem);
            }
        }
    }

    private boolean validateFolderInput(Text widget) {
        if (widget == null || widget.getText() == null || widget.getText()
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

    private URI getURI(Text widget) {
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
        setText(configuration, in, RuleEngineConfiguration.RULE_ENGINE_INPUT_PATH);
        setText(configuration, out, RuleEngineConfiguration.RULE_ENGINE_OUTPUT_PATH);

        for (Button ruleButton : ruleButtons) {
            setButton(configuration, ruleButton, RuleEngineConfiguration.RULE_ENGINE_SELECTED_RULES);
        }
    }

    private void setButton(ILaunchConfiguration configuration, Button ruleButton, String attributeName) {
        try {
            Set<String> configRules = (Set<String>) configuration.getAttribute(attributeName, new HashSet<>());
            if (configRules.contains(ruleButton.getText())) {
                ruleButton.setSelection(true);
                rules.add(DefaultRule.valueOf(ruleButton.getText()));
            } else {
                ruleButton.setSelection(false);
                rules.remove(DefaultRule.valueOf(ruleButton.getText()));
            }
        } catch (final Exception e) {
            LaunchConfigPlugin.errorLogger(getName(), attributeName, e.getMessage());
            error(e.getLocalizedMessage());
        }
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
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_SELECTED_RULES, rules);
        // TODO Implement parsing and saving
        // configuration.getAttribute(RuleEngineConfiguration.RULE_ENGINE_ANALYST_CONFIG_PREFIX, new
        // HashMap<String, String>());
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

    private void setAttribute(ILaunchConfigurationWorkingCopy configuration, String attributeName,
            Set<DefaultRule> rules) {
        try {
            configuration.setAttribute(attributeName, RuleEngineConfiguration.serializeRules(rules));
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

        // By default, no rule is selected
        rules = new HashSet<>();
        setAttribute(configuration, RuleEngineConfiguration.RULE_ENGINE_OUTPUT_PATH, rules);
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
