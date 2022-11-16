package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.util.Images;
import com.jvms.i18neditor.util.TypeFile;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.HashMap;

/**
 * This class represents a default cell renderer for the translation tree.
 *
 * @author Jacob van Mourik
 */
public class TranslationTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 3511394180407171920L;
    private static final ImageIcon JSON_ICON = Images.loadFromClasspath("images/json.png");
    private static final ImageIcon FOLDER_ICON = Images.loadFromClasspath("images/icon-folder.png");
    private final Color selectionBackground;

    public TranslationTreeCellRenderer() {
        super();
        Color bg = UIManager.getColor("Panel.background");
        selectionBackground = new Color(bg.getRed(), bg.getGreen(), bg.getBlue());
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        for (MouseListener l : getMouseListeners()) {
            removeMouseListener(l);
        }
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        TranslationTreeNode node = (TranslationTreeNode) value;
        TranslationTreeModel model = (TranslationTreeModel) tree.getModel();
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        l.setOpaque(true);
        l.setForeground(tree.getForeground());
        l.setBackground(tree.getBackground());
        if (!node.isRoot() && (node.hasError() || model.hasErrorChildNode(node))) {
            l.setIcon(new TranslationTreeStatusIcon(TranslationTreeStatusIcon.StatusIconType.WARNING));
        }
        HashMap<TypeFile, ImageIcon> type = new HashMap<>();
        type.put(TypeFile.FOLDER, FOLDER_ICON);
        type.put(TypeFile.JSON, JSON_ICON);
        type.put(TypeFile.ELEMENT, null);
        l.setIcon(type.get(node.getTypeFile()));


        if (selected) {
            l.setBackground(selectionBackground);
        }
        return l;
    }
}
