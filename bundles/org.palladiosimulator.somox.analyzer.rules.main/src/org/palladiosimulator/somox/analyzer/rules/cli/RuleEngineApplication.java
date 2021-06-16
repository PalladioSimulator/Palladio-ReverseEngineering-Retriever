package org.palladiosimulator.somox.analyzer.rules.cli;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.engine.ParserAdapter;
import org.palladiosimulator.somox.analyzer.rules.main.RuleEngineAnalyzer;

public class RuleEngineApplication implements IApplication {
    
    private static final String FORMAT_EXPLANATION = "The following format is expected:"
            + "\n<input directory> <output directory> [rules]\n\nSupported rules: "
            + String.join(", ", DefaultRule.valuesAsString());

    @Override
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        
        if (args.length < 2) {
            System.err.println("Too few arguments!\n" + FORMAT_EXPLANATION);
            return -1;
        }
        
        // Extract and check path arguments
        final Path in;
        try {
            in = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: \"" + args[0] + "\"\n" + FORMAT_EXPLANATION);
            return -1;
        }
        final Path out;
        try {
            out = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: \"" + args[1] + "\"\n" + FORMAT_EXPLANATION);
            return -1;
        }
        
        // Extract and check rules
        final Set<DefaultRule> rules = new HashSet<DefaultRule>();
        for (int i = 2; i < args.length; i++) {
            try {
                rules.add(DefaultRule.valueOf(args[i]));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid rule: \"" + args[i] + "\"\n" + FORMAT_EXPLANATION);
                return -1;
            }
        }
        
        final List<CompilationUnitImpl> roots = ParserAdapter.generateModelForProject(in);
        
        RuleEngineAnalyzer.executeWith(in, out, roots, rules);
        
        return 0;
    }

    @Override
    public void stop() {
    }
}
