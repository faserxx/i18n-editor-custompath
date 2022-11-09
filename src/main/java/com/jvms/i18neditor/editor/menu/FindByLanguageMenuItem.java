/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jvms.i18neditor.editor.menu;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.util.MessageBundle;
/**
 *
 * @author arianna
 */
public class FindByLanguageMenuItem extends JMenuItem {
    
    private final static long serialVersionUID = -1298283182489578961L;
    public FindByLanguageMenuItem(Editor editor, boolean enabled) {
		super(MessageBundle.get("dialogs.translation.language.find.title"));
		//setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addActionListener(e -> editor.showFindByLanguageDialog());
        setEnabled(enabled);
	}
}
