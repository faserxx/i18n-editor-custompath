package com.jvms.i18neditor.swing.text;

import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;

/**
 * An action implementation useful for selecting all text.
 *
 * @author Jacob van Mourik
 */
public class SelectAllAction extends TextAction {
    private static final long serialVersionUID = -4913270947629733919L;

    public SelectAllAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent component = getFocusedComponent();
        component.selectAll();
    }
}
