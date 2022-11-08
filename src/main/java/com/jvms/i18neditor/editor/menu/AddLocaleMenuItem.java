package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for adding a new locale.
 *
 * @author Jacob van Mourik
 */
public class AddLocaleMenuItem extends JMenuItem {
    private static final long serialVersionUID = -5108677891532028898L;

    public AddLocaleMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.edit.add.locale.title"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(e -> editor.showAddLocaleDialog());
        setEnabled(enabled);
    }
}