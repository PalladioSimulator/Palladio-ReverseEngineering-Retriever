package org.palladiosimulator.somox.analyzer.rules.gui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.palladiosimulator.somox.analyzer.rules.engine.Service;
import org.palladiosimulator.somox.analyzer.rules.engine.ServiceConfiguration;

public class ServiceConfigurationView<T extends Service> extends ServiceConfigurationManager<T> {
    private static final int SERVICE_CONFIGURATION_VALUE_COLUMN = 1;

    private final Map<String, Map<String, TreeItem>> configTreeItems;
    private final Map<String, Button> serviceCheckboxes;
    
    private final ModifyListener modifyListener;

    public ServiceConfigurationView(ServiceConfiguration<T> serviceConfiguration, ModifyListener modifyListener) {
    	super(serviceConfiguration);

        configTreeItems = new HashMap<>();
        serviceCheckboxes = new HashMap<>();

        this.modifyListener = modifyListener;
    }

    public void createControl(Composite container) {
        Tree tree = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION);
        TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
        nameColumn.setWidth(200);
        TreeColumn valueColumn = new TreeColumn(tree, SWT.NONE);
        valueColumn.setWidth(200);

        tree.addListener(SWT.Selection, new TreeEditListener(tree, modifyListener, SERVICE_CONFIGURATION_VALUE_COLUMN));

        List<T> sortedServices = getServiceConfiguration()
        	.getAvailable()
        	.stream()
            .sorted(Comparator.comparing(T::getName))
            .collect(Collectors.toList());
        for (T service : sortedServices) {
            TreeItem serviceItem = new TreeItem(tree, SWT.NONE);
            serviceItem.setText(0, service.getClass()
                .getSimpleName());
            addCheckboxTo(serviceItem, service);
            if (service.getConfigurationKeys() != null) {
                String serviceId = service.getID();
                configTreeItems.putIfAbsent(serviceId, new HashMap<>());
                for (String configKey : service.getConfigurationKeys()) {
                    TreeItem propertyItem = new TreeItem(serviceItem, SWT.NONE);
                    propertyItem.setText(0, configKey);
                    configTreeItems.get(serviceId)
                        .put(configKey, propertyItem);
                }
            }
        }
    }

    private void addCheckboxTo(TreeItem item, T service) {
        Tree tree = item.getParent();
        TreeEditor editor = new TreeEditor(tree);
        Button checkbox = new Button(tree, SWT.CHECK);
        checkbox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if (((Button) e.getSource()).getSelection()) {
            		getServiceConfiguration().select(service);
            	} else {
            		getServiceConfiguration().deselect(service);
            	}
                modifyListener.modifyText(null);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        checkbox.pack();
        serviceCheckboxes.put(service.getID(), checkbox);
        editor.minimumWidth = checkbox.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(checkbox, item, SERVICE_CONFIGURATION_VALUE_COLUMN);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
    	super.initializeFrom(configuration);
        for (T service : getServiceConfiguration().getAvailable()) {
            String id = service.getID();
            initializeCheckbox(service, serviceCheckboxes.get(id));
            initializeTreeItems(service, configTreeItems.get(id));
        }
    }

    private void initializeCheckbox(T service, Button checkbox) {
        boolean selected = getServiceConfiguration().isManuallySelected(service);
        checkbox.setSelection(selected);
    }

    private void initializeTreeItems(T service, Map<String, TreeItem> treeItems) {
        Map<String, String> strings = getServiceConfiguration()
        		.getWholeConfig(service.getID());
        for (Entry<String, TreeItem> entry : treeItems.entrySet()) {
            String value = strings.get(entry.getKey());
            if (value == null) {
            	value = "";
            }
            entry.getValue()
                .setText(SERVICE_CONFIGURATION_VALUE_COLUMN, value);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	// Update the ServiceConfiguration with the values from the tree items
        for (T service : getServiceConfiguration().getAvailable()) {
        	for (Entry<String, TreeItem> entry : configTreeItems.get(service.getID()).entrySet()) {
        		TreeItem treeItem = entry.getValue();
        		String configurationValue = treeItem.getText(SERVICE_CONFIGURATION_VALUE_COLUMN);
        		getServiceConfiguration().setConfig(service.getID(), entry.getKey(), configurationValue);
        	}
        }
        
        super.performApply(configuration);
    }

    /**
     * Called when a new launch configuration is created, to set the values to sensible defaults.
     * @param configuration the new launch configuration
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        writeServiceConfigAttributes(configuration);
    }
    
    private void writeServiceConfigAttributes(ILaunchConfigurationWorkingCopy configuration) {
    	Map<String, Object> attributes = getServiceConfiguration().toMap();
    	for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
    		configuration.setAttribute(attribute.getKey(), attribute.getValue());
    	}
    }
}
