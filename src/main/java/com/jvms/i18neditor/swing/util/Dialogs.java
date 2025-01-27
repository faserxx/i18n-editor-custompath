package com.jvms.i18neditor.swing.util;

import com.google.common.base.Strings;
import com.jvms.i18neditor.swing.JHelpLabel;
import com.jvms.i18neditor.swing.JHtmlPane;
import com.jvms.i18neditor.swing.JTextField;
import com.jvms.i18neditor.swing.event.RequestInitialFocusListener;
import com.jvms.i18neditor.swing.text.BlinkCaret;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;

/**
 * This class provides utility functions for dialogs using {@link JOptionPane}.
 *
 * @author Jacob van Mourik
 */
public final class Dialogs {


    private Dialogs() {
        throw new IllegalStateException("Utility class");
    }

    public static void showErrorDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarningDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfoDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showComponentDialog(Component parent, String title, Component component) {
        JOptionPane.showMessageDialog(parent, component, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showHtmlDialog(Component parent, String title, String body) {
        Font font = parent.getFont();
        JHtmlPane pane = new JHtmlPane(parent, "<html><body style=\"font-family:" +
                font.getFamily() + ";text-align:center;\">" + body + "</body></html>");
        pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        showComponentDialog(parent, title, pane);
    }

    public static boolean showConfirmDialog(Component parent, String title, String message, int type) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, type) == JOptionPane.YES_OPTION;
    }

    public static String showInputDialog(Component parent, String title, String label, String help, int type) {
        return showInputDialog(parent, title, label, help, type, null, new BlinkCaret());
    }

    public static String showInputDialog(Component parent, String title, String label, String help, int type, String initialText, Caret caret) {
        JPanel content = new JPanel(new GridLayout(0, 1));

        if (!Strings.isNullOrEmpty(label)) {
            content.add(new JLabel(label));
        }

        JTextField field = new JTextField(initialText);
        field.addAncestorListener(new RequestInitialFocusListener());
        field.setCaret(caret);
        if (initialText != null) {
            field.setCaretPosition(initialText.length());
        }
        content.add(field);

        if (!Strings.isNullOrEmpty(help)) {
            content.add(new JHelpLabel(help));
        }

        JPanel container = new JPanel(new GridBagLayout());
        container.add(content);

        int result = JOptionPane.showConfirmDialog(parent, container, title, JOptionPane.OK_CANCEL_OPTION, type);
        return result == JOptionPane.OK_OPTION ? field.getText() : null;
    }
}