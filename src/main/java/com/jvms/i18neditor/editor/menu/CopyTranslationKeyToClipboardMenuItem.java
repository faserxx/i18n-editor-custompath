package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for copying a translations key to the system clipboard.
 *
 * @author Fabian Terstegen
 */
public class CopyTranslationKeyToClipboardMenuItem extends JMenuItem {
    private static final long serialVersionUID = 6032182493888769724L;

    public CopyTranslationKeyToClipboardMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.edit.copy.key.title"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(e -> editor.copySelectedTranslationKey());
        setEnabled(enabled);
    }

}
