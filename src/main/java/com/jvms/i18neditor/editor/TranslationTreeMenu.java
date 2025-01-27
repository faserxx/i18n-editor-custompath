package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.editor.menu.AddTranslationMenuItem;
import com.jvms.i18neditor.editor.menu.CollapseTranslationsMenuItem;
import com.jvms.i18neditor.editor.menu.ExpandTranslationsMenuItem;
import com.jvms.i18neditor.editor.menu.FindTranslationMenuItem;

import javax.swing.*;

/**
 * This class represents a right click menu for the translation tree.
 *
 * @author Jacob van Mourik
 */
public class TranslationTreeMenu extends JPopupMenu {
    private static final long serialVersionUID = -8450484152294368841L;

    public TranslationTreeMenu(Editor editor, TranslationTree tree) {
        super();
        add(new AddTranslationMenuItem(editor, tree, true));
        add(new FindTranslationMenuItem(editor, true));
        addSeparator();
        add(new ExpandTranslationsMenuItem(tree));
        add(new CollapseTranslationsMenuItem(tree));
    }
}
