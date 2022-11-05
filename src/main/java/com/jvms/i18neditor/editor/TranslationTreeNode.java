package com.jvms.i18neditor.editor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.swing.util.Dialogs;
import com.jvms.i18neditor.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

private void doJSonAnalisis(EditorProject project,Editor editor,File dir) {
    Optional<ResourceType> type = Optional.ofNullable(project.getResourceType());
    List<Resource> resourceList = null;
    try {
        resourceList = Resources.get(dir.toPath(),
                project.getResourceFileDefinition(), project.getResourceFileStructure(), type);
    } catch (IOException e) {
        e.printStackTrace();
    }
    Map<String,String> keys = Maps.newTreeMap();


        resourceList.forEach(resource -> {
            try {
                Resources.load(resource);
                editor.setupResource(resource);
                project.addResource(resource);

            } catch (IOException e) {
                log.error("Error importing resource file " + resource.getPath(), e);
                showError(MessageBundle.get("resources.import.error.single", resource.getPath()));
            }
        });

        project.getResources().forEach(r -> keys.putAll(r.getTranslations()));

    add(new TranslationTreeNode(dir.getParentFile().getName(), Lists.newArrayList(keys.keySet())));


}
    private void scan(File node,EditorProject project,Editor editor) {

        if (node.isDirectory()) {

            for (File child : node.listFiles()) {
                if (child.isDirectory() && child.getName().equals("i18n")) {
                    doJSonAnalisis(project,editor,child);

                } else {
                    if (!child.isDirectory() && !child.getName().equals(".i18n-editor-metadata")  && !Utils.getExtension(child).equals("json") ) {

                       showError(  MessageBundle.get("dialogs.notvalid.folderstructure", child.getAbsolutePath()));
                       break;
                    } else if (!child.getName().equals(".i18n-editor-metadata")){
                        add(new TranslationTreeNode(project,editor,child, TypeFile.FOLDER));
                    }

                }
            }
        }

    }

    public TranslationTreeNode(EditorProject project,Editor editor,File dir, TypeFile typeFile) {
        name = dir.getName();
        this.typeFile = typeFile;
        scan(dir,project,editor);


//		super();
//		this.name = name;
//		ResourceKeys.uniqueRootKeys(keys).forEach(rootKey -> {
//			List<String> subKeys = ResourceKeys.extractChildKeys(keys, rootKey);
//			add(new TranslationTreeNode(rootKey, subKeys));
//		});
    }

    public TranslationTreeNode(String name, List<String> keys) {
        super();
        this.name = name;
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