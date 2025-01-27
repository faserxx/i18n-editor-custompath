package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.editor.TranslationTree;
import com.jvms.i18neditor.editor.TranslationTreeNode;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for adding a new translation.
 *
 * @author Jacob van Mourik
 */
public class AddTranslationMenuItem extends JMenuItem {
    private static final long serialVersionUID = -2673278052970076105L;

    public AddTranslationMenuItem(Editor editor, TranslationTreeNode node, boolean enabled) {
        this(editor, enabled, e -> editor.showAddTranslationDialog(node));
    }

    public AddTranslationMenuItem(Editor editor, TranslationTree tree, boolean enabled) {
        this(editor, enabled, e -> editor.showAddTranslationDialog(tree.getSelectionNode()));
    }

    private AddTranslationMenuItem(Editor editor, boolean enabled, ActionListener action) {
        super(MessageBundle.get("menu.edit.add.translation.title"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(action);
        setEnabled(enabled);
    }
}