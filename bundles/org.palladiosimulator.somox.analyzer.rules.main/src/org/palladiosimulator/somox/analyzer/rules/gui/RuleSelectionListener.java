package org.palladiosimulator.somox.analyzer.rules.gui;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;

import java.util.Set;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;

public class RuleSelectionListener extends SelectionAdapter {
    private final Button selectionButton;
    private final ModifyListener modifyListener;
    private final Set<DefaultRule> rules;
    private final DefaultRule rule;

    public RuleSelectionListener(Button selectionButton, ModifyListener modifyListener, Set<DefaultRule> rules,
            DefaultRule rule) {
        this.selectionButton = selectionButton;
        this.modifyListener = modifyListener;
        this.rules = rules;
        this.rule = rule;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (selectionButton.getSelection()) {
            rules.add(rule);
        } else {
            rules.remove(rule);
        }
        modifyListener.modifyText(null);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

}
