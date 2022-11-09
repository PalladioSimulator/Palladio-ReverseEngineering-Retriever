package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class CompositeBuilder {

    private Set<CompilationUnit> parts = new HashSet<>();
    private Set<String> requiredInterfaces = new HashSet<>();
    private Set<String> providedInterfaces = new HashSet<>();

    public void addPart(CompilationUnit unit) {
        parts.add(unit);
    }

    public void addRequiredInterface(String ifaceName) {
        requiredInterfaces.add(ifaceName);
    }

    public void addProvidedOperation(String declaringIface, IMethodBinding method) {
        providedInterfaces.add(declaringIface);
    }

    public Composite construct(Map<CompilationUnit, Set<String>> totalRequirements,
            Map<CompilationUnit, Set<String>> totalProvisions) {

        // Add all explicit parts.
        Set<CompilationUnit> allParts = new HashSet<>(parts);

        Map<String, Set<CompilationUnit>> provisionsInverted = new HashMap<>();

        for (Entry<CompilationUnit, Set<String>> entry : totalProvisions.entrySet()) {
            for (String provision : entry.getValue()) {
                if (providedInterfaces.contains(provision)) {
                    // Do not include the interfaces this composite provides.
                    // This ensures that components requiring this composite do not become part of
                    // it.
                    continue;
                }
                if (!provisionsInverted.containsKey(provision)) {
                    provisionsInverted.put(provision, new HashSet<>());
                }
                provisionsInverted.get(provision)
                    .add(entry.getKey());
            }
        }

        for (CompilationUnit requiringUnit : totalRequirements.keySet()) {

            Stack<String> unitRequirements = new Stack<>();
            unitRequirements.addAll(totalRequirements.get(requiringUnit));

            boolean isPart = false;
            while (!unitRequirements.isEmpty() && !isPart) {
                String requirement = unitRequirements.pop();
                Set<CompilationUnit> requiredUnits = provisionsInverted.get(requirement);

                for (CompilationUnit requiredUnit : requiredUnits) {
                    if (allParts.contains(requiredUnit)) {
                        isPart = true;
                        break;
                    }
                    Set<String> transitiveRequirements = totalProvisions.get(requiredUnit);
                    unitRequirements.addAll(transitiveRequirements);
                }
            }

            // A unit is a part of this composite if it depends on another part of it.
            if (isPart) {
                allParts.add(requiringUnit);
            }
        }
        return new Composite(parts, requiredInterfaces, providedInterfaces);
    }
}
