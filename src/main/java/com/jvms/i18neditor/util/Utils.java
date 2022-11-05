package com.jvms.i18neditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.*;
import com.jvms.i18neditor.swing.util.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Utils {
    private final static Logger log = LoggerFactory.getLogger(Editor.class);

    public static Optional<String> getExtension(File file) {
        String filename = file.getName();
        return Optional.of(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private static TranslationTreeNode analizeJson(EditorProject project, Editor editor, File dir) {
        Optional<ResourceType> type = Optional.ofNullable(project.getResourceType());
        List<Resource> resourceList = null;
        try {
            resourceList = Resources.get(dir.toPath(),
                    project.getResourceFileDefinition(), project.getResourceFileStructure(), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> keys = Maps.newTreeMap();


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

        return new TranslationTreeNode(dir.getParentFile().getName(), Lists.newArrayList(keys.keySet()), TypeFile.FOLDER);


    }

    private static void showError(String message) {
        Dialogs.showErrorDialog(null, MessageBundle.get("dialogs.error.title"), message);
    }

    public static TranslationTreeNode createTreeByDir(EditorProject project, Editor editor, File node) {
        //Create the node as usually
        TranslationTreeNode ret = new TranslationTreeNode(node.getName(), TypeFile.FOLDER);

        //If exist the i18n need to override the default node to eliminate de i18n folder in tree
        if (new File(node, "i18n").exists()) {
            ret = analizeJson(project, editor, new File(node, "i18n"));
        }

        //If is directory iterate for the childs
        if (node.isDirectory()) {

            for (File child : Objects.requireNonNull(node.listFiles())) {

                //If is is a directory and not contain i18n (because we analixed that before ) create a structure recursively
                if (child.isDirectory() && !child.getName().equals("i18n")) {
                 TranslationTreeNode noda =   createTreeByDir(project, editor, child);
                 //If it is null it means that some error occurred and it is not necessary to rebuild the ui
                 if(noda == null){
                     return null;
                 }
                    ret.add(noda);
                }

                //If is not a directory must be a json file or a metadata or the folder not have a valid structure
                if (!child.isDirectory() && !child.getName().equals(".i18n-editor-metadata") && !Utils.getExtension(child).equals("json")) {
                    showError(MessageBundle.get("dialogs.notvalid.folderstructure", child.getAbsolutePath()));
                    return null;
                }

            }

        }
        return ret;
    }
}
