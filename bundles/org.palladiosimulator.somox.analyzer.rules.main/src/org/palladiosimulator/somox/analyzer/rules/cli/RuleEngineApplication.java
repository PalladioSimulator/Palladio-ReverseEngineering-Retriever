package org.palladiosimulator.somox.analyzer.rules.cli;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.nio.file.Path;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;

public class RuleEngineApplication implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        final Path in = Paths.get(args[0]);
        final Path out = Paths.get(args[1]);
        final List<CompilationUnitImpl> roots = ParserAdapter.generateModelForProject(in);
        final Set<DefaultRule> rules = Set.of(DefaultRule.values());
        
        RuleEngineAnalyzer.executeWith(in, out, roots, rules);
        
        return null;
    }

    @Override
    public void stop() {
    }

}
