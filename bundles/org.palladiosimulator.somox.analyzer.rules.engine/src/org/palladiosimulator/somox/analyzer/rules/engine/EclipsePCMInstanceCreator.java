package org.palladiosimulator.somox.analyzer.rules.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.ParameterModifier;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.somox.analyzer.rules.blackboard.CompilationUnitWrapper;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.generator.fluent.exceptions.FluentApiException;
import org.palladiosimulator.generator.fluent.repository.api.Repo;
import org.palladiosimulator.generator.fluent.repository.factory.FluentRepositoryFactory;
import org.palladiosimulator.generator.fluent.repository.structure.components.BasicComponentCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationInterfaceCreator;
import org.palladiosimulator.generator.fluent.repository.structure.interfaces.OperationSignatureCreator;
import org.palladiosimulator.generator.fluent.repository.structure.internals.Primitive;
import org.palladiosimulator.generator.fluent.repository.structure.types.CompositeDataTypeCreator;

// TODO Bug-fix, probably
// Class to create a pcm instance out of all results from the detector class
public class EclipsePCMInstanceCreator {
    private static final Logger LOG = Logger.getLogger(EclipsePCMInstanceCreator.class);

    private static final String REPO_NAME = "Software Architecture Repository";
    private final FluentRepositoryFactory create;
    private final Repo repository;
    private final RuleEngineBlackboard blackboard;
    private final Map<String, CompositeDataTypeCreator> existingDataTypesMap;
    private final Map<String, DataType> existingCollectionDataTypes;

    public EclipsePCMInstanceCreator(RuleEngineBlackboard blackboard) {
        existingDataTypesMap = new HashMap<>();
        existingCollectionDataTypes = new HashMap<>();
        create = new FluentRepositoryFactory();
        repository = create.newRepository()
            .withName(REPO_NAME);
        this.blackboard = blackboard;
    }

    /**
     * Returns a PCM Repository model. It first creates the interfaces, then the components.
     *
     * @param mapping
     *            a mapping between microservice names and java model instances
     * @return the PCM repository model
     */
    public Repository createPCM(Map<String, List<CompilationUnitWrapper>> mapping) {
        final List<CompilationUnit> components = blackboard.getEclipsePCMDetector()
            .getComponents();
        final List<ITypeBinding> interfaces = blackboard.getEclipsePCMDetector()
            .getOperationInterfaces();

        createPCMInterfaces(interfaces);

        createPCMComponents(components);

        Repository repo = repository.createRepositoryNow();

        return repo;
    }

    private void createPCMInterfaces(List<ITypeBinding> interfaces) {
        interfaces.forEach(inter -> {
            LOG.info("Current PCM Interface: " + inter.getQualifiedName());

            OperationInterfaceCreator pcmInterface = create.newOperationInterface()
                .withName(wrapName(inter));

            for (final IMethodBinding m : inter.getDeclaredMethods()) {
                OperationSignatureCreator signature = create.newOperationSignature()
                    .withName(m.getName());

                // parameter type
                for (final ITypeBinding p : m.getParameterTypes()) {
                    signature = handleSignatureDataType(signature, p.getName(), p, p.getDimensions(), false);
                }

                // Return type: Cast Method Return Type to Variable
                // OrdinaryParameterImpl is sufficient since return types cannot be varargs.
                ITypeBinding rt = m.getReturnType();
                signature = handleSignatureDataType(signature, "", rt, rt.getDimensions(), true);

                pcmInterface.withOperationSignature(signature);
            }

            repository.addToRepository(pcmInterface);
        });
    }

    private void createPCMComponents(List<CompilationUnit> components) {
        for (final CompilationUnit comp : components) {
            AbstractTypeDeclaration firstTypeDecl = (AbstractTypeDeclaration) comp.types()
                .get(0);
            BasicComponentCreator pcmComp = create.newBasicComponent()
                .withName(wrapName(firstTypeDecl.resolveBinding()));

            final List<EclipseProvidesRelation> providedRelations = blackboard.getEclipsePCMDetector()
                .getProvidedInterfaces(comp);

            Set<ITypeBinding> realInterfaces = providedRelations.stream()
                .map(relation -> relation.getOperationInterface())
                .collect(Collectors.toSet());
            for (ITypeBinding realInterface : realInterfaces) {
                try {
                    pcmComp.provides(create.fetchOfOperationInterface(wrapName(realInterface)), "dummy name");
                } catch (FluentApiException e) {
                    // Add the interface on-demand if it was not in the model previously.
                    // This is necessary for interfaces that were not in the class path of the java
                    // parser.
                    create.newOperationInterface()
                        .withName(wrapName(realInterface));
                    pcmComp.provides(create.fetchOfOperationInterface(wrapName(realInterface)), "dummy name");
                }
            }

            final List<List<VariableDeclaration>> requiredIs = blackboard.getEclipsePCMDetector()
                .getRequiredInterfaces(comp);
            Set<ITypeBinding> requireInterfaces = requiredIs.stream()
                .map(variable -> variable.get(0)
                    .resolveBinding()
                    .getType())
                .collect(Collectors.toSet());

            for (ITypeBinding requInter : requireInterfaces) {
                try {
                    pcmComp.requires(create.fetchOfOperationInterface(wrapName(requInter)), "dummy require name");
                } catch (FluentApiException e) {
                    // Add the interface on-demand if it was not in the model previously.
                    // This is necessary for interfaces that were not in the class path of the java
                    // parser.
                    create.newOperationInterface()
                        .withName(wrapName(requInter));
                    pcmComp.requires(create.fetchOfOperationInterface(wrapName(requInter)), "dummy require name");
                }
            }
            BasicComponent builtComp = pcmComp.build();
            blackboard.putRepositoryComponentLocation(builtComp, new CompilationUnitWrapper(comp));
            repository.addToRepository(builtComp);
        }
    }

    private static Primitive convertPrimitive(ITypeBinding primT) {
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

    private OperationSignatureCreator handleSignatureDataType(OperationSignatureCreator signature, String varName,
            ITypeBinding var, int varDimensions, boolean asReturnType) {

        // Parameter is a collection (extends Collection, is an array or a vararg)
        DataType collectionType = handleCollectionType(var, varDimensions);
        if (collectionType != null) {
            if (asReturnType) {
                return signature.withReturnType(collectionType);
            }
            return signature.withParameter(varName, collectionType, ParameterModifier.IN);
        }

        // Check if type is a primitive type
        Primitive prim = handlePrimitive(var);
        if (prim != null) {
            if (asReturnType) {
                return signature.withReturnType(prim);
            }
            return signature.withParameter(varName, prim, ParameterModifier.IN);
        }

        // Check if type is void (not part of pcm primitives)
        if (var.getQualifiedName()
            .equals("void") && asReturnType) {
            if (!create.containsDataType("Void")) {
                repository.addToRepository(create.newCompositeDataType()
                    .withName("Void"));
            }
            return signature.withReturnType(create.fetchOfDataType("Void"));
        }

        // Parameter is Composite Type
        DataType compositeType = handleCompositeType(var);
        if (compositeType != null) {
            if (asReturnType) {
                return signature.withReturnType(compositeType);
            }
            return signature.withParameter(varName, compositeType, ParameterModifier.IN);
        }

        return null;
    }

    private DataType handleCollectionType(ITypeBinding ref, int dimensions) {
        // Base for the name of the collection data type
        String typeName = wrapName(ref);

        CollectionDataType collectionType = null;
        String collectionTypeName = null;

        if (dimensions != 0) {
            if (ref.isPrimitive()) {
                typeName = convertPrimitive(ref).name();
            }
            collectionTypeName = typeName;

            if (existingCollectionDataTypes.containsKey(collectionTypeName)) {
                return existingCollectionDataTypes.get(collectionTypeName);
            }

            collectionType = createCollectionWithTypeArg(collectionTypeName, ref, dimensions - 1);
        } else if (isCollectionType(ref) && ref.getTypeArguments().length > 0) {
            // TODO: I do not think this works properly for deeper collection types (e.g.
            // List<String>[]), especially the naming.
            typeName = wrapName(ref);
            ITypeBinding typeArg = ref.getTypeArguments()[0];
            String argumentTypeName = wrapName(typeArg);
            collectionTypeName = typeName + "<" + argumentTypeName + ">";

            LOG.info("Current Argument type name: " + argumentTypeName);

            if (existingCollectionDataTypes.containsKey(collectionTypeName)) {
                return existingCollectionDataTypes.get(collectionTypeName);
            }

            collectionType = createCollectionWithTypeArg(collectionTypeName, typeArg, typeArg.getDimensions());
        }
        if (collectionType != null) {
            existingCollectionDataTypes.put(collectionTypeName, collectionType);
            repository.addToRepository(collectionType);
        }
        return collectionType;
    }

    @SuppressWarnings("static-access")
    private CollectionDataType createCollectionWithTypeArg(String collectionTypeName, ITypeBinding typeArg,
            int typeArgDimensions) {
        // Type argument is primitive
        Primitive primitiveArg = handlePrimitive(typeArg);
        if (primitiveArg != null) {
            return create.newCollectionDataType(collectionTypeName, primitiveArg);
        }

        // Type argument is a collection again
        // A type argument cannot be a vararg, therefore it is "ordinary"
        DataType collectionArg = handleCollectionType(typeArg, typeArgDimensions);
        if (collectionArg != null) {
            return create.newCollectionDataType(collectionTypeName, collectionArg);
        }

        // Type argument is a composite data type
        DataType compositeArg = handleCompositeType(typeArg);
        if (compositeArg != null) {
            return create.newCollectionDataType(collectionTypeName, compositeArg);
        }

        return null;
    }

    private static boolean isCollectionType(ITypeBinding varClassifier) {

        List<ITypeBinding> refs = new ArrayList<>();

        if (varClassifier.isClass()) {
            refs.addAll(List.of(varClassifier.getInterfaces()));
        } else if (varClassifier.isInterface()) {
            if (varClassifier.getQualifiedName()
                .equals("java.util.Collection")) {
                return true;
            } else {
                refs.addAll(List.of(varClassifier.getInterfaces()));
            }
        }

        for (ITypeBinding ref : refs) {
            if (ref.getQualifiedName()
                .equals("java.util.Collection")) {
                return true;
            }
        }

        return false;
    }

    private static Primitive handlePrimitive(ITypeBinding var) {
        if (var.isPrimitive()) {
            return convertPrimitive(var);
        }
        // Parameter is String, which counts for PCM as Primitive
        if (var.getQualifiedName()
            .equals("java.lang.String")) {
            return Primitive.STRING;
        }
        return null;
    }

    private DataType handleCompositeType(ITypeBinding ref) {
        String classifierName = wrapName(ref);

        if (!existingDataTypesMap.containsKey(classifierName)) {
            // TODO why is this commented out?
//            existingDataTypesMap.put(classifierName, createTypesRecursively(ref));
            existingDataTypesMap.put(classifierName, create.newCompositeDataType()
                .withName(classifierName));
            repository.addToRepository(existingDataTypesMap.get(classifierName));
        }

        return create.fetchOfCompositeDataType(classifierName);
    }

    // TODO creation of CompositeDataTypes
    @SuppressWarnings("unused")
    private CompositeDataTypeCreator createTypesRecursively(ITypeBinding type) {
        if (existingDataTypesMap.containsKey(wrapName(type))) {
            return existingDataTypesMap.get(wrapName(type));
        }

        CompositeDataTypeCreator currentDataType = create.newCompositeDataType()
            .withName(wrapName(type));
        for (IVariableBinding f : type.getDeclaredFields()) {

            if (f.getType()
                .isPrimitive()) {
                currentDataType = currentDataType.withInnerDeclaration(f.getName(), convertPrimitive(f.getType()));
            } else if (f.getType()
                .getQualifiedName()
                .equals("java.lang.String")) {
                currentDataType = currentDataType.withInnerDeclaration(f.getName(), Primitive.STRING);
            } else if (f.getType()
                .getQualifiedName()
                .equals("java.util.List")) {
                // TODO Why BYTE?
                currentDataType = currentDataType.withInnerDeclaration(f.getName(),
                        create.newCollectionDataType(f.getName(), Primitive.BYTE));
            } else {
                currentDataType = currentDataType.withInnerDeclaration(f.getName(),
                        createTypesRecursively(f.getType()).build());
            }
        }

        repository.addToRepository(currentDataType);
        return currentDataType;
    }

    private static String wrapName(ITypeBinding name) {
        String fullName = name.getQualifiedName()
            .replace(".", "_");
        // Erase type parameters in identifiers
        // TODO is this the right solution?
        if (fullName.contains("<")) {
            return fullName.substring(0, fullName.indexOf('<'));
        }
        return fullName;
    }
}
