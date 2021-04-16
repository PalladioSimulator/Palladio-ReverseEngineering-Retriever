package org.palladiosimulator.somox.analyzer.rules.cli;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class RuleEngineApplication implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {
        final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

}
