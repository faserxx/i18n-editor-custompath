package com.jvms.i18neditor.swing.text;

import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;

/**
 * An action implementation useful for deleting text.
 *
 * @author Jacob van Mourik
 */
public class DeleteAction extends TextAction {
    private static final long serialVersionUID = -7933405670677160997L;

    public DeleteAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent component = getFocusedComponent();
        component.replaceSelection("");
    }
}
