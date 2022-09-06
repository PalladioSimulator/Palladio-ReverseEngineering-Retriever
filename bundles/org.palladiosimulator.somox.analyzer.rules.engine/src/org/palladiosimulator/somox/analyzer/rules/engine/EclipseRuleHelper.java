package org.palladiosimulator.somox.analyzer.rules.engine;

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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * This class is used as a supporting library for writing rules for the rule engine. It contains
 * numerous methods to query a certain state of a java model instance. For example, is a class is
 * annotated with a specific annotation name. Also the helper contains methods for retrieving
 * aspects of a class like the interfaces it is implementing.
 *
 * @author Florian Bossert
 */
public class EclipseRuleHelper {

    private static final Logger LOG = Logger.getLogger(EclipseRuleHelper.class);

    public static boolean isAbstraction(CompilationUnit unit) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if ((abstType instanceof TypeDeclaration) && ((TypeDeclaration) abstType).isInterface()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUnitAnnotatedWithName(CompilationUnit unit, String... names) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final String name : names) {
            for (final AbstractTypeDeclaration abstType : types) {
                if (isClassifierAnnotatedWithName(abstType, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isObjectAnnotatedWithName(BodyDeclaration body, String name) {
        return containsAnnotationWithName(cast(body.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(SingleVariableDeclaration parameter, String name) {
        return containsAnnotationWithName(cast(parameter.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(TypeParameter parameter, String name) {
        return containsAnnotationWithName(cast(parameter.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(VariableDeclarationExpression expression, String name) {
        return containsAnnotationWithName(cast(expression.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isObjectAnnotatedWithName(VariableDeclarationStatement statement, String name) {
        return containsAnnotationWithName(cast(statement.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isClassifierAnnotatedWithName(BodyDeclaration abstTypeDecl, String name) {
        return containsAnnotationWithName(cast(abstTypeDecl.modifiers(), IExtendedModifier.class), name);
    }

    private static boolean containsAnnotationWithName(List<IExtendedModifier> modifiers, String name) {
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

    public static List<MethodDeclaration> getMethods(CompilationUnit unit) {
        final List<MethodDeclaration> methods = new ArrayList<>();
        // TODO: Methods of sub-classes are not returned

        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                methods.addAll(getMethods((TypeDeclaration) abstType));

            } else if (abstType instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) abstType;

                List<BodyDeclaration> bodies = cast(enumDecl.bodyDeclarations(), BodyDeclaration.class);

                for (BodyDeclaration body : bodies) {
                    if (body instanceof MethodDeclaration) {
                        methods.add((MethodDeclaration) body);
                    }
                }

            } else if (abstType instanceof AnnotationTypeDeclaration) {
                AnnotationTypeDeclaration anno = (AnnotationTypeDeclaration) abstType;

                List<BodyDeclaration> bodies = cast(anno.bodyDeclarations(), BodyDeclaration.class);

                for (BodyDeclaration body : bodies) {
                    if (body instanceof MethodDeclaration) {
                        methods.add((MethodDeclaration) body);
                    }
                }
            }
        }

        return methods;
    }

    public static List<MethodDeclaration> getMethods(TypeDeclaration type) {
        return List.of(type.getMethods());
    }

    public static List<IMethodBinding> getMethods(Type type) {
        return List.of(type.resolveBinding()
            .getDeclaredMethods());
    }

    public static List<FieldDeclaration> getFields(CompilationUnit unit) {
        final List<FieldDeclaration> fields = new ArrayList<>();

        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (final AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;
                fields.addAll(List.of(type.getFields()));

            } else if (abstType instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) abstType;

                List<BodyDeclaration> bodies = cast(enumDecl.bodyDeclarations(), BodyDeclaration.class);

                for (BodyDeclaration body : bodies) {
                    if (body instanceof FieldDeclaration) {
                        fields.add((FieldDeclaration) body);
                    }
                }

            } else if (abstType instanceof AnnotationTypeDeclaration) {
                AnnotationTypeDeclaration anno = (AnnotationTypeDeclaration) abstType;

                List<BodyDeclaration> bodies = cast(anno.bodyDeclarations(), BodyDeclaration.class);

                for (BodyDeclaration body : bodies) {
                    if (body instanceof FieldDeclaration) {
                        fields.add((FieldDeclaration) body);
                    }
                }
            }
        }

        return fields;
    }

    public static boolean isMethodAnnotatedWithName(MethodDeclaration method, String... names) {
        for (String name : names) {
            if (isObjectAnnotatedWithName(method, name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFieldAbstract(FieldDeclaration field) {
        Type type = field.getType();
        ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("field: \"" + field + "\", has type binding null => returning false for isFieldAbstract");
            return false;
        }

        return binding.isInterface();
    }

    public static boolean isParameterAbstract(SingleVariableDeclaration parameter) {
        Type type = parameter.getType();
        ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("parameter: \"" + parameter + "\", has type binding null "
                    + "=> returning false for isParameterAbstract");
            return false;
        }

        return binding.isInterface();
    }

    public static boolean isParameterAClassAnnotatedWith(SingleVariableDeclaration parameter, String... names) {
        Type type = parameter.getType();
        ITypeBinding binding = type.resolveBinding();

        if (binding == null) {
            LOG.warn("parameter: \"" + parameter + "\", has type binding null "
                    + "=> returning false for isParameterAClassAnnotatedWith");
            return false;
        }

        for (final String name : names) {
            for (IAnnotationBinding anno : binding.getAnnotations()) {
                if (anno.getName()
                    .equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isFieldModifiedExactlyWith(BodyDeclaration field, String... names) {
        return areModifiersExactly(cast(field.modifiers(), IExtendedModifier.class), names);
    }

    private static boolean areModifiersExactly(List<IExtendedModifier> modifiers, String... names) {
        Set<String> nameSet = Set.of(names)
            .stream()
            .map(x -> x.toLowerCase(Locale.US))
            .collect(Collectors.toSet());

        long exactModifierCount = modifiers.stream()
            .filter(IExtendedModifier::isModifier)
            .map(Modifier.class::cast)
            .map(x -> x.getKeyword()
                .toString()
                .toLowerCase(Locale.US))
            .filter(nameSet::contains)
            .count();

        return exactModifierCount == names.length;
    }

    public static boolean isParameterAnnotatedWith(SingleVariableDeclaration parameter, String name) {
        return isObjectAnnotatedWithName(parameter, name);
    }

    public static boolean isUnitNamedWith(CompilationUnit unit, String name) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration type : types) {
            if (type.getName()
                .getFullyQualifiedName()
                .equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isUnitAnEnum(CompilationUnit unit) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration type : types) {
            if (type instanceof EnumDeclaration) {
                return true;
            }
        }
        return false;
    }

    public static List<Type> getAllInterfaces(CompilationUnit unit) {
        List<Type> interfaces = new ArrayList<>();

        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;

                List<Type> interfaceTypes = cast(type.superInterfaceTypes(), Type.class);
                interfaces.addAll(interfaceTypes);
            }
        }

        return interfaces;
    }

    public static boolean isFieldAnnotatedWithName(BodyDeclaration field, String name) {
        return containsAnnotationWithName(cast(field.modifiers(), IExtendedModifier.class), name);
    }

    public static boolean isClassImplementing(CompilationUnit unit) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface() && (!type.superInterfaceTypes().isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isClassExtending(CompilationUnit unit) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface() && (type.getSuperclassType() != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Type getExtends(CompilationUnit unit) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface()) {
                    Type superclass = type.getSuperclassType();
                    if (superclass != null) {
                        return superclass;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isClassModifiedExactlyWith(CompilationUnit unit, String... names) {
        List<AbstractTypeDeclaration> types = cast(unit.types(), AbstractTypeDeclaration.class);

        for (AbstractTypeDeclaration abstType : types) {
            if (abstType instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) abstType;
                if (!type.isInterface()) {
                    return areModifiersExactly(cast(type.modifiers(), IExtendedModifier.class), names);
                }
            }
        }
        return false;
    }

    public static boolean isMethodModifiedExactlyWith(BodyDeclaration method, String... names) {
        return areModifiersExactly(cast(method.modifiers(), IExtendedModifier.class), names);
    }

    public static List<MethodDeclaration> getAllPublicMethods(CompilationUnit unit) {
        return getMethods(unit).stream()
            .filter(MethodDeclaration::isConstructor)
            .filter(x -> cast(x.modifiers(), IExtendedModifier.class).stream()
                .filter(IExtendedModifier::isModifier)
                .map(Modifier.class::cast)
                .anyMatch(Modifier::isPublic))
            .collect(Collectors.toList());
    }

    public static List<MethodDeclaration> getConstructors(CompilationUnit unit) {
        return getMethods(unit).stream()
            .filter(MethodDeclaration::isConstructor)
            .collect(Collectors.toList());
    }

    public static boolean isConstructorAnnotatedWithName(MethodDeclaration constructor, String name) {
        if (!constructor.isConstructor()) {
            return false;
        }
        return isMethodAnnotatedWithName(constructor, name);
    }

    public static boolean isClassOfFieldAnnotatedWithName(FieldDeclaration field, String... names) {
        IAnnotationBinding[] annotations = field.getType()
            .resolveBinding()
            .getAnnotations();
        Set<String> uniqueNames = Set.of(names);

        return List.of(annotations)
            .stream()
            .map(IAnnotationBinding::getName)
            .anyMatch(uniqueNames::contains);
    }

    // Concentrate the warnings to this single method. It is necessary due to the
    // Eclipse JDT DOM API.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> List<T> cast(List list, Class<T> clazz) {
        if (!list.isEmpty() && !clazz.isInstance(list.get(0))) {
            throw new ClassCastException("Illegal cast in EclipseRuleHelper!" + "\n" + list.get(0)
                .getClass() + " -> " + clazz);
        }
        return list;
    }
}