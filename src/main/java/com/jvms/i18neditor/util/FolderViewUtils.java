package com.jvms.i18neditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FolderViewUtils {

    private FolderViewUtils() {
        throw new IllegalStateException("Utility class");
    }
    private static final Logger log = LoggerFactory.getLogger(FolderViewUtils.class);
    public static  Pair<Boolean, TranslationTreeNode> analizeAndCreateTree(Editor editor, EditorProject project, Path dir, Path dirLanguage, TranslationTree translationTree, EditorSettings settings){
        //Create the Nodes with the folder structure
        Pair<Boolean, TranslationTreeNode> createDir = createTreeByDir(project, editor, dir.toFile(), dirLanguage);

        //if it is null it means that some error occurred and it is not necessary to rebuild the ui
        if (createDir != null) {
            translationTree.setModel(new TranslationTreeModel(createDir.second));
            settings.setCloseInError(false);
            if (Boolean.TRUE.equals(createDir.first)) {
                Utils.showError(MessageBundle.get("dialog.json.invalid"));
            }
        } else {

            //Delete from history the access
            project = null;
            settings.setCloseInError(true);

        }
        return createDir;
    }

    private static Pair<Boolean, TranslationTreeNode> analizeJson(EditorProject project, Editor editor, File dir, Path dirLenguage) throws JsonSyntaxException {
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
                    if (dirLenguage == null || resource.getPath().toString().equals(dirLenguage.toString())) {
                        try {

                            if (Resources.load(resource)) {
                                showErrorJson[0] = true;
                            }

                            editor.setupResource(resource);
                            project.addResource(resource);

                        } catch (IOException e) {
                            log.error("Error importing resource file " + resource.getPath(), e);
                            Utils.showError(MessageBundle.get("resources.import.error.single", resource.getPath()));
                        }
                    }
                }

        );

        project.getResources().
                stream().filter(x -> x.getPath().toAbsolutePath().toString().contains(dir.getAbsolutePath())).
                forEach(resource -> keys.putAll(resource.getTranslations()));

        return Pair.create(showErrorJson[0], new TranslationTreeNode(dir.getParentFile().getName(), Lists.newArrayList(keys.keySet()), TypeFile.FOLDER));


    }




    public static Pair<Boolean, TranslationTreeNode> createTreeByDir(EditorProject project, Editor editor, File node, Path dirLanguage) {

        //This boolean is to show if there is any json that does not comply with the structure
        boolean showErrorJsonMalformed = false;
        //Create the node as usually
        TranslationTreeNode ret = new TranslationTreeNode(node.getName(), TypeFile.FOLDER);

        //If exist the i18n need to override the default node to eliminate de i18n folder in tree
        if (new File(node, "i18n").exists()) {


            Pair<Boolean, TranslationTreeNode> analize = analizeJson(project, editor, new File(node, "i18n"), dirLanguage);


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
                    Pair<Boolean, TranslationTreeNode> createDir = createTreeByDir(project, editor, child, dirLanguage);
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


            }

        }


        return Pair.create(showErrorJsonMalformed, ret);
    }


}
