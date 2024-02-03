package org.palladiosimulator.retriever.extraction.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.repository.structure.components.BasicComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.components.CompositeComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationInterfaceCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationSignatureCreator;
import org.palladiosimulator.generator.fluent.repository.structure.internals.Primitive;
import org.palladiosimulator.generator.fluent.repository.structure.types.CompositeDataTypeCreator;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.ParameterModifier;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.retriever.extraction.blackboard.RetrieverBlackboard;
import org.palladiosimulator.retriever.extraction.commonalities.Component;
import org.palladiosimulator.retriever.extraction.commonalities.Composite;
import org.palladiosimulator.retriever.extraction.commonalities.Operation;
import org.palladiosimulator.retriever.extraction.commonalities.OperationInterface;
import org.palladiosimulator.retriever.extraction.commonalities.PCMDetectionResult;

// TODO Bug-fix, probably
// Class to create a pcm instance out of all results from the detector class
public class PCMInstanceCreator {
    private static final Logger LOG = Logger.getLogger(PCMInstanceCreator.class);

    private static final String JAVA_DISCOVERER_ID = "org.palladiosimulator.retriever.extraction.discoverers.java";

    private static final String REPO_NAME = "Software Architecture Repository";
    private final FluentRepositoryFactory create;
    private final Repo repository;
    private final RetrieverBlackboard blackboard;
    private final Map<String, CompositeDataTypeCreator> existingDataTypesMap;
    private final Map<String, DataType> existingCollectionDataTypes;
    private final Map<Component, CompositeComponentCreator> componentCompositeCreators;
    private final Map<String, CompositeComponentCreator> ifaceCompositeCreators;
    private final Map<Composite, CompositeComponentCreator> compositeCreators;
    private final Map<OperationInterface, org.palladiosimulator.pcm.repository.OperationInterface> pcmInterfaces;

    public PCMInstanceCreator(final RetrieverBlackboard blackboard) {
        this.existingDataTypesMap = new HashMap<>();
        this.existingCollectionDataTypes = new HashMap<>();
        this.componentCompositeCreators = new HashMap<>();
        this.ifaceCompositeCreators = new HashMap<>();
        this.compositeCreators = new HashMap<>();
        this.pcmInterfaces = new HashMap<>();
        this.create = new FluentRepositoryFactory();
        this.repository = this.create.newRepository()
            .withName(REPO_NAME);
        this.blackboard = blackboard;
    }

    private class Pair<T1, T2> {
        private final T1 t1;
        private final T2 t2;

        public Pair(final T1 t1, final T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public T1 getT1() {
            return this.t1;
        }

        public T2 getT2() {
            return this.t2;
        }
    }

    private <K, V> void put(final Map<K, List<V>> map, final K key, final V value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key)
            .add(value);
    }

    /**
     * Returns a PCM Repository model. It first creates the interfaces, then the components.
     *
     * @param mapping
     *            a mapping between microservice names and java model instances
     * @return the PCM repository model
     */
    public Repository createPCM(final Map<String, Set<CompilationUnit>> mapping) {
        final PCMDetectionResult detectionResult = this.blackboard.getPCMDetector()
            .getResult();
        final Set<Component> components = detectionResult.getComponents();
        final Map<OperationInterface, List<Operation>> interfaces = detectionResult.getOperationInterfaces();
        final Set<Composite> composites = detectionResult.getCompositeComponents();

        this.createPCMInterfaces(interfaces);

        final Map<String, Integer> compositeComponentNames = new HashMap<>();
        for (final Composite composite : composites) {
            compositeComponentNames.put(composite.name(),
                    1 + compositeComponentNames.getOrDefault(composite.name(), 0));
        }

        for (final Composite composite : composites) {
            String name = composite.name();
            int nameOccurrences = compositeComponentNames.get(name);
            if (nameOccurrences > 1) {
                compositeComponentNames.put(name, nameOccurrences - 1);
                name += " " + nameOccurrences;
            }

            final CompositeComponentCreator c = this.create.newCompositeComponent()
                .withName(name.replace(".", "_"));
            composite.parts()
                .forEach(x -> this.componentCompositeCreators.put(x, c));
            composite.internalInterfaces()
                .forEach(x -> this.ifaceCompositeCreators.put(x.getInterface(), c));
            this.compositeCreators.put(composite, c);

            final Set<org.palladiosimulator.pcm.repository.OperationInterface> distinctInterfaces = new HashSet<>();
            for (final OperationInterface compRequirement : composite.requirements()) {
                final org.palladiosimulator.pcm.repository.OperationInterface requiredInterface = this.pcmInterfaces
                    .get(compRequirement);
                if (distinctInterfaces.contains(requiredInterface)) {
                    continue;
                }
                distinctInterfaces.add(requiredInterface);
                c.requires(requiredInterface);
            }

            distinctInterfaces.clear();
            for (final OperationInterface compProvision : composite.provisions()) {
                final org.palladiosimulator.pcm.repository.OperationInterface providedInterface = this.pcmInterfaces
                    .get(compProvision);
                if (distinctInterfaces.contains(providedInterface)) {
                    continue;
                }
                distinctInterfaces.add(providedInterface);
                c.provides(providedInterface);
            }
        }

        this.createPCMComponents(components);

        // Add assemblyConnections to composite component.
        for (final Composite composite : composites) {
            final CompositeComponentCreator c = this.compositeCreators.get(composite);
            final CompositeComponent builtComp = (CompositeComponent) c.build();

            final Map<String, List<Pair<OperationRequiredRole, AssemblyContext>>> innerRequirements = new HashMap<>();
            final Map<String, List<Pair<OperationProvidedRole, AssemblyContext>>> innerProvisions = new HashMap<>();

            // Find requiredRoles
            builtComp.getAssemblyContexts__ComposedStructure()
                .stream()
                .forEach(x -> x.getEncapsulatedComponent__AssemblyContext()
                    .getRequiredRoles_InterfaceRequiringEntity()
                    .stream()
                    .map(OperationRequiredRole.class::cast)
                    .forEach(y -> this.put(innerRequirements, y.getRequiredInterface__OperationRequiredRole()
                        .getEntityName(), new Pair<>(y, x))));

            // Find providedRoles
            builtComp.getAssemblyContexts__ComposedStructure()
                .stream()
                .forEach(x -> x.getEncapsulatedComponent__AssemblyContext()
                    .getProvidedRoles_InterfaceProvidingEntity()
                    .stream()
                    .map(OperationProvidedRole.class::cast)
                    .forEach(y -> this.put(innerProvisions, y.getProvidedInterface__OperationProvidedRole()
                        .getEntityName(), new Pair<>(y, x))));

            // Match them up
            for (final OperationInterface internalInterface : composite.internalInterfaces()) {
                final String ifaceName = internalInterface.getInterface();
                for (final Pair<OperationRequiredRole, AssemblyContext> r : innerRequirements.getOrDefault(ifaceName,
                        List.of())) {
                    for (final Pair<OperationProvidedRole, AssemblyContext> p : innerProvisions.getOrDefault(ifaceName,
                            List.of())) {
                        if (!r.getT2()
                            .equals(p.getT2())) {
                            c.withAssemblyConnection(p.getT1(), p.getT2(), r.getT1(), r.getT2());
                        }
                    }
                }
            }

            final Map<String, OperationRequiredRole> outerRequirements = new HashMap<>();
            builtComp.getRequiredRoles_InterfaceRequiringEntity()
                .stream()
                .map(OperationRequiredRole.class::cast)
                .forEach(x -> outerRequirements.put(x.getRequiredInterface__OperationRequiredRole()
                    .getEntityName(), x));

            for (final OperationInterface requiredInterface : composite.requirements()) {
                final String requiredInterfaceName = requiredInterface.getName()
                    .toString()
                    .replace(".", "_");
                for (final Pair<OperationRequiredRole, AssemblyContext> r : innerRequirements
                    .getOrDefault(requiredInterfaceName, List.of())) {
                    c.withRequiredDelegationConnection(r.getT2(), r.getT1(),
                            outerRequirements.get(requiredInterfaceName));
                }
            }

            final Map<String, OperationProvidedRole> outerProvisions = new HashMap<>();
            builtComp.getProvidedRoles_InterfaceProvidingEntity()
                .stream()
                .map(OperationProvidedRole.class::cast)
                .forEach(x -> outerProvisions.put(x.getProvidedInterface__OperationProvidedRole()
                    .getEntityName(), x));

            for (final OperationInterface providedInterface : composite.provisions()) {
                final String providedInterfaceName = providedInterface.getName()
                    .toString()
                    .replace(".", "_");
                for (final Pair<OperationProvidedRole, AssemblyContext> r : innerProvisions
                    .getOrDefault(providedInterfaceName, List.of())) {
                    c.withProvidedDelegationConnection(r.getT2(), r.getT1(),
                            outerProvisions.get(providedInterfaceName));
                }
            }

            this.repository.addToRepository(c);
        }

        return this.repository.createRepositoryNow();
    }

    private void createPCMInterfaces(final Map<OperationInterface, List<Operation>> interfaces) {
        final Map<String, Integer> signatureNameCount = new HashMap<>();
        interfaces.forEach((inter, operations) -> {
            final String interName = inter.getName()
                .toString();
            LOG.info("Current PCM Interface: " + interName);

            final String pcmInterfaceName = interName.replace(".", "_");
            final OperationInterfaceCreator pcmInterface = this.create.newOperationInterface()
                .withName(pcmInterfaceName);

            for (final Operation operation : operations) {
                String name = operation.getName()
                    .forInterface(interName)
                    .orElseThrow();
                name = name.replace(".", "_");
                final Integer oldCount = signatureNameCount.getOrDefault(name, 0);
                signatureNameCount.put(name, oldCount + 1);
                // Omit suffix for first occurrence.
                if (oldCount > 0) {
                    name = name + "$" + signatureNameCount.get(name);
                }
                OperationSignatureCreator signature = this.create.newOperationSignature()
                    .withName(name);

                final IMethodBinding method = operation.getBinding();

                if (method != null) {
                    // parameter type
                    for (final ITypeBinding parameter : method.getParameterTypes()) {
                        signature = this.handleSignatureDataType(signature, parameter.getName(), parameter,
                                parameter.getDimensions(), false);
                    }

                    // Return type: Cast Method Return Type to Variable
                    // OrdinaryParameterImpl is sufficient since return types cannot be varargs.
                    final ITypeBinding returned = method.getReturnType();
                    signature = this.handleSignatureDataType(signature, "", returned, returned.getDimensions(), true);
                }

                pcmInterface.withOperationSignature(signature);

                final Optional<ASTNode> astNode = this.getDeclaration(method);
                if (astNode.isPresent() && this.blackboard.getSeffAssociation(astNode.get()) == null) {
                    final ResourceDemandingSEFF seff = this.create.newSeff()
                        .onSignature(this.create.fetchOfSignature(name))
                        .buildRDSeff();
                    this.blackboard.putSeffAssociation(astNode.get(), seff);
                }
            }

            this.repository.addToRepository(pcmInterface);
            this.pcmInterfaces.put(inter, this.create.fetchOfOperationInterface(pcmInterfaceName));
        });
    }

    private Optional<ASTNode> getDeclaration(final IMethodBinding binding) {
        return this.blackboard.getDiscoveredFiles(JAVA_DISCOVERER_ID, CompilationUnit.class)
            .values()
            .stream()
            .map(unit -> unit.findDeclaringNode(binding))
            .filter(node -> node != null)
            .findAny();
    }

    private void createPCMComponents(final Set<Component> components) {
        for (final Component comp : components) {
            final BasicComponentCreator pcmComp = this.create.newBasicComponent()
                .withName(wrapName(comp.name()));

            final Set<org.palladiosimulator.pcm.repository.OperationInterface> distinctInterfaces = new HashSet<>();
            for (final OperationInterface provision : comp.provisions()
                .getGrouped()
                .keySet()) {
                final org.palladiosimulator.pcm.repository.OperationInterface providedInterface = this.pcmInterfaces
                    .get(provision);
                if (distinctInterfaces.contains(providedInterface)) {
                    continue;
                }
                distinctInterfaces.add(providedInterface);
                pcmComp.provides(providedInterface, provision.toString());
            }

            comp.provisions()
                .simplified()
                .values()
                .stream()
                .flatMap(List::stream)
                .forEach(operation -> {
                    final IMethodBinding method = operation.getBinding();
                    final Optional<ASTNode> declaration = this.getDeclaration(method);
                    if (declaration.isPresent()) {
                        pcmComp.withServiceEffectSpecification(this.blackboard.getSeffAssociation(declaration.get()));
                    }
                });

            distinctInterfaces.clear();
            for (final OperationInterface requirement : comp.requirements()) {
                final org.palladiosimulator.pcm.repository.OperationInterface requiredInterface = this
                    .fetchInterface(requirement);
                if (distinctInterfaces.contains(requiredInterface)) {
                    continue;
                }
                distinctInterfaces.add(requiredInterface);
                pcmComp.requires(requiredInterface, requirement.getName()
                    .toString());
            }

            final BasicComponent builtComp = pcmComp.build();

            // Add component to its composite, if it is part of one.
            final CompositeComponentCreator c = this.componentCompositeCreators.get(comp);
            if (c != null) {
                c.withAssemblyContext(builtComp);
            }

            if (!comp.compilationUnit()
                .isEmpty()) {
                this.blackboard.putRepositoryComponentLocation(builtComp, comp.compilationUnit()
                    .get());
            }
            this.repository.addToRepository(builtComp);
        }

    }

    private org.palladiosimulator.pcm.repository.OperationInterface fetchInterface(final OperationInterface iface) {
        if (this.pcmInterfaces.containsKey(iface)) {
            return this.pcmInterfaces.get(iface);
        }
        for (final OperationInterface registeredInterface : this.pcmInterfaces.keySet()) {
            if (iface.isPartOf(registeredInterface)) {
                return this.pcmInterfaces.get(registeredInterface);
            }
        }
        throw new IllegalArgumentException();
    }

    private static Primitive convertPrimitive(final ITypeBinding primT) {
        switch (primT.getQualifiedName()) {
        case "boolean":
            return Primitive.BOOLEAN;
        case "byte":
            return Primitive.BYTE;
        case "char":
            return Primitive.CHAR;
        case "double":
            return Primitive.DOUBLE;
        case "float":
            // TODO replace with Primitive.FLOAT as soon as that works
            return Primitive.DOUBLE;
        case "int":
            return Primitive.INTEGER;
        case "long":
            return Primitive.LONG;
        case "short":
            // TODO replace with Primitive.SHORT as soon as that works
            return Primitive.INTEGER;
        default:
            return null;
        }
    }

    private OperationSignatureCreator handleSignatureDataType(final OperationSignatureCreator signature,
            final String varName, final ITypeBinding variable, final int varDimensions, final boolean asReturnType) {

        // Parameter is a collection (extends Collection, is an array or a vararg)
        final DataType collectionType = this.handleCollectionType(variable, varDimensions);
        if (collectionType != null) {
            if (asReturnType) {
                return signature.withReturnType(collectionType);
            }
            return signature.withParameter(varName, collectionType, ParameterModifier.IN);
        }

        // Check if type is a primitive type
        final Primitive prim = handlePrimitive(variable);
        if (prim != null) {
            if (asReturnType) {
                return signature.withReturnType(prim);
            }
            return signature.withParameter(varName, prim, ParameterModifier.IN);
        }

        // Check if type is void (not part of pcm primitives)
        if ("void".equals(variable.getQualifiedName()) && asReturnType) {
            if (!this.create.containsDataType("Void")) {
                this.repository.addToRepository(this.create.newCompositeDataType()
                    .withName("Void"));
            }
            return signature.withReturnType(this.create.fetchOfDataType("Void"));
        }

        // Parameter is Composite Type
        final DataType compositeType = this.handleCompositeType(variable);
        if (compositeType != null) {
            if (asReturnType) {
                return signature.withReturnType(compositeType);
            }
            return signature.withParameter(varName, compositeType, ParameterModifier.IN);
        }

        return null;
    }

    private DataType handleCollectionType(final ITypeBinding ref, final int dimensions) {
        // Base for the name of the collection data type
        String typeName = wrapName(ref);

        CollectionDataType collectionType = null;
        String collectionTypeName = null;

        if (dimensions != 0) {
            if (ref.isPrimitive()) {
                typeName = convertPrimitive(ref).name();
            }
            collectionTypeName = typeName;

            if (this.existingCollectionDataTypes.containsKey(collectionTypeName)) {
                return this.existingCollectionDataTypes.get(collectionTypeName);
            }

            collectionType = this.createCollectionWithTypeArg(collectionTypeName, ref, dimensions - 1);
        } else if (isCollectionType(ref) && (ref.getTypeArguments().length > 0)) {
            // TODO: I do not think this works properly for deeper collection types (e.g.
            // List<String>[]), especially the naming.
            typeName = wrapName(ref);
            final ITypeBinding typeArg = ref.getTypeArguments()[0];
            final String argumentTypeName = wrapName(typeArg);
            collectionTypeName = typeName + "<" + argumentTypeName + ">";

            LOG.info("Current Argument type name: " + argumentTypeName);

            if (this.existingCollectionDataTypes.containsKey(collectionTypeName)) {
                return this.existingCollectionDataTypes.get(collectionTypeName);
            }

            collectionType = this.createCollectionWithTypeArg(collectionTypeName, typeArg, typeArg.getDimensions());
        }
        if (collectionType != null) {
            this.existingCollectionDataTypes.put(collectionTypeName, collectionType);
            this.repository.addToRepository(collectionType);
        }
        return collectionType;
    }

    private CollectionDataType createCollectionWithTypeArg(final String collectionTypeName, final ITypeBinding typeArg,
            final int typeArgDimensions) {
        // Type argument is primitive
        final Primitive primitiveArg = handlePrimitive(typeArg);
        if (primitiveArg != null) {
            return this.create.newCollectionDataType(collectionTypeName, primitiveArg);
        }

        // Type argument is a collection again
        // A type argument cannot be a vararg, therefore it is "ordinary"
        final DataType collectionArg = this.handleCollectionType(typeArg, typeArgDimensions);
        if (collectionArg != null) {
            return FluentRepositoryFactory.newCollectionDataType(collectionTypeName, collectionArg);
        }

        // Type argument is a composite data type
        final DataType compositeArg = this.handleCompositeType(typeArg);
        if (compositeArg != null) {
            return FluentRepositoryFactory.newCollectionDataType(collectionTypeName, compositeArg);
        }

        return null;
    }

    private static boolean isCollectionType(final ITypeBinding varClassifier) {

        final List<ITypeBinding> refs = new ArrayList<>();

        if (varClassifier.isClass()) {
            refs.addAll(List.of(varClassifier.getInterfaces()));
        } else if (varClassifier.isInterface()) {
            if ("java.util.Collection".equals(varClassifier.getQualifiedName())) {
                return true;
            }
            refs.addAll(List.of(varClassifier.getInterfaces()));
        }

        for (final ITypeBinding ref : refs) {
            if ("java.util.Collection".equals(ref.getQualifiedName())) {
                return true;
            }
        }

        return false;
    }

    private static Primitive handlePrimitive(final ITypeBinding variable) {
        if (variable.isPrimitive()) {
            return convertPrimitive(variable);
        }
        // Parameter is String, which counts for PCM as Primitive
        if ("java.lang.String".equals(variable.getQualifiedName())) {
            return Primitive.STRING;
        }
        return null;
    }

    private DataType handleCompositeType(final ITypeBinding ref) {
        final String classifierName = wrapName(ref);

        if (!this.existingDataTypesMap.containsKey(classifierName)) {
            // TODO why is this commented out?
//            existingDataTypesMap.put(classifierName, createTypesRecursively(ref));
            this.existingDataTypesMap.put(classifierName, this.create.newCompositeDataType()
                .withName(classifierName));
            this.repository.addToRepository(this.existingDataTypesMap.get(classifierName));
        }

        return this.create.fetchOfCompositeDataType(classifierName);
    }

    private static String wrapName(final ITypeBinding name) {
        return wrapName(name.getQualifiedName());
    }

    private static String wrapName(final String name) {
        final String fullName = name.replace(".", "_");
        // Erase type parameters in identifiers
        // TODO is this the right solution?
        if (fullName.contains("<")) {
            return fullName.substring(0, fullName.indexOf('<'));
        }
        return fullName;
    }
}
