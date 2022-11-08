package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for renaming a translation key.
 *
 * @author Jacob van Mourik
 */
public class RestoreToDefaultMenuItem extends JMenuItem {
    private final static long serialVersionUID = -907122077814626281L;

    public RestoreToDefaultMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.settings.restore.default"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(e -> editor.restoreToDefaultProject());
        setEnabled(enabled);
    }
}