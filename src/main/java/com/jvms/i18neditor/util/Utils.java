package com.jvms.i18neditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.Editor;
import com.jvms.i18neditor.editor.EditorProject;
import com.jvms.i18neditor.editor.TranslationTree;
import com.jvms.i18neditor.editor.TranslationTreeNode;
import com.jvms.i18neditor.swing.util.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static void compareJsonString(Optional<String> old, String newJson, Path path) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> leftMap = gson.fromJson(old.orElse(""), type);
        Map<String, Object> rightMap = gson.fromJson(newJson, type);
        Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap);
        Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap);

        MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);


        difference.entriesDiffering()
                .forEach((key, value) -> {
                    LogParameters params = new LogParameters("", path.toString(), 'a', key + "- " + value);
                    LogManager.logMessage(params);


                });


    }

    public static Path retunPathByTypeofNode(TranslationTreeNode node, EditorProject project, TranslationTree translationTree) {

        //if is null then is root
        if (node == null) {
            return project.getPath();
        }

     /*   if (node.typeFile == TypeFile.FOLDER) {
            return Paths.get(Utils.construcPathByNodes(node.getKey(), project.getPath().toString(), translationTree));
        }*/

        Path path = Paths.get(Utils.getAllPathFromKeys(project, Utils.restoreStringTrunk(node, node.getKey())).get(0));
        return path.getParent();


    }

    public static List<String> getAllPathFromKeys(EditorProject project, String nameKey) {

        //Iterate over all resources and find the respective key
        return project.getResources().stream()
                .filter(c -> c.hasTranslation(nameKey))
                .map(x -> x.getPath().toAbsolutePath().toString())
                .collect(Collectors.toList());

    }

    public static List<String> getAllPathFromKeysAlls(EditorProject project, String nameKey) {

        //Iterate over all resources and find the respective key
        return project.getResources().stream()
                .map(x -> x.getPath().toAbsolutePath().toString())
                .collect(Collectors.toList());

    }


    public static String restoreStringTrunk(TranslationTreeNode node, String key) {
        return key.replaceFirst(getNameTrunk(node) + ".", "");

    }

    public static void writeLogsByNameKey(EditorProject project, String nameKey, char operation, String message) {
        List<String> paths = getAllPathFromKeys(project, nameKey);
        //Once remove the key show the log
        paths.forEach(x -> {
            LogParameters params = new LogParameters("", x, operation, message);
            LogManager.logMessage(params);
        });

    }


    public static void logsPathWithMessage(List<String> paths, char operation, String message) {
        //Once remove the key show the log
        paths.forEach(x -> {
            LogParameters params = new LogParameters("", x, operation, message);
            LogManager.logMessage(params);
        });
    }

    public static void createPathWithLocale(Path path, String trim) {

        File file = path.toFile();
        if (!file.getName().equals("i18n"))
            file = file.toPath().resolve("i18n").toFile();
        file.mkdirs();

        //If is json

        try {
            file.toPath().resolve(trim + ".json").toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String construcPathByNodes(String name, String path, TranslationTree translationTree) {

        //Obtain the last element that is a leaf
        String parentKey = name.replaceAll("." + Files.getFileExtension(name), "");
        //replace to key to obtain the father
        parentKey = getNameTrunk(translationTree.getNodeByKey(parentKey));
        //Convert String to path
        final Path[] objec = {Paths.get(path)};
        //Replace every . in the key to create the path
        Arrays.stream(parentKey.split("\\.")).forEach(x -> objec[0] = objec[0].resolve(x));
        return objec[0].resolve("i18n").toString();


    }


    public static String getNameTrunk(TreeNode node) {

        StringBuilder sb = new StringBuilder();


        if (node.getParent() != null) {
            TranslationTreeNode nodeParent = (TranslationTreeNode) node.getParent();
            if (nodeParent.typeFile == TypeFile.FOLDER) {
                sb.append(((TranslationTreeNode) node.getParent()).getKey());

            } else {
                sb.append(getNameTrunk(node.getParent()));
            }
        }


        return sb.toString();
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
                            showError(MessageBundle.get("resources.import.error.single", resource.getPath()));
                        }
                    }
                }

        );

        project.getResources().
                stream().filter(x -> x.getPath().toAbsolutePath().toString().contains(dir.getAbsolutePath().toString())).
                forEach(resource -> keys.putAll(resource.getTranslations()));

        return Pair.create(showErrorJson[0], new TranslationTreeNode(dir.getParentFile().getName(), Lists.newArrayList(keys.keySet()), TypeFile.FOLDER));


    }


    public static void showError(String message) {
        Dialogs.showErrorDialog(null, MessageBundle.get("dialogs.error.title"), message);
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

                //If is not a directory must be a json file or a metadata or the folder not have a valid structure
                if (!child.isDirectory() && !child.getName().equals(".i18n-editor-metadata") && !Files.getFileExtension(child.getName()).equals("json")) {

                    showError(MessageBundle.get("dialogs.notvalid.folderstructure", child.getParentFile().getAbsolutePath()));
                    return null;
                }

            }

        }


        return Pair.create(showErrorJsonMalformed, ret);
    }


    public static String transformKeyByPathinResources(String key, Path path) {
        String nameOfFOlder = path.getParent().getParent().toFile().getName();
                 return key.replaceAll(".*"+ nameOfFOlder+"\\.","");
    }
}
