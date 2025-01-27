package com.jvms.i18neditor.util;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jvms.i18neditor.FileStructure;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.editor.EditorProject;
import com.jvms.i18neditor.editor.TranslationTreeNode;
import com.jvms.i18neditor.swing.util.Dialogs;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;


import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {


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

    public static Path findDirectoryWithSameName(String name, File root) {
        Path result = null;
        if (name.equals("")) {
            return root.toPath();
        }
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                if (file.getName().equals(name)) {
                    return file.toPath();
                }
                result = findDirectoryWithSameName(name, file);
            }
        }

        return result;


    }

    public static Resource getResourceByKey(String key, List<Resource> resources) {
        return resources.stream().filter(
                        resource -> resource.hasTranslation(key)
                ).findFirst().orElse(null)
               ;


    }

    public static Path getPathOfNode(TranslationTreeNode node, EditorProject project) {
        if (node.getTypeFile() == TypeFile.FOLDER) {
            String key = node.getKey();

            return findDirectoryWithSameName(key, project.getPath().toFile());
        }

        String stringpath;
       if(project.getResourceFileStructure() == FileStructure.Flat){
          stringpath  = Utils.getResourceByKey(node.getKey(),project.getResources()).getPath().toString();

       }else {
            stringpath = Utils.getAllPathFromKeys(project, Utils.restoreStringTrunk(node, node.getKey())).get(0);
       }
        Path path = Paths.get(stringpath).getParent();
        if ("i18n".equals(path.toFile().getName())) {
            return path.getParent();
        }
        return path;


    }




    public static List<String> getAllPathFromKeys(EditorProject project, String nameKey) {

        //Iterate over all resources and find the respective key
        return project.getResources().stream()
                .filter(c -> c.hasTranslation(nameKey))
                .map(x -> x.getPath().toAbsolutePath().toString())
                .collect(Collectors.toList());

    }




    public static String restoreStringTrunk(TranslationTreeNode node, String key) {
        TranslationTreeNode parent = (TranslationTreeNode) node.getParent();
        if (parent.getParent() == null) {
            return key.replaceFirst(node.getName() + ".", "");
        }

        if (key.contains(".")) {

            return key.replaceFirst(getNameTrunk(node) + ".", "");
        } else {
            return key;
        }
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




    public static String getNameTrunk(TreeNode node) {

        StringBuilder sb = new StringBuilder();


        if (node.getParent() != null) {
            TranslationTreeNode nodeParent = (TranslationTreeNode) node.getParent();
            if (nodeParent.getTypeFile() == TypeFile.FOLDER) {
                sb.append(((TranslationTreeNode) node.getParent()).getKey());

            } else {
                sb.append(getNameTrunk(node.getParent()));
            }
        }


        return sb.toString();
    }


    public static boolean isLocaleAvailable(File file) {

        return isLocaleAvailable(getBaseName(file));
    }

    public static String getBaseName(File file) {
        return FilenameUtils.getBaseName(file.getName());

    }

    public static boolean isLocaleAvailable(String localeString) {
        if ("".equals(localeString)) {
            return false;
        }
        try {
            Locale locale = LocaleUtils.toLocale(localeString);

            return Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        } catch (Exception e) {

            return false;
        }
    }

    public static void showError(String message) {
        Dialogs.showErrorDialog(null, MessageBundle.get("dialogs.error.title"), message);
    }

    public static String transformKeyByPathinResources(String key, Path path) {
        String nameOfFOlder = path.getParent().getParent().toFile().getName();
        return key.replaceAll(".*" + nameOfFOlder + "\\.", "");
    }
}
