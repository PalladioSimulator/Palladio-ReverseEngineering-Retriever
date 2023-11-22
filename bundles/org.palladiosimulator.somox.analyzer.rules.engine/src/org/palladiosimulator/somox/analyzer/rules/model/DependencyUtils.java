package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public final class DependencyUtils {

    private DependencyUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Group all dependencies in {@code dependencies} by finding their common ancestors.
     * Ensure that no other dependency (not in {@code dependencies}, but in {@code allDependencies}) is included into a group by accident.
     * 
     * @param <T> only for ease of calling
     */
    public static <T extends OperationInterface> Map<OperationInterface, List<OperationInterface>> groupDependencies(
            Collection<T> dependencies, Collection<OperationInterface> allDependencies) {
        Map<OperationInterface, List<OperationInterface>> groupedDependencies = new HashMap<>();
        Queue<OperationInterface> sortedDependencies = new PriorityQueue<>(dependencies);

        while (!sortedDependencies.isEmpty()) {
            OperationInterface grouplessDependency = sortedDependencies.poll();
            boolean isRoot = true;
            for (OperationInterface rootInterface : groupedDependencies.keySet()) {
                if (grouplessDependency.isPartOf(rootInterface)) {
                    groupedDependencies.get(rootInterface)
                        .add(grouplessDependency);
                    isRoot = false;
                    break;
                }
            }
            if (isRoot) {
                for (OperationInterface rootInterface : groupedDependencies.keySet()) {
                    Optional<String> commonInterfaceName = grouplessDependency.getName()
                        .getCommonInterface(rootInterface.getName());
                    boolean containsOtherDependency = false;

                    if (!commonInterfaceName.isPresent()) {
                        continue;
                    }

                    OperationInterface commonInterface = new EntireInterface(rootInterface.getName()
                        .createInterface(commonInterfaceName.get()));

                    for (OperationInterface dependency : allDependencies) {
                        // Check all foreign dependencies
                        if (!dependencies.contains(dependency)) {
                            // If a foreign dependency is part of the new common interface, it must
                            // not be created
                            containsOtherDependency |= dependency.isPartOf(commonInterface);
                        }
                    }

                    if (!containsOtherDependency) {
                        // De-duplicate interfaces.
                        Set<OperationInterface> interfaces = new HashSet<>(groupedDependencies.remove(rootInterface));
                        interfaces.add(commonInterface);
                        interfaces.add(rootInterface);
                        interfaces.add(grouplessDependency);
                        groupedDependencies.put(commonInterface, new ArrayList<>(interfaces));
                        isRoot = false;
                        break;
                    }
                }
            }
            if (isRoot) {
                groupedDependencies.put(grouplessDependency, new LinkedList<>());
                groupedDependencies.get(grouplessDependency).add(grouplessDependency);
            }
        }
        return groupedDependencies;
    }

}
