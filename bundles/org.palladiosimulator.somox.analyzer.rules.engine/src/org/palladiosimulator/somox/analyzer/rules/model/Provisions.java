package org.palladiosimulator.somox.analyzer.rules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.somox.analyzer.rules.engine.MapMerger;

public class Provisions implements Iterable<OperationInterface> {
    private final Set<OperationInterface> provisions;
    private final Map<OperationInterface, List<OperationInterface>> groupedProvisions;

    public Provisions(Collection<OperationInterface> provisions) {
        this.provisions = Collections.unmodifiableSet(new HashSet<>(provisions));
        this.groupedProvisions = new HashMap<>();
        if (provisions.isEmpty()) {
            return;
        }

        Queue<OperationInterface> sortedProvisions = new PriorityQueue<>(provisions);

        while (!sortedProvisions.isEmpty()) {
            OperationInterface provision = sortedProvisions.poll();
            boolean isRoot = true;
            for (OperationInterface rootInterface : groupedProvisions.keySet()) {
                if (provision.isPartOf(rootInterface)) {
                    groupedProvisions.get(rootInterface)
                        .add(provision);
                    isRoot = false;
                    break;
                }
            }
            for (OperationInterface rootInterface : groupedProvisions.keySet()) {
                Optional<String> commonInterface = provision.getName()
                    .getCommonInterface(rootInterface.getName());
                if (commonInterface.isPresent()) {
                    // De-duplicate interfaces.
                    Set<OperationInterface> interfaces = new HashSet<>(groupedProvisions.remove(rootInterface));
                    interfaces.add(rootInterface);
                    interfaces.add(provision);
                    groupedProvisions.put(new EntireInterface(rootInterface.getName()
                        .createInterface(commonInterface.get())), new ArrayList<>(interfaces));
                    isRoot = false;
                    break;
                }
            }
            if (isRoot) {
                groupedProvisions.put(provision, new LinkedList<>());
            }
        }
    }

    public Set<OperationInterface> get() {
        return provisions;
    }

    public boolean contains(OperationInterface iface) {
        return provisions.stream()
            .anyMatch(x -> x.isPartOf(iface));
    }

    @Override
    public Iterator<OperationInterface> iterator() {
        return provisions.iterator();
    }

    public Map<String, List<Operation>> simplified() {
        List<Map<String, List<Operation>>> simplifiedInterfaces = new LinkedList<>();
        for (OperationInterface root : groupedProvisions.keySet()) {
            Map<String, List<Operation>> simplifiedRoot = new HashMap<>();
            simplifiedRoot.put(root.getInterface(), new ArrayList<>(root.simplified()
                .values()
                .stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList())));
            for (OperationInterface member : groupedProvisions.get(root)) {
                simplifiedRoot.get(root.getInterface())
                    .addAll(member.simplified()
                        .values()
                        .stream()
                        .flatMap(x -> x.stream())
                        .collect(Collectors.toList()));
            }
            simplifiedInterfaces.add(simplifiedRoot);
        }
        return MapMerger.merge(simplifiedInterfaces);
    }
}
