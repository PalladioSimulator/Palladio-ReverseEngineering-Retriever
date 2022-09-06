package org.palladiosimulator.somox.analyzer.rules.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
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
import org.palladiosimulator.somox.analyzer.rules.service.Service;
import org.palladiosimulator.somox.analyzer.rules.service.ServiceCollection;
import org.palladiosimulator.somox.analyzer.rules.service.ServiceConfiguration;

import de.uka.ipd.sdq.workflow.launchconfig.LaunchConfigPlugin;

public class ServiceConfigurationView<T extends Service> {
    private static final int SERVICE_CONFIGURATION_VALUE_COLUMN = 1;

    private final Map<String, Map<String, TreeItem>> configTreeItems;
    private final Map<String, Button> serviceCheckboxes;

    private final List<T> services;
    private final Set<T> selectedServices;

    private final ModifyListener modifyListener;
    private final Consumer<String> error;
    private final String tabName;
    private final String serviceConfigKeyPrefix;
    private final String selectedServicesKey;

    public ServiceConfigurationView(ServiceCollection<T> serviceCollection, ModifyListener modifyListener,
            Consumer<String> error, String tabName, String serviceConfigKeyPrefix, String selectedServicesKey) {
        selectedServices = new HashSet<>();
        configTreeItems = new HashMap<>();
        serviceCheckboxes = new HashMap<>();
        services = new ArrayList<T>(serviceCollection.getServices());

        this.modifyListener = modifyListener;
        this.error = error;
        this.tabName = tabName;
        this.serviceConfigKeyPrefix = serviceConfigKeyPrefix;
        this.selectedServicesKey = selectedServicesKey;

    }

    public void createControl(Composite container) {
        Tree tree = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION);
        TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
        nameColumn.setWidth(200);
        TreeColumn valueColumn = new TreeColumn(tree, SWT.NONE);
        valueColumn.setWidth(200);

        tree.addListener(SWT.Selection, new TreeEditListener(tree, modifyListener, SERVICE_CONFIGURATION_VALUE_COLUMN));

        List<T> sortedServices = services.stream()
            .sorted((a, b) -> a.getName()
                .compareTo(b.getName()))
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
                    selectedServices.add(service);
                } else {
                    selectedServices.remove(service);
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

    public void initializeFrom(ILaunchConfiguration configuration) {
        for (T service : services) {
            String id = service.getID();
            setCheckbox(configuration, service, serviceCheckboxes.get(id), selectedServicesKey);
            setTreeItems(configuration, configTreeItems.get(id), serviceConfigKeyPrefix + id);
        }
    }

    private void setCheckbox(ILaunchConfiguration configuration, T service, Button checkbox, String attributeName) {
        try {
            Set<String> configServiceIds = (Set<String>) configuration.getAttribute(attributeName, new HashSet<>());
            if (configServiceIds.contains(service.getID())) {
                checkbox.setSelection(true);
                selectedServices.add(service);
            } else {
                checkbox.setSelection(false);
                selectedServices.remove(service);
            }
        } catch (final Exception e) {
            LaunchConfigPlugin.errorLogger(tabName, attributeName, e.getMessage());
            error.accept(e.getLocalizedMessage());
        }
    }

    private void setTreeItems(ILaunchConfiguration configuration, Map<String, TreeItem> treeItems,
            String attributeName) {
        Map<String, String> strings;
        try {
            strings = configuration.getAttribute(attributeName, new HashMap<>());
            for (Entry<String, TreeItem> entry : treeItems.entrySet()) {
                String value = strings.get(entry.getKey());
                if (value == null) {
                    entry.getValue()
                        .setText(SERVICE_CONFIGURATION_VALUE_COLUMN, "");
                } else {
                    entry.getValue()
                        .setText(SERVICE_CONFIGURATION_VALUE_COLUMN, value);
                }
            }
        } catch (final Exception e) {
            LaunchConfigPlugin.errorLogger(tabName, attributeName, e.getMessage());
            error.accept(e.getLocalizedMessage());
            return;
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        for (T service : services) {
            setAttribute(configuration, serviceConfigKeyPrefix + service.getID(), configTreeItems.get(service.getID()));
        }
        setAttribute(configuration, selectedServicesKey, selectedServices);
    }

    private void setAttribute(ILaunchConfigurationWorkingCopy configuration, String attributeName, Set<T> services) {
        try {
            configuration.setAttribute(attributeName, ServiceConfiguration.serializeServices(services));
        } catch (final Exception e) {
            error.accept(e.getLocalizedMessage());
        }
    }

    private void setAttribute(ILaunchConfigurationWorkingCopy configuration, String attributeName,
            Map<String, TreeItem> treeItems) {
        Map<String, String> strings = new HashMap<>();
        if (treeItems != null) {
            for (Entry<String, TreeItem> entry : treeItems.entrySet()) {
                strings.put(entry.getKey(), entry.getValue()
                    .getText(SERVICE_CONFIGURATION_VALUE_COLUMN));
            }
        }
        configuration.setAttribute(attributeName, strings);
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        for (T service : services) {
            Map<String, TreeItem> treeItems = configTreeItems.get(service.getID());
            clearTreeItems(treeItems);
            setAttribute(configuration, serviceConfigKeyPrefix + service.getID(), treeItems);
        }
        setAttribute(configuration, selectedServicesKey, selectedServices);
    }

    private void clearTreeItems(Map<String, TreeItem> treeItems) {
    	if (treeItems != null) {
    		for (Entry<String, TreeItem> entry : treeItems.entrySet()) {
            	entry.getValue()
            		.setText("");
        	}
    	}
    }
}
