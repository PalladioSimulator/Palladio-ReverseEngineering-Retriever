package org.palladiosimulator.retriever.core.gui;

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
import org.palladiosimulator.retriever.extraction.engine.Service;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

public class ServiceConfigurationView<T extends Service> extends ServiceConfigurationManager<T> {
    private static final int SERVICE_CONFIGURATION_VALUE_COLUMN = 1;

    private final Map<String, Map<String, TreeItem>> configTreeItems;
    private final Map<String, Button> serviceCheckboxes;

    private final ModifyListener modifyListener;

    public ServiceConfigurationView(final ServiceConfiguration<T> serviceConfiguration,
            final ModifyListener modifyListener) {
        super(serviceConfiguration);

        this.configTreeItems = new HashMap<>();
        this.serviceCheckboxes = new HashMap<>();

        this.modifyListener = modifyListener;
    }

    public void createControl(final Composite container) {
        final Tree tree = new Tree(container, SWT.BORDER | SWT.FULL_SELECTION);
        final TreeColumn nameColumn = new TreeColumn(tree, SWT.NONE);
        nameColumn.setWidth(200);
        final TreeColumn valueColumn = new TreeColumn(tree, SWT.NONE);
        valueColumn.setWidth(200);

        tree.addListener(SWT.Selection,
                new TreeEditListener(tree, this.modifyListener, SERVICE_CONFIGURATION_VALUE_COLUMN));

        final List<T> sortedServices = this.getServiceConfiguration()
            .getAvailable()
            .stream()
            .sorted(Comparator.comparing(T::getName))
            .collect(Collectors.toList());
        for (final T service : sortedServices) {
            final TreeItem serviceItem = new TreeItem(tree, SWT.NONE);
            serviceItem.setText(0, service.getClass()
                .getSimpleName());
            this.addCheckboxTo(serviceItem, service);
            if (service.getConfigurationKeys() != null) {
                final String serviceId = service.getID();
                this.configTreeItems.putIfAbsent(serviceId, new HashMap<>());
                for (final String configKey : service.getConfigurationKeys()) {
                    final TreeItem propertyItem = new TreeItem(serviceItem, SWT.NONE);
                    propertyItem.setText(0, configKey);
                    this.configTreeItems.get(serviceId)
                        .put(configKey, propertyItem);
                }
            }
        }
    }

    private void addCheckboxTo(final TreeItem item, final T service) {
        final Tree tree = item.getParent();
        final TreeEditor editor = new TreeEditor(tree);
        final Button checkbox = new Button(tree, SWT.CHECK);
        checkbox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (((Button) e.getSource()).getSelection()) {
                    ServiceConfigurationView.this.getServiceConfiguration()
                        .select(service);
                } else {
                    ServiceConfigurationView.this.getServiceConfiguration()
                        .deselect(service);
                }
                ServiceConfigurationView.this.modifyListener.modifyText(null);
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        checkbox.pack();
        this.serviceCheckboxes.put(service.getID(), checkbox);
        editor.minimumWidth = checkbox.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(checkbox, item, SERVICE_CONFIGURATION_VALUE_COLUMN);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        super.initializeFrom(configuration);
        for (final T service : this.getServiceConfiguration()
            .getAvailable()) {
            final String id = service.getID();
            this.initializeCheckbox(service, this.serviceCheckboxes.get(id));
            this.initializeTreeItems(service, this.configTreeItems.get(id));
        }
    }

    private void initializeCheckbox(final T service, final Button checkbox) {
        final boolean selected = this.getServiceConfiguration()
            .isManuallySelected(service);
        checkbox.setSelection(selected);
    }

    private void initializeTreeItems(final T service, final Map<String, TreeItem> treeItems) {
        final Map<String, String> strings = this.getServiceConfiguration()
            .getWholeConfig(service.getID());
        for (final Entry<String, TreeItem> entry : treeItems.entrySet()) {
            String value = strings.get(entry.getKey());
            if (value == null) {
                value = "";
            }
            entry.getValue()
                .setText(SERVICE_CONFIGURATION_VALUE_COLUMN, value);
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        // Update the ServiceConfiguration with the values from the tree items
        for (final T service : this.getServiceConfiguration()
            .getAvailable()) {
            for (final Entry<String, TreeItem> entry : this.configTreeItems.get(service.getID())
                .entrySet()) {
                final TreeItem treeItem = entry.getValue();
                final String configurationValue = treeItem.getText(SERVICE_CONFIGURATION_VALUE_COLUMN);
                this.getServiceConfiguration()
                    .setConfig(service.getID(), entry.getKey(), configurationValue);
            }
        }

        super.performApply(configuration);
    }

    /**
     * Called when a new launch configuration is created, to set the values to sensible defaults.
     *
     * @param configuration
     *            the new launch configuration
     */
    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        this.writeServiceConfigAttributes(configuration);
    }

    private void writeServiceConfigAttributes(final ILaunchConfigurationWorkingCopy configuration) {
        final Map<String, Object> attributes = this.getServiceConfiguration()
            .toMap();
        for (final Map.Entry<String, Object> attribute : attributes.entrySet()) {
            configuration.setAttribute(attribute.getKey(), attribute.getValue());
        }
    }
}
