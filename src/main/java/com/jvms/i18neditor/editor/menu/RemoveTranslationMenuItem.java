package com.jvms.i18neditor.editor.menu;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This class represents a menu item for removing a translation key.
 *
 * @author Jacob van Mourik
 */
public class RemoveTranslationMenuItem extends JMenuItem {
    private final static long serialVersionUID = 5207946396515235714L;

    public RemoveTranslationMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.edit.delete.title"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        addActionListener(e -> editor.removeSelectedTranslation());
        setEnabled(enabled);
    }
}