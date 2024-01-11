package org.palladiosimulator.retriever.core.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeEditListener implements Listener {
    private final TreeItem[] lastItem = new TreeItem[1];
    private final TreeEditor editor;
    private final Tree tree;
    private final ModifyListener modifyListener;
    private final int column;

    public TreeEditListener(Tree tree, ModifyListener modifyListener, int column) {
        this.tree = tree;
        this.modifyListener = modifyListener;
        this.column = column;
        editor = new TreeEditor(tree);
    }

    // Editable TreeItems adapted from
    // https://git.eclipse.org/c/platform/eclipse.platform.swt.git/
    // plain/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet111.java
    @Override
    public void handleEvent(Event event) {
        final TreeItem item = (TreeItem) event.item;
        if ((item != null) && (item == lastItem[0]) && (item.getParentItem() != null)) {
            boolean showBorder = true;
            final Composite composite = new Composite(tree, SWT.NONE);
            if (showBorder) {
                composite.setBackground(new Color(0, 0, 0));
            }
            final Text text = new Text(composite, SWT.NONE);
            final int inset = showBorder ? 1 : 0;
            composite.addListener(SWT.Resize, e1 -> {
                Rectangle rect1 = composite.getClientArea();
                text.setBounds(rect1.x + inset, rect1.y + inset, rect1.width - (inset * 2), rect1.height - (inset * 2));
            });
            Listener textListener = e2 -> {
                switch (e2.type) {
                case SWT.FocusOut:
                    item.setText(column, text.getText());
                    modifyListener.modifyText(null);
                    composite.dispose();
                    break;
                case SWT.Verify:
                    String newText = text.getText();
                    String leftText = newText.substring(0, e2.start);
                    String rightText = newText.substring(e2.end);
                    GC gc = new GC(text);
                    Point size = gc.textExtent(leftText + e2.text + rightText);
                    gc.dispose();
                    size = text.computeSize(size.x, SWT.DEFAULT);
                    editor.horizontalAlignment = SWT.LEFT;
                    editor.setColumn(column);
                    Rectangle itemRect = item.getBounds(column), rect2 = tree.getClientArea();
                    editor.minimumWidth = Math.max(size.x, itemRect.width) + (inset * 2);
                    int left = itemRect.x, right = rect2.x + rect2.width;
                    editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
                    editor.minimumHeight = size.y + (inset * 2);
                    editor.layout();
                    break;
                case SWT.Traverse:
                    switch (e2.detail) {
                    case SWT.TRAVERSE_RETURN:
                        item.setText(column, text.getText());
                        modifyListener.modifyText(null);
                        // FALL THROUGH
                    case SWT.TRAVERSE_ESCAPE:
                        composite.dispose();
                        e2.doit = false;
                    default:
                        break;
                    }
                    break;
                default:
                    break;
                }
            };
            text.addListener(SWT.FocusOut, textListener);
            text.addListener(SWT.Traverse, textListener);
            text.addListener(SWT.Verify, textListener);
            editor.setEditor(composite, item);
            text.setText(item.getText(column));
            text.selectAll();
            text.setFocus();
        }
        lastItem[0] = item;
    }

}
