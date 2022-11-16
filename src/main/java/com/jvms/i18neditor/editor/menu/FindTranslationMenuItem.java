package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for searching a translation key.
 *
 * @author Jacob van Mourik
 */
public class FindTranslationMenuItem extends JMenuItem {
    private static final long serialVersionUID = -1298283182450978961L;

    public FindTranslationMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.edit.find.translation.title"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(e -> editor.showFindTranslationDialog());
        setEnabled(enabled);
    }
}