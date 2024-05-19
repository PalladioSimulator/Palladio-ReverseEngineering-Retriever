package org.palladiosimulator.retriever.services;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ServiceConfiguration<T extends Service> {
    private final String selectedServicesKey;
    private final String serviceConfigKeyPrefix;
    private final Map<String, Map<String, String>> serviceConfigs;
    private final Map<String, T> services;
    private final Set<T> manuallySelectedServices;
    private final Map<T, Set<Service>> selectedDependencies;
    private final Set<ServiceConfiguration<? extends Service>> dependencyProviders;

    public ServiceConfiguration(final ServiceCollection<T> serviceCollection, final String selectedServicesKey,
            final String serviceConfigKeyPrefix) {
        this.selectedServicesKey = selectedServicesKey;
        this.serviceConfigKeyPrefix = serviceConfigKeyPrefix;
        this.serviceConfigs = new HashMap<>();
        this.services = new HashMap<>();
        for (final T service : serviceCollection.getServices()) {
            this.services.put(service.getID(), service);
            final Map<String, String> initializedConfig = new HashMap<>();
            for (final String key : service.getConfigurationKeys()) {
                initializedConfig.put(key, "");
            }
            this.serviceConfigs.put(service.getID(), initializedConfig);
        }
        this.manuallySelectedServices = new HashSet<>();
        this.selectedDependencies = new HashMap<>();
        this.dependencyProviders = new HashSet<>();
        this.dependencyProviders.add(this);
    }

    public void addDependencyProvider(final ServiceConfiguration<? extends Service> dependencyProvider) {
        this.dependencyProviders.add(dependencyProvider);
    }

    @SuppressWarnings("unchecked")
    public void applyAttributeMap(final Map<String, Object> attributeMap) {
        final Set<String> serviceIds = (Set<String>) attributeMap.get(this.selectedServicesKey);
        for (final Map.Entry<String, T> serviceEntry : this.services.entrySet()) {
            final String serviceId = serviceEntry.getKey();
            final T service = serviceEntry.getValue();
            if (attributeMap.get(this.serviceConfigKeyPrefix + serviceId) != null) {
                this.serviceConfigs.put(serviceId,
                        (Map<String, String>) attributeMap.get(this.serviceConfigKeyPrefix + serviceId));
            }
            if ((serviceIds != null) && serviceIds.contains(service.getID())) {
                this.select(service);
            }
        }

    }

    public String getConfig(final String serviceId, final String key) {
        final Map<String, String> config = this.serviceConfigs.get(serviceId);
        if (config == null) {
            return null;
        }
        return config.get(key);
    }

    public Map<String, String> getWholeConfig(final String serviceId) {
        return Collections.unmodifiableMap(this.serviceConfigs.get(serviceId));
    }

    public void setConfig(final String serviceId, final String key, final String value) {
        Map<String, String> config = this.serviceConfigs.get(serviceId);
        if (config == null) {
            config = new HashMap<>();
            this.serviceConfigs.put(serviceId, config);
        }
        config.put(key, value);
    }

    public void select(final T service) {
        this.manuallySelectedServices.add(service);
        for (final ServiceConfiguration<? extends Service> dependencyProvider : this.dependencyProviders) {
            dependencyProvider.selectDependenciesOf(service);
        }
    }

    public void deselect(final T service) {
        this.manuallySelectedServices.remove(service);
        for (final ServiceConfiguration<? extends Service> dependencyProvider : this.dependencyProviders) {
            dependencyProvider.deselectDependenciesOf(service);
        }
    }

    public boolean isManuallySelected(final T service) {
        return this.manuallySelectedServices.contains(service);
    }

    public Set<T> getSelected() {
        final Set<T> selectedServices = new HashSet<>(this.manuallySelectedServices);
        selectedServices.addAll(this.selectedDependencies.keySet());
        return Collections.unmodifiableSet(selectedServices);
    }

    public Queue<Collection<T>> getExecutionOrder() {
        final List<Collection<T>> executionOrder = new ArrayList<>();
        final Queue<T> remainingServices = new ArrayDeque<>(this.getSelected());
        final List<T> requiringServices = new LinkedList<>();
        final Map<String, Set<String>> extendedRequirements = new HashMap<>();

        final Set<String> selectedIDs = new HashSet<>();
        for (final T service : remainingServices) {
            selectedIDs.add(service.getID());
        }

        for (final T service : remainingServices) {
            Set<String> requiredServices = service.getRequiredServices();
            // Support types such as immutable sets that do not support contains(null).
            if (requiredServices.stream()
                .anyMatch(x -> x == null)) {
                extendedRequirements.put(service.getID(), new HashSet<>(selectedIDs));
            } else {
                extendedRequirements.put(service.getID(), new HashSet<>(requiredServices));
            }
        }

        // Rephrase all dependencies into requirements
        for (final T providingService : remainingServices) {
            for (final String dependentID : providingService.getDependentServices()) {
                if (!extendedRequirements.containsKey(dependentID)) {
                    continue;
                }
                extendedRequirements.get(dependentID)
                    .add(providingService.getID());
            }
        }

        while (!remainingServices.isEmpty()) {
            final T candidate = remainingServices.poll();
            final String candidateID = candidate.getID();
            final Set<String> candidateRequirements = extendedRequirements.get(candidateID);
            if (this.isRequiringAny(candidateRequirements, remainingServices)
                    || this.isRequiringAny(candidateRequirements, requiringServices)) {
                requiringServices.add(candidate);
            } else {
                this.addAfterRequirements(candidate, candidateRequirements, executionOrder);

                remainingServices.addAll(requiringServices);
                requiringServices.clear();
            }
        }
        if (!requiringServices.isEmpty()) {
            throw new IllegalStateException("Dependency cycle in services, no possible execution order.");
        }
        return new ArrayDeque<>(executionOrder);
    }

    private void addAfterRequirements(final T service, final Set<String> serviceRequirements,
            final List<Collection<T>> executionOrder) {
        if (executionOrder.isEmpty()
                || this.isRequiringAny(serviceRequirements, executionOrder.get(executionOrder.size() - 1))) {
            final Collection<T> newStep = new ArrayList<>();
            newStep.add(service);
            executionOrder.add(newStep);
            return;
        }

        Collection<T> earliestCandidate = executionOrder.get(executionOrder.size() - 1);
        for (int i = executionOrder.size() - 2; i >= 0; i--) {
            final Collection<T> currentStep = executionOrder.get(i);
            if (this.isRequiringAny(serviceRequirements, currentStep)) {
                break;
            }
            earliestCandidate = currentStep;
        }
        earliestCandidate.add(service);
    }

    private boolean isRequiringAny(final Set<String> requirements, final Collection<T> services) {
        return services.stream()
            .map(Service::getID)
            .anyMatch(requirements::contains);
    }

    public Collection<T> getAvailable() {
        return Collections.unmodifiableCollection(this.services.values());
    }

    public void selectDependenciesOf(final Service service) {
        if (service == null) {
            return;
        }
        for (final String dependencyID : service.getRequiredServices()) {
            if (!this.services.containsKey(dependencyID)) {
                continue;
            }
            final T dependency = this.services.get(dependencyID);
            this.addDependingService(dependency, service);
        }
    }

    private void addDependingService(final T dependency, final Service service) {
        if (this.selectedDependencies.containsKey(dependency)) {
            final Set<Service> dependingServices = this.selectedDependencies.get(dependency);
            dependingServices.add(service);
        } else {
            final Set<Service> dependingServices = new HashSet<>();
            dependingServices.add(service);
            this.selectedDependencies.put(dependency, dependingServices);
            for (final ServiceConfiguration<? extends Service> dependencyProvider : this.dependencyProviders) {
                dependencyProvider.selectDependenciesOf(dependency);
            }
        }
    }

    public void deselectDependenciesOf(final Service service) {
        for (final String dependencyID : service.getRequiredServices()) {
            if (!this.services.containsKey(dependencyID)) {
                continue;
            }
            final T dependency = this.services.get(dependencyID);
            this.removeDependingService(dependency, service);
        }
    }

    private void removeDependingService(final T dependency, final Service service) {
        if (!this.selectedDependencies.containsKey(dependency)) {
            return;
        }
        final Set<Service> dependingServices = this.selectedDependencies.get(dependency);
        dependingServices.remove(service);
        // Remove not needed dependencies.
        if (dependingServices.isEmpty()) {
            this.selectedDependencies.remove(dependency);
        }
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        for (final String serviceId : this.serviceConfigs.keySet()) {
            result.put(this.serviceConfigKeyPrefix + serviceId, this.serviceConfigs.get(serviceId));
        }
        result.put(this.selectedServicesKey, serializeServices(this.manuallySelectedServices));
        return result;
    }

    private static Set<String> serializeServices(final Iterable<? extends Service> services) {
        final Set<String> serviceIds = new HashSet<>();
        for (final Service service : services) {
            serviceIds.add(service.getID());
        }
        return serviceIds;
    }
}
