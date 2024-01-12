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

    public TreeEditListener(final Tree tree, final ModifyListener modifyListener, final int column) {
        this.tree = tree;
        this.modifyListener = modifyListener;
        this.column = column;
        this.editor = new TreeEditor(tree);
    }

    // Editable TreeItems adapted from
    // https://git.eclipse.org/c/platform/eclipse.platform.swt.git/
    // plain/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet111.java
    @Override
    public void handleEvent(final Event event) {
        final TreeItem item = (TreeItem) event.item;
        if ((item != null) && (item == this.lastItem[0]) && (item.getParentItem() != null)) {
            final boolean showBorder = true;
            final Composite composite = new Composite(this.tree, SWT.NONE);
            if (showBorder) {
                composite.setBackground(new Color(0, 0, 0));
            }
            final Text text = new Text(composite, SWT.NONE);
            final int inset = showBorder ? 1 : 0;
            composite.addListener(SWT.Resize, e1 -> {
                final Rectangle rect1 = composite.getClientArea();
                text.setBounds(rect1.x + inset, rect1.y + inset, rect1.width - (inset * 2), rect1.height - (inset * 2));
            });
            final Listener textListener = e2 -> {
                switch (e2.type) {
                case SWT.FocusOut:
                    item.setText(this.column, text.getText());
                    this.modifyListener.modifyText(null);
                    composite.dispose();
                    break;
                case SWT.Verify:
                    final String newText = text.getText();
                    final String leftText = newText.substring(0, e2.start);
                    final String rightText = newText.substring(e2.end);
                    final GC gc = new GC(text);
                    Point size = gc.textExtent(leftText + e2.text + rightText);
                    gc.dispose();
                    size = text.computeSize(size.x, SWT.DEFAULT);
                    this.editor.horizontalAlignment = SWT.LEFT;
                    this.editor.setColumn(this.column);
                    final Rectangle itemRect = item.getBounds(this.column), rect2 = this.tree.getClientArea();
                    this.editor.minimumWidth = Math.max(size.x, itemRect.width) + (inset * 2);
                    final int left = itemRect.x, right = rect2.x + rect2.width;
                    this.editor.minimumWidth = Math.min(this.editor.minimumWidth, right - left);
                    this.editor.minimumHeight = size.y + (inset * 2);
                    this.editor.layout();
                    break;
                case SWT.Traverse:
                    switch (e2.detail) {
                    case SWT.TRAVERSE_RETURN:
                        item.setText(this.column, text.getText());
                        this.modifyListener.modifyText(null);
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
            this.editor.setEditor(composite, item);
            text.setText(item.getText(this.column));
            text.selectAll();
            text.setFocus();
        }
        this.lastItem[0] = item;
    }

}
