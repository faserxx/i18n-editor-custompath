package com.jvms.i18neditor.swing;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

/**
 * This class extends a default {@link JEditorPane} with default settings for HTML content.
 *
 * @author Jacob van Mourik
 */
public class JHtmlPane extends JEditorPane {
    private static final long serialVersionUID = 2873290055720408299L;

    public JHtmlPane(Component parent, String content) {
        super("text/html", content);
        setEditable(false);
        setBackground(parent.getBackground());
        addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception e1) {
                    //
                }
            }
        });
    }
}
