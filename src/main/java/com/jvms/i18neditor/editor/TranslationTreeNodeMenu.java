package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.editor.menu.AddTranslationMenuItem;
import com.jvms.i18neditor.editor.menu.CopyTranslationKeyToClipboardMenuItem;
import com.jvms.i18neditor.editor.menu.RemoveTranslationMenuItem;
import com.jvms.i18neditor.editor.menu.RenameTranslationMenuItem;
import com.jvms.i18neditor.util.TypeFile;

import javax.swing.*;

/**
 * This class represents a right click menu for a single node of the translation tree.
 *
 * @author Jacob van Mourik
 */
public class TranslationTreeNodeMenu extends JPopupMenu {
    private final static long serialVersionUID = -8450484152294368841L;

    public TranslationTreeNodeMenu(Editor editor, TranslationTreeNode node) {
        super();
        add(new AddTranslationMenuItem(editor, node, true));
        if (node.typeFile != TypeFile.FOLDER || node.isRoot()) {
            addSeparator();
            add(new RenameTranslationMenuItem(editor, true));
            add(new RemoveTranslationMenuItem(editor, true));
            add(new CopyTranslationKeyToClipboardMenuItem(editor, true));
        }
    }
}
