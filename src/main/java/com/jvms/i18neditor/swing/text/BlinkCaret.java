package com.jvms.i18neditor.swing.text;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * This class extends the {@link DefaultCaret} with a default blink rate set.
 *
 * @author Jacob van Mourik
 */
public class BlinkCaret extends DefaultCaret {
    private static final long serialVersionUID = -3365578081904749196L;

    public BlinkCaret() {
        int blinkRate = 0;
        Object o = UIManager.get("TextArea.caretBlinkRate");
        if (o != null && o instanceof Integer) {
            blinkRate = ((Integer) o).intValue();
        }
        setBlinkRate(blinkRate);
    }
}
