package com.jvms.i18neditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.*;
import com.jvms.i18neditor.swing.util.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Utils {
    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger log = LoggerFactory.getLogger(Utils.class);


    public static Optional<String> getExtension(File file) {
        String filename = file.getName();
        return Optional.of(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private static Pair<Boolean, TranslationTreeNode> analizeJson(EditorProject project, Editor editor, File dir) throws JsonSyntaxException {
        final boolean[] showErrorJson = {false};

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
                if (Resources.load(resource)) {
                    showErrorJson[0] = true;
                }

                editor.setupResource(resource);
                project.addResource(resource);

            } catch (IOException e) {
                log.error("Error importing resource file " + resource.getPath(), e);
                showError(MessageBundle.get("resources.import.error.single", resource.getPath()));
            }
        });

        project.getResources().forEach(r -> keys.putAll(r.getTranslations()));
        return Pair.create(showErrorJson[0], new TranslationTreeNode(dir.getParentFile().getName(), Lists.newArrayList(keys.keySet()), TypeFile.FOLDER));


    }

   public static void showError(String message) {
        Dialogs.showErrorDialog(null, MessageBundle.get("dialogs.error.title"), message);
    }

    public static Pair<Boolean, TranslationTreeNode> createTreeByDir(EditorProject project, Editor editor, File node) {

        //This boolean is to show if there is any json that does not comply with the structure
        boolean showErrorJsonMalformed = false;
        //Create the node as usually
        TranslationTreeNode ret = new TranslationTreeNode(node.getName(), TypeFile.FOLDER);

        //If exist the i18n need to override the default node to eliminate de i18n folder in tree
        if (new File(node, "i18n").exists()) {


            Pair<Boolean, TranslationTreeNode> analize = analizeJson(project, editor, new File(node, "i18n"));

            ret = analize.second;
            if (Boolean.TRUE.equals(analize.first)) {
                showErrorJsonMalformed = true;
            }


        }

        //If is directory iterate for the childs
        if (node.isDirectory()) {
            for (File child : Objects.requireNonNull(node.listFiles())) {

                //If is is a directory and not contain i18n (because we analixed that before ) create a structure recursively
                if (child.isDirectory() && !child.getName().equals("i18n")) {
                    Pair<Boolean, TranslationTreeNode> createDir = createTreeByDir(project, editor, child);
                    TranslationTreeNode noda = createDir.second;
                    if (Boolean.TRUE.equals(createDir.first)) {
                        showErrorJsonMalformed = true;
                    }
                    //If it is null it means that some error occurred and it is not necessary to rebuild the ui
                    if (noda == null) {
                        return null;
                    }
                    ret.add(noda);
                }

                //If is not a directory must be a json file or a metadata or the folder not have a valid structure
                if (!child.isDirectory() && !child.getName().equals(".i18n-editor-metadata") && !Utils.getExtension(child).equals("json")) {
                    showError(MessageBundle.get("dialogs.notvalid.folderstructure", child.getParentFile().getAbsolutePath()));
                    return null;
                }

            }

        }


        return Pair.create(showErrorJsonMalformed, ret);
    }
}
