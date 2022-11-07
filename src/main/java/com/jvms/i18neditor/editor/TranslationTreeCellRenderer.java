package com.jvms.i18neditor.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.lang.reflect.Type;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.jvms.i18neditor.util.Images;
import com.jvms.i18neditor.util.TypeFile;

/**
 * This class represents a default cell renderer for the translation tree.
 * 
 * @author Jacob van Mourik
 */
public class TranslationTreeCellRenderer extends DefaultTreeCellRenderer {
	private final static long serialVersionUID = 3511394180407171920L;
	private final static ImageIcon JSON_ICON = Images.loadFromClasspath("images/json.png");
	private final static ImageIcon FOLDER_ICON = Images.loadFromClasspath("images/icon-folder.png");
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
        	l.setIcon(new TranslationTreeStatusIcon(TranslationTreeStatusIcon.StatusIconType.Warning));
        }
		HashMap<TypeFile,ImageIcon> type = new HashMap<>();
		type.put(TypeFile.FOLDER,FOLDER_ICON);
		type.put(TypeFile.JSON,JSON_ICON);
		type.put(TypeFile.ELEMENT,null);
		l.setIcon(type.get(node.typeFile));


        if (selected) {
        	l.setBackground(selectionBackground);
        }
        return l;
    }
}
