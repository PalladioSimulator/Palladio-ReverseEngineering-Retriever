package org.palladiosimulator.retriever.extraction.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.util.IModifierConstants;

/**
 * This class is used as a supporting library for writing rules for Retriever. It contains numerous
 * methods to query a certain state of a java model instance. For example, is a class is annotated
 * with a specific annotation name. Also the helper contains methods for retrieving aspects of a
 * class like the interfaces it is implementing.
 *
 * @author Florian Bossert
 */
public class RuleHelper {

    private static final Logger LOG = Logger.getLogger(RuleHelper.class);

    public static String getUnitName(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        String name = "NO NAME";
        for (final AbstractTypeDeclaration abstType : types) {
            name = abstType.getName()
                .getFullyQualifiedName();
        }

        return name;
    }

    public static boolean isAbstraction(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration typeDecl && (typeDecl.isInterface() || typeDecl.modifiers()
                .contains(Modifier.ABSTRACT))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUnitAnnotatedWithName(final CompilationUnit unit, final String... names) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final String name : names) {
            for (final AbstractTypeDeclaration abstType : types) {
                if (isClassifierAnnotatedWithName(abstType, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isObjectAnnotatedWithName(final BodyDeclaration body, final String name) {
        return containsAnnotationWithName(cast(body.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(final SingleVariableDeclaration parameter, final String name) {
        return containsAnnotationWithName(cast(parameter.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(final TypeParameter parameter, final String name) {
        return containsAnnotationWithName(cast(parameter.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(final VariableDeclarationExpression expression, final String name) {
        return containsAnnotationWithName(cast(expression.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(final VariableDeclarationStatement statement, final String name) {
        return containsAnnotationWithName(cast(statement.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isClassifierAnnotatedWithName(final BodyDeclaration abstTypeDecl, final String name) {
        return containsAnnotationWithName(cast(abstTypeDecl.modifiers(), IExtendedModifier.class), name);
    }

    private static boolean containsAnnotationWithName(final List<IExtendedModifier> modifiers, final String name) {
        for (final IExtendedModifier mod : modifiers) {
            if (mod.isAnnotation()) {
                final Annotation anno = (Annotation) mod;
                final Name annoName = anno.getTypeName();

                if (annoName.getFullyQualifiedName()
                    .equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<MethodDeclaration> getMethods(final CompilationUnit unit) {
        final List<MethodDeclaration> methods = new ArrayList<>();
        // TODO: Methods of sub-classes are not returned

        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                methods.addAll(getMethods((TypeDeclaration) abstType));

            } else if (abstType instanceof EnumDeclaration) {
                final EnumDeclaration enumDecl = (EnumDeclaration) abstType;

                final List<BodyDeclaration> bodies = cast(enumDecl.bodyDeclarations(), BodyDeclaration.class);

                for (final BodyDeclaration body : bodies) {
                    if (body instanceof MethodDeclaration) {
                        methods.add((MethodDeclaration) body);
                    }
                }

            } else if (abstType instanceof AnnotationTypeDeclaration) {
                final AnnotationTypeDeclaration anno = (AnnotationTypeDeclaration) abstType;

                final List<BodyDeclaration> bodies = cast(anno.bodyDeclarations(), BodyDeclaration.class);

                for (final BodyDeclaration body : bodies) {
                    if (body instanceof MethodDeclaration) {
                        methods.add((MethodDeclaration) body);
                    }
                }
            }
        }

        return methods;
    }

    public static List<MethodDeclaration> getMethods(final TypeDeclaration type) {
        return List.of(type.getMethods());
    }

    public static List<IMethodBinding> getMethods(final Type type) {
        final ITypeBinding binding = type.resolveBinding();
        if (binding == null) {
            LOG.warn("Could not resolve type binding for \"" + type + "\". Returning empty list for getMethods");
            return List.of();
        } else {
            return List.of(binding.getDeclaredMethods());
        }
    }

    public static List<FieldDeclaration> getFields(final CompilationUnit unit) {
        final List<FieldDeclaration> fields = new ArrayList<>();

        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                fields.addAll(List.of(type.getFields()));

            } else if (abstType instanceof EnumDeclaration) {
                final EnumDeclaration enumDecl = (EnumDeclaration) abstType;

                final List<BodyDeclaration> bodies = cast(enumDecl.bodyDeclarations(), BodyDeclaration.class);

                for (final BodyDeclaration body : bodies) {
                    if (body instanceof FieldDeclaration) {
                        fields.add((FieldDeclaration) body);
                    }
                }

            } else if (abstType instanceof AnnotationTypeDeclaration) {
                final AnnotationTypeDeclaration anno = (AnnotationTypeDeclaration) abstType;

                final List<BodyDeclaration> bodies = cast(anno.bodyDeclarations(), BodyDeclaration.class);

                for (final BodyDeclaration body : bodies) {
                    if (body instanceof FieldDeclaration) {
                        fields.add((FieldDeclaration) body);
                    }
                }
            }
        }

        return fields;
    }

    public static List<SingleVariableDeclaration> getParameters(final MethodDeclaration method) {
        return cast(method.parameters(), SingleVariableDeclaration.class);
    }

    public static boolean isMethodAnnotatedWithName(final MethodDeclaration method, final String... names) {
        for (final String name : names) {
            if (isObjectAnnotatedWithName(method, name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFieldAbstract(final FieldDeclaration field) {
        final Type type = field.getType();
        final ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("field: \"" + field + "\", has type binding null => returning false for isFieldAbstract");
            return false;
        }

        return binding.isInterface();
    }

    public static boolean isParameterAbstract(final SingleVariableDeclaration parameter) {
        final Type type = parameter.getType();
        final ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("parameter: \"" + parameter + "\", has type binding null "
                    + "=> returning false for isParameterAbstract");
            return false;
        }

        return binding.isInterface();
    }

    public static boolean isParameterAClassAnnotatedWith(final SingleVariableDeclaration parameter,
            final String... names) {
        final Type type = parameter.getType();
        final ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("parameter: \"" + parameter + "\", has type binding null "
                    + "=> returning false for isParameterAClassAnnotatedWith");
            return false;
        }

        for (final String name : names) {
            for (final IAnnotationBinding anno : binding.getAnnotations()) {
                if (anno.getName()
                    .equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isFieldModifiedExactlyWith(final BodyDeclaration field, final String... names) {
        return areModifiersExactly(cast(field.modifiers(), IExtendedModifier.class), names);
    }

    private static boolean areModifiersExactly(final List<IExtendedModifier> modifiers, final String... names) {
        final Set<String> nameSet = Set.of(names)
            .stream()
            .map(x -> x.toLowerCase(Locale.US))
            .collect(Collectors.toSet());

        final long exactModifierCount = modifiers.stream()
            .filter(IExtendedModifier::isModifier)
            .map(Modifier.class::cast)
            .map(x -> x.getKeyword()
                .toString()
                .toLowerCase(Locale.US))
            .filter(nameSet::contains)
            .count();

        return exactModifierCount == names.length;
    }

    public static boolean isParameterAnnotatedWith(final SingleVariableDeclaration parameter, final String name) {
        return isObjectAnnotatedWithName(parameter, name);
    }

    public static boolean isUnitNamedWith(final CompilationUnit unit, final String name) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration type : types) {
            if (type.getName()
                .getFullyQualifiedName()
                .equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isUnitAnEnum(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration type : types) {
            if (type instanceof EnumDeclaration) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns everything listed after "implements" and "extends" in the type declaration that is
     * either abstract or an interface.
     */
    public static List<Type> getAllAbstractParents(final CompilationUnit unit) {
        final List<Type> interfaces = new ArrayList<>();

        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration type) {
                final List<Type> interfaceTypes = cast(type.superInterfaceTypes(), Type.class);
                interfaces.addAll(interfaceTypes);

                Type superclassType = type.getSuperclassType();
                if (superclassType == null) {
                    continue;
                }
                ITypeBinding superclassBinding = superclassType.resolveBinding();
                if (superclassBinding == null) {
                    continue;
                }
                if ((superclassBinding.getModifiers() & IModifierConstants.ACC_ABSTRACT) != 0) {
                    interfaces.add(superclassType);
                }
            }
        }

        return interfaces;
    }

    public static boolean isFieldAnnotatedWithName(final BodyDeclaration field, final String name) {
        return containsAnnotationWithName(cast(field.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isClassImplementing(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface() && (!type.superInterfaceTypes()
                    .isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isImplementingOrExtending(final CompilationUnit unit, final String ifaceName) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                if (isImplementingOrExtending(type.resolveBinding(), ifaceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isImplementingOrExtending(final ITypeBinding binding, final String ifaceName) {
        if (binding == null) {
            LOG.warn(
                    "binding is null => returning false for isImplementingOrExtending(binding, \"" + ifaceName + "\")");
            return false;
        }

        if (equalsWithGeneric(binding.getName(), ifaceName)) {
            return true;
        }
        final ITypeBinding superClass = binding.getSuperclass();
        if (superClass != null && isImplementingOrExtending(superClass, ifaceName)) {
            return true;
        }
        for (final ITypeBinding type : binding.getInterfaces()) {
            if (equalsWithGeneric(type.getName(), ifaceName) || isImplementingOrExtending(type, ifaceName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalsWithGeneric(final String withGeneric, final String withoutGeneric) {
        if (!withGeneric.startsWith(withoutGeneric)) {
            return false;
        }
        final String rest = withGeneric.substring(withoutGeneric.length());
        if (!rest.isEmpty() && (!rest.startsWith("<") || !rest.endsWith(">"))) {
            return false;
        }
        return true;
    }

    public static boolean isClassExtending(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface() && (type.getSuperclassType() != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Type getExtends(final CompilationUnit unit) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface()) {
                    final Type superclass = type.getSuperclassType();
                    if (superclass != null) {
                        return superclass;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isClassModifiedExactlyWith(final CompilationUnit unit, final String... names) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                final TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface()) {
                    return areModifiersExactly(cast(type.modifiers(), IExtendedModifier.class), names);
                }
            }
        }
        return false;
    }

    public static boolean isMethodModifiedExactlyWith(final BodyDeclaration method, final String... names) {
        return areModifiersExactly(cast(method.modifiers(), IExtendedModifier.class), names);
    }

    public static List<MethodDeclaration> getAllPublicMethods(final CompilationUnit unit) {
        return getMethods(unit).stream()
            .filter(MethodDeclaration::isConstructor)
            .filter(x -> cast(x.modifiers(), IExtendedModifier.class).stream()
                .filter(IExtendedModifier::isModifier)
                .map(Modifier.class::cast)
                .anyMatch(Modifier::isPublic))
            .collect(Collectors.toList());
    }

    public static List<MethodDeclaration> getConstructors(final CompilationUnit unit) {
        return getMethods(unit).stream()
            .filter(MethodDeclaration::isConstructor)
            .collect(Collectors.toList());
    }

    public static boolean isConstructorAnnotatedWithName(final MethodDeclaration constructor, final String name) {
        if (!constructor.isConstructor()) {
            return false;
        }
        return isMethodAnnotatedWithName(constructor, name);
    }

    public static boolean isClassOfFieldAnnotatedWithName(final FieldDeclaration field, final String... names) {
        final ITypeBinding binding = field.getType()
            .resolveBinding();
        if (binding == null) {
            LOG.warn("field: could not resolve type binding for \"" + field.getType()
                    + "\" => returning false from isClassOfFieldAnnotatedWithName");
            return false;
        }
        final IAnnotationBinding[] annotations = binding.getAnnotations();
        final Set<String> uniqueNames = Set.of(names);

        return List.of(annotations)
            .stream()
            .map(IAnnotationBinding::getName)
            .anyMatch(uniqueNames::contains);
    }

    public static String getMethodAnnotationStringValue(final MethodDeclaration method, final String annotation) {
        return getMethodAnnotationStringValue(method, annotation, "value");
    }

    public static String getMethodAnnotationStringValue(final MethodDeclaration method, final String annotationName,
            final String memberName) {
        final List<Annotation> annotations = cast(method.modifiers(), IExtendedModifier.class).stream()
            .filter(x -> x.isAnnotation())
            .map(Annotation.class::cast)
            .filter(x -> x.getTypeName()
                .getFullyQualifiedName()
                .endsWith(annotationName))
            .collect(Collectors.toList());

        for (final Annotation annotation : annotations) {

            Expression expression = null;
            if (annotation.isSingleMemberAnnotation()) {
                final SingleMemberAnnotation smAnnotation = (SingleMemberAnnotation) annotation;
                expression = smAnnotation.getValue();
            } else if (annotation.isNormalAnnotation()) {
                final NormalAnnotation nAnnotation = (NormalAnnotation) annotation;
                expression = cast(nAnnotation.values(), MemberValuePair.class).stream()
                    .filter(x -> x.getName()
                        .getIdentifier()
                        .equals(memberName))
                    .map(x -> x.getValue())
                    .findFirst()
                    .orElse(null);
            } else if (annotation.isMarkerAnnotation()) {
                return "";
            }

            if (expression == null) {
                return "";
            }

            if (expression instanceof StringLiteral) {
                return ((StringLiteral) expression).getLiteralValue();
            }

            final Object compiledValue = expression.resolveConstantExpressionValue();
            if (compiledValue != null) {
                return compiledValue.toString();
            }

        }

        return null;
    }

    public static String getUnitAnnotationStringValue(final CompilationUnit unit, final String annotation) {
        return getUnitAnnotationStringValue(unit, annotation, "value");
    }

    public static String getUnitAnnotationStringValue(final CompilationUnit unit, final String annotationName,
            final String memberName) {
        final List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        final List<Annotation> annotations = types.stream()
            .map(y -> cast(y.modifiers(), IExtendedModifier.class).stream()
                .filter(x -> x.isAnnotation())
                .map(Annotation.class::cast)
                .filter(x -> x.getTypeName()
                    .getFullyQualifiedName()
                    .endsWith(annotationName))
                .collect(Collectors.toList()))
            .collect(() -> new ArrayList<>(), (acc, x) -> acc.addAll(x), (acc1, acc2) -> acc1.addAll(acc2));

        for (final Annotation annotation : annotations) {

            Expression expression = null;
            if (annotation.isSingleMemberAnnotation()) {
                final SingleMemberAnnotation smAnnotation = (SingleMemberAnnotation) annotation;
                expression = smAnnotation.getValue();
            } else if (annotation.isNormalAnnotation()) {
                final NormalAnnotation nAnnotation = (NormalAnnotation) annotation;
                expression = cast(nAnnotation.values(), MemberValuePair.class).stream()
                    .filter(x -> x.getName()
                        .getIdentifier()
                        .equals(memberName))
                    .map(x -> x.getValue())
                    .findFirst()
                    .orElse(null);
            }

            if (expression == null) {
                continue;
            }

            if (expression instanceof StringLiteral) {
                return ((StringLiteral) expression).getLiteralValue();
            }
        }

        return null;
    }

    // Concentrate the warnings to this single method. It is necessary due to the
    // Eclipse JDT DOM API.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> List<T> cast(final List list, final Class<T> clazz) {
        if (!list.isEmpty() && !clazz.isInstance(list.get(0))) {
            throw new ClassCastException("Illegal cast in EclipseRuleHelper!" + "\n" + list.get(0)
                .getClass() + " -> " + clazz);
        }
        return list;
    }
}