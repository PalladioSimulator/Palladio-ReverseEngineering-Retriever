package org.palladiosimulator.somox.analyzer.rules.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceConfiguration<T extends Service> {
    private final String selectedServicesKey;
    private final String serviceConfigKeyPrefix;
    private final Map<String, Map<String, String>> serviceConfigs;
    private final List<T> services;
    private final Set<T> selectedServices;

    public ServiceConfiguration(ServiceCollection<T> serviceCollection, String selectedServicesKey,
            String serviceConfigKeyPrefix) {
        this.selectedServicesKey = selectedServicesKey;
        this.serviceConfigKeyPrefix = serviceConfigKeyPrefix;
        this.serviceConfigs = new HashMap<>();
        this.selectedServices = new HashSet<>();
        this.services = new ArrayList<>(serviceCollection.getServices());
    }

    @SuppressWarnings("unchecked")
    public void applyAttributeMap(Map<String, Object> attributeMap) {
        Set<String> serviceIds = (Set<String>) attributeMap.get(selectedServicesKey);
        for (T service : services) {
            String serviceId = service.getID();
            if (attributeMap.get(serviceConfigKeyPrefix + serviceId) != null) {
                serviceConfigs.put(serviceId,
                        (Map<String, String>) attributeMap.get(serviceConfigKeyPrefix + serviceId));
            }
            if ((serviceIds != null) && serviceIds.contains(service.getID())) {
                selectedServices.add(service);
            }
        }

    }

    public String getConfig(String serviceId, String key) {
        Map<String, String> config = serviceConfigs.get(serviceId);
        if (config == null) {
            return null;
        }
        return config.get(key);
    }

    public Map<String, String> getWholeConfig(String serviceId) {
        return Collections.unmodifiableMap(serviceConfigs.get(serviceId));
    }

    public void setConfig(String serviceId, String key, String value) {
        Map<String, String> config = serviceConfigs.get(serviceId);
        if (config == null) {
            config = new HashMap<>();
            serviceConfigs.put(serviceId, config);
        }
        config.put(key, value);
    }

    public void setSelected(T service, boolean selected) {
        if (selected) {
            selectedServices.add(service);
        } else {
            selectedServices.remove(service);
        }
    }

    public boolean isSelected(T service) {
        return selectedServices.contains(service);
    }

    public Set<T> getSelected() {
        return Collections.unmodifiableSet(selectedServices);
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        for (String serviceId : serviceConfigs.keySet()) {
            result.put(serviceConfigKeyPrefix + serviceId, serviceConfigs.get(serviceId));
        }
        result.put(selectedServicesKey, serializeServices(selectedServices));
        return result;
    }

    public static Set<String> serializeServices(Iterable<? extends Service> services) {
        Set<String> serviceIds = new HashSet<>();
        for (Service service : services) {
            serviceIds.add(service.getID());
        }
        return serviceIds;
    }
}
