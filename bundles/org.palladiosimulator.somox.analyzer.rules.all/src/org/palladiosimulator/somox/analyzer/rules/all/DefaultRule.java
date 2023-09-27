package org.palladiosimulator.somox.analyzer.rules.all;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.docker.DockerRules;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
import org.palladiosimulator.somox.analyzer.rules.gradle.GradleRules;
import org.palladiosimulator.somox.analyzer.rules.impl.JaxRSRules;
import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;
import org.palladiosimulator.somox.analyzer.rules.impl.EcmaScriptRules;
import org.palladiosimulator.somox.analyzer.rules.maven.MavenRules;

/**
 * This enum contains all default rule technologies the rule engine provides
 */
public enum DefaultRule {

    SPRING(SpringRules.class),
    JAX_RS(JaxRSRules.class),
    MAVEN(MavenRules.class),
    GRADLE(GradleRules.class),
    DOCKER(DockerRules.class),
    ECMASCRIPT(EcmaScriptRules.class);

    private final Class<? extends IRule> ruleClass;

    DefaultRule(Class<? extends IRule> ruleClass) {
        this.ruleClass = ruleClass;
    }

    /**
     * Returns the names of all currently available default rule technologies.
     *
     * @return the names of all available default rule technologies
     * @see Image
     */
    public static String[] valuesAsString() {
        String[] names = new String[DefaultRule.values().length];
        for (int i = 0; i < DefaultRule.values().length; i++) {
            names[i] = DefaultRule.values()[i].name();
        }

        return names;
    }

    public IRule getRule(RuleEngineBlackboard blackboard) {
        try {
            return ruleClass.getDeclaredConstructor(RuleEngineBlackboard.class)
                .newInstance(blackboard);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            // TODO Maybe solve this a little bit better..?
            e.printStackTrace();
        }
        return null;
    }

}
