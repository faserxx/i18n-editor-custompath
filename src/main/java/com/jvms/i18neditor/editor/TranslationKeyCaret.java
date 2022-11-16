package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.swing.JTextField;
import com.jvms.i18neditor.swing.text.BlinkCaret;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class TranslationKeyCaret extends BlinkCaret {
    private static final long serialVersionUID = -4481421558690248419L;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!e.isConsumed() && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            handleDoubleClick(e);
            return;
        }
        super.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.isConsumed() && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            handleDoubleClick(e);
            return;
        }
        super.mousePressed(e);
    }

    private void handleDoubleClick(MouseEvent e) {
        JTextField field = (JTextField) e.getComponent();

        int caretPos = field.getCaretPosition();
        int start = caretPos;
        int end = caretPos;
        String text = field.getText();

        while (start > 0) {
            if (text.charAt(start - 1) == '.') {
                break;
            }
            start--;
        }
        while (end < text.length()) {
            if (text.charAt(end) == '.') {
                break;
            }
            end++;
        }

        field.setSelectionStart(start);
        field.setSelectionEnd(end);
    }
}
