package com.jvms.i18neditor.swing;

import com.jvms.i18neditor.swing.text.JTextComponentMenuListener;
import com.jvms.i18neditor.util.Colors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * This class extends a default {@link javax.swing.JTextArea} with a {@link UndoManager},
 * a right click menu and a custom look and feel.
 *
 * @author Jacob van Mourik
 */
public class JTextArea extends javax.swing.JTextArea {
    private static final long serialVersionUID = -5043046809426384893L;
    protected final UndoManager undoManager = new UndoManager();

    /**
     * Constructs a {@link JTextArea}.
     */
    public JTextArea() {
        super();

        Border border = BorderFactory.createLineBorder(Colors.scale(UIManager.getColor("Panel.background"), .8f));
        setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        setAlignmentX(LEFT_ALIGNMENT);
        setLineWrap(true);
        setWrapStyleWord(true);

        // Add undo support
        getActionMap().put("undo", new UndoAction(undoManager));
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");

        // Add redo support
        getActionMap().put("redo", new RedoAction(undoManager));
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");

        // Add popup menu support
        addMouseListener(new JTextComponentMenuListener(this, undoManager));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setOpaque(enabled);
    }
}
