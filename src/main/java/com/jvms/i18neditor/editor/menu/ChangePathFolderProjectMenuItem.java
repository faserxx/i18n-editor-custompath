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
public class ChangePathFolderProjectMenuItem extends JMenuItem {
	private final static long serialVersionUID = -907122077814626287L;

	public ChangePathFolderProjectMenuItem(Editor editor, boolean enabled) {
        super(MessageBundle.get("menu.edit.change.path.folder"));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		addActionListener(e -> editor.changePathFolder());
		setEnabled(enabled);
	}
}