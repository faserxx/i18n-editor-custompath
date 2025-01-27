package com.jvms.i18neditor.swing;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

/**
 * An action implementation useful for redoing an edit.
 *
 * @author Jacob van Mourik
 */
public class RedoAction extends AbstractAction {
    private static final long serialVersionUID = -3051499148079684354L;
    private final UndoManager undoManager;

    public RedoAction(UndoManager undoManager) {
        super();
        this.undoManager = undoManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }
}
