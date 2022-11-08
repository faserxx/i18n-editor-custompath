package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.swing.util.Dialogs;
import com.jvms.i18neditor.util.MessageBundle;
import com.jvms.i18neditor.util.ResourceKeys;
import com.jvms.i18neditor.util.TypeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents a single node of the translation tree.
 *
 * @author Jacob van Mourik
 */
public class TranslationTreeNode extends DefaultMutableTreeNode {
    private final static Logger log = LoggerFactory.getLogger(Editor.class);
    private final static long serialVersionUID = -7372403592538358822L;
    private String name;
    private boolean error;
    public TypeFile typeFile = TypeFile.JSON;

    private void showError(String message) {
        Dialogs.showErrorDialog(null, MessageBundle.get("dialogs.error.title"), message);
    }


    public TranslationTreeNode(String name, TypeFile typeFile) {
        this.typeFile = typeFile;
        this.name = name;
    }

    public TranslationTreeNode(String name, List<String> keys, TypeFile typeFile) {
        super();
        this.name = name;
        this.typeFile = typeFile;
        ResourceKeys.uniqueRootKeys(keys).forEach(rootKey -> {
            List<String> subKeys = ResourceKeys.extractChildKeys(keys, rootKey);

            add(new TranslationTreeNode(rootKey, subKeys));
        });
    }

    public TranslationTreeNode(String name, List<String> keys) {
        super();
        this.name = name;
        if (ResourceKeys.uniqueRootKeys(keys).size() == 0) {
            this.typeFile = TypeFile.ELEMENT;
        }

        ResourceKeys.uniqueRootKeys(keys).forEach(rootKey -> {
            List<String> subKeys = ResourceKeys.extractChildKeys(keys, rootKey);

            add(new TranslationTreeNode(rootKey, subKeys));
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean hasError() {
        return isEditable() && error;
    }

    public boolean isEditable() {
        return !isRoot();
    }

    public String getKey() {
        List<TreeNode> path = Arrays.asList(getPath());
        List<String> parts = path.stream()
                .filter(p -> !((TranslationTreeNode) p).isRoot())
                .map(p -> p.toString())
                .collect(Collectors.toList());

        return ResourceKeys.create(parts);
    }

    @SuppressWarnings("unchecked")
    public List<TranslationTreeNode> getChildren() {
        return Collections.list(children());
    }

    public TranslationTreeNode getChild(String name) {
        Optional<TranslationTreeNode> child = getChildren().stream()
                .filter(i -> i.getName().equals(name))
                .findFirst();

        return child.isPresent() ? child.get() : null;
    }

    public TranslationTreeNode cloneWithChildren() {
        return cloneWithChildren(this);
    }

    @Override
    public String toString() {
        return name;
    }

    private TranslationTreeNode cloneWithChildren(TranslationTreeNode parent) {
        TranslationTreeNode newParent = (TranslationTreeNode) parent.clone();
        for (TranslationTreeNode n : parent.getChildren()) {
            newParent.add(cloneWithChildren(n));
        }
        return newParent;
    }
}