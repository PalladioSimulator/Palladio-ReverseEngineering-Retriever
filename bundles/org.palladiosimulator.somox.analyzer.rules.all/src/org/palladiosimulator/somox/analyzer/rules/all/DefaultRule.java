package org.palladiosimulator.somox.analyzer.rules.all;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;

import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.engine.IRule;
import org.palladiosimulator.somox.analyzer.rules.impl.DockerRules;
import org.palladiosimulator.somox.analyzer.rules.impl.EcmaScriptRules;
import org.palladiosimulator.somox.analyzer.rules.impl.GradleRules;
import org.palladiosimulator.somox.analyzer.rules.impl.JaxRSRules;
import org.palladiosimulator.somox.analyzer.rules.impl.MavenRules;
import org.palladiosimulator.somox.analyzer.rules.impl.SpringRules;

/**
 * This enum contains all default rule technologies the rule engine provides
 */
public enum DefaultRule {

    SPRING(SpringRules.class, false),
    JAX_RS(JaxRSRules.class, false),
    MAVEN(MavenRules.class, true),
    GRADLE(GradleRules.class, true),
    DOCKER(DockerRules.class, true),
    ECMASCRIPT(EcmaScriptRules.class, false);

    private final Class<? extends IRule> ruleClass;
    private final boolean isBuildRule;

    DefaultRule(Class<? extends IRule> ruleClass, boolean isBuildRule) {
        this.ruleClass = ruleClass;
        this.isBuildRule = isBuildRule;
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

    public boolean isBuildRule() {
        return isBuildRule;
    }

}
