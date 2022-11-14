package com.jvms.i18neditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.*;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlatViewUtils {

    private FlatViewUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger log = LoggerFactory.getLogger(FolderViewUtils.class);

    public static List<Resource> showFiles(File[] files) {
        List<Resource> result = Lists.newLinkedList();

        for (File file : files) {


            if (file.isDirectory()) {

                result.addAll(showFiles(Objects.requireNonNull(file.listFiles()))); // Calls same method again.
            } else if (Utils.isLocaleAvailable(file)) {
                result.add(new Resource(ResourceType.JSON, file.toPath(), LocaleUtils.toLocale(Utils.getBaseName(file))));
            }


        }
        return result;
    }
    public static  Pair<Boolean, TranslationTreeNode> analizeAndCreateTree(Editor editor, EditorProject project, Path dir, Path dirLanguage, TranslationTree translationTree, EditorSettings settings){
        //Create the Nodes with the folder structure
        Pair<Boolean, TranslationTreeNode> createDir = createTreeByDir( editor,project,  dir.toFile(),  dirLanguage);

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

    public static Pair<Boolean, TranslationTreeNode> createTreeByDir(Editor editor, EditorProject project, File dir, Path dirLanguage) {
        final boolean[] showErrorJson = {false};

        ResourceType type = ResourceType.JSON;
        List<Resource> resourceList = null;

        Map<String, String> keys = Maps.newTreeMap();


        showFiles(dir.listFiles()).forEach(resource -> {
                    if (dirLanguage == null || resource.getPath().toString().equals(dirLanguage.toString())) {
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

                forEach(resource -> keys.putAll(resource.getTranslations()));

        return Pair.create(showErrorJson[0], new TranslationTreeNode(MessageBundle.get("tree.root.name"), Lists.newArrayList(keys.keySet()), TypeFile.FOLDER));


    }

}



