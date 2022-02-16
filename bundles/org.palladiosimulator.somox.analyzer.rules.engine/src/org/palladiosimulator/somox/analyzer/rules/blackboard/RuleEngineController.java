package org.palladiosimulator.somox.analyzer.rules.blackboard;

import java.util.LinkedList;

import org.somox.configuration.ConfigurableComponent;
import org.somox.configuration.ConfigurationDefinition;

public class RuleEngineController implements ConfigurableComponent {

    private final LinkedList<ConfigurationDefinition> configurations;
    private final RuleEngineBlackboard blackboard;

    public RuleEngineController() {
        configurations = new LinkedList<>();
        blackboard = new RuleEngineBlackboard();
    }

    @Override
    public LinkedList<ConfigurationDefinition> getConfigurationDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

}
