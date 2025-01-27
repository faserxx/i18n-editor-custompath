package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.TranslationTree;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;

/**
 * This class represents a menu item for collapsing all keys of the translation tree.
 *
 * @author Jacob van Mourik
 */
public class CollapseTranslationsMenuItem extends JMenuItem {
    private static final long serialVersionUID = 7885728865417192564L;

    public CollapseTranslationsMenuItem(TranslationTree tree) {
        super(MessageBundle.get("menu.view.collapse.title"));
        addActionListener(e -> tree.collapseAll());
    }
}