package com.jvms.i18neditor.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.jvms.i18neditor.FileStructure;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.io.ChecksumException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class provides utility functions for a {@link Resource}.
 *
 * @author Jacob van Mourik
 */
public final class Resources {
    private final static Charset UTF8_ENCODING;
    private final static String FILENAME_LOCALE_REGEX;

    static {
        UTF8_ENCODING = Charset.forName("UTF-8");
        FILENAME_LOCALE_REGEX = Pattern.quote("{") + "(.*)" + Pattern.quote("LOCALE") + "(.*)" + Pattern.quote("}");
    }

    /**
     * Gets all resources from the given <code>rootDir</code> directory path.
     *
     * <p>The <code>fileDefinition</code> is the filename definition of resource files to look for.
     * The definition consists of a filename including optional locale part (see <code>useLocaleDirs</code>).
     * The locale part should be in the format: <code>{LOCALE}</code>, where <code>{</code> and <code>}</code> tags
     * defines the start and end of the locale part and <code>LOCALE</code> the location of the locale itself.</p>
     *
     * <p>When a resource type is given, only resources of that type will returned.</p>
     *
     * <p>This function will not load the contents of the file, only its description.<br>
     * If you want to load the contents, use {@link #load(Resource)} afterwards.</p>
     *
     * @param root           the root directory of the resources
     * @param fileDefinition the resource's file definition for lookup (using locale interpolation)
     * @param type           the type of the resource files to look for
     * @param structure      the file structure used for the lookup
     * @return list of found resources
     * @throws IOException if an I/O error occurs reading the directory.
     */
    public static List<Resource> get(Path root, String fileDefinition, FileStructure structure, Optional<ResourceType> type)
            throws IOException {
        List<Resource> result = Lists.newLinkedList();
        List<Path> files = Files.walk(root, 1).collect(Collectors.toList());
        String defaultFileName = getFilename(fileDefinition, Optional.empty());
        Pattern fileDefinitionPattern = Pattern.compile("^" + getFilenameRegex(fileDefinition) + "$");

        for (Path file : files) {
            Path parent = file.getParent();
            if (parent == null || Files.isSameFile(root, file) || !Files.isSameFile(root, parent)) {
                continue;
            }
            String filename = com.google.common.io.Files.getNameWithoutExtension(file.getFileName().toString());
            for (ResourceType rt : ResourceType.values()) {
                if (!type.orElse(rt).equals(rt)) {
                    continue;
                }
                if (structure == FileStructure.Nested && Files.isDirectory(file)) {
                    Locale locale = Locales.parseLocale(filename);
                    if (locale == null) {
                        continue;
                    }
                    Path rf = Paths.get(root.toString(), locale.toString(), getFilename(fileDefinition, Optional.of(locale)) + rt.getExtension());
                    if (Files.isRegularFile(rf)) {
                        result.add(new Resource(rt, rf, locale));
                    }
                }
                if (structure == FileStructure.Flat && Files.isRegularFile(file)) {
                    Matcher matcher = fileDefinitionPattern.matcher(filename);
                    if (!matcher.matches() && !filename.equals(defaultFileName)) {
                        continue;
                    }
                    if (!matchesResourceType(file, rt)) {
                        continue;
                    }
                    Locale locale = null;
                    if (matcher.matches() && matcher.groupCount() > 0) {
                        locale = Locales.parseLocale(matcher.group(1));
                    }
                    result.add(new Resource(rt, file, locale));
                }
            }
        }
        ;

        return result;
    }

    /**
     * Loads the translations of a {@link Resource} from disk.
     *
     * <p>This function will store a checksum to the resource.</p>
     *
     * @param resource the resource.
     * @throws IOException if an I/O error occurs reading the file.
     */
    public static boolean load(Resource resource) throws IOException {
        boolean showErrorJson = false;
        try {


            ResourceType type = resource.getType();
            Path path = resource.getPath();
            SortedMap<String, String> translations;
            if (type == ResourceType.Properties) {
                ExtendedProperties content = new ExtendedProperties();
                content.load(path);
                translations = fromProperties(content);
            } else {
                String content = Files.lines(path, UTF8_ENCODING).collect(Collectors.joining());
                if (type == ResourceType.ES6) {
                    content = es6ToJson(content);
                }

                translations = fromJson(content);
            }
            resource.setTranslations(translations);
            resource.setChecksum(createChecksum(resource));
        } catch (Exception ex) {
            showErrorJson = true;

        }

        return showErrorJson;
    }

    /**
     * Writes the translations of the given {@link Resource} to disk.
     * Empty translation values will be skipped.
     *
     * <p>This function will perform a checksum check before saving
     * to see if the file on disk has been changed in the meantime.</p>
     *
     * <p>This function will store a checksum to the resource.</p>
     *
     * @param resource       the resource to write.
     * @param prettyPrinting whether to pretty print the contents
     * @param plainKeys
     * @throws IOException if an I/O error occurs writing the file.
     */
    public static void write(Resource resource, boolean prettyPrinting, boolean flattenKeys) throws IOException {

        if (resource.getChecksum() != null) {
            String checksum = createChecksum(resource);
            if (!checksum.equals(resource.getChecksum())) {
                throw new ChecksumException("File on disk has been changed.");
            }
        }

        ResourceType type = resource.getType();
        if (type == ResourceType.Properties) {
            ExtendedProperties content = toProperties(resource.getTranslations());

            content.store(resource.getPath());
        } else {
            Utils.compareJsonString(FileUtils.readFileToString(resource.getPath().toFile(), StandardCharsets.UTF_8), toJson(resource, prettyPrinting, flattenKeys), resource.getPath());
            String content = toJson(resource, prettyPrinting, flattenKeys);

            if (type == ResourceType.ES6) {
                content = jsonToEs6(content);
            }
            if (!Files.exists(resource.getPath())) {
                Files.createDirectories(resource.getPath().getParent());
                Files.createFile(resource.getPath());
            }
            Files.write(resource.getPath(), Lists.newArrayList(content), UTF8_ENCODING);
        }
        resource.setChecksum(createChecksum(resource));
    }

    /**
     * Creates a new {@link Resource} with the given {@link ResourceType} in the given directory path.
     * This function should be used to create new resources. For creating an instance of an
     * existing resource on disk, see {@link #read(Path)}.
     *
     * <p>This function will store a checksum to the resource.</p>
     *
     * @param type               the type of the resource to create.
     * @param root               the root directory to write the resource to.
     * @param filenameDefinition the filename definition of the resource.
     * @param structure          the file structure to use
     * @param locale             the locale of the resource (optional).
     * @return The newly created resource.
     * @throws IOException if an I/O error occurs writing the file.
     */
    public static Resource create(ResourceType type, Path root, String fileDefinition, FileStructure structure, Optional<Locale> locale)
            throws IOException {
        String extension = type.getExtension();
        Path path;
        if (structure == FileStructure.Nested) {
            path = Paths.get(root.toString(), locale.get().toString(), getFilename(fileDefinition, locale) + extension);
        } else {
            path = Paths.get(root.toString(), getFilename(fileDefinition, locale) + extension);
        }
        Resource resource = new Resource(type, path, locale.orElse(null));
        write(resource, false, false);
        return resource;
    }

    private static String getFilenameRegex(String fileDefinition) {
        return fileDefinition.replaceAll(FILENAME_LOCALE_REGEX, "$1(" + Locales.LOCALE_REGEX + ")$2");
    }

    private static String getFilename(String fileDefinition, Optional<Locale> locale) {
        return fileDefinition.replaceAll(FILENAME_LOCALE_REGEX, locale.isPresent() ? ("$1" + locale.get().toString() + "$2") : "");
    }

    private static SortedMap<String, String> fromProperties(Properties properties) {
        SortedMap<String, String> result = Maps.newTreeMap();
        properties.forEach((key, value) -> {
            result.put((String) key, StringEscapeUtils.unescapeJava((String) value));
        });
        return result;
    }

    private static ExtendedProperties toProperties(Map<String, String> translations) {
        ExtendedProperties result = new ExtendedProperties();
        translations.forEach((key, value) -> {
            if (!Strings.isNullOrEmpty(value)) {
                result.put(key, value);
            }
        });
        return result;
    }

    private static SortedMap<String, String> fromJson(String json) throws JsonSyntaxException, IllegalArgumentException {
        SortedMap<String, String> result = Maps.newTreeMap();
        JsonElement elem = new Gson().fromJson(json, JsonElement.class);

        fromJson(null, elem, result);


        return result;
    }

    private static void fromJson(String key, JsonElement elem, Map<String, String> content) {
        if (elem.isJsonObject()) {
            elem.getAsJsonObject().entrySet().forEach(entry -> {
                String newKey = key == null ? entry.getKey() : ResourceKeys.create(key, entry.getKey());
                fromJson(newKey, entry.getValue(), content);
            });
        } else if (elem.isJsonPrimitive()) {
            content.put(key, StringEscapeUtils.unescapeJava(elem.getAsString()));
        } else if (elem.isJsonNull()) {
            content.put(key, "");
        } else {
            throw new IllegalArgumentException("Found invalid json element.");
        }
    }

    private static String toJson(Resource resource, boolean prettify, boolean flattenKeys) {

        String path = resource.getPath().getParent().getParent().toFile().getName();
        SortedMap<String, String> translations = new TreeMap<>(resource.getTranslations());
        List<String> as = new ArrayList<>();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (entry.getKey().startsWith(path)) {
                as.add(entry.getKey());

            }

        }
        for (String a : as) {
            translations.put(a.replaceFirst(path + ".", ""), translations.get(a));
            translations.remove(a);
        }
        resource.setTranslations(translations);
        List<String> keys = Lists.newArrayList(translations.keySet());
        JsonElement elem = !flattenKeys ? toJson(translations, null, keys) : toFlatJson(translations, keys);
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        if (prettify) {
            builder.setPrettyPrinting();
        }
        return builder.create().toJson(elem);
    }

    private static JsonElement toFlatJson(Map<String, String> translations, List<String> keys) {
        JsonObject object = new JsonObject();
        if (keys.size() > 0) {
            translations.forEach((k, v) -> {
                if (!Strings.isNullOrEmpty(translations.get(k))) {
                    object.add(k, new JsonPrimitive(translations.get(k)));
                }
            });
        }
        return object;
    }

    private static JsonElement toJson(Map<String, String> translations, String key, List<String> keys) {
        if (keys.size() > 0) {
            JsonObject object = new JsonObject();
            ResourceKeys.uniqueRootKeys(keys).forEach(rootKey -> {
                String subKey = ResourceKeys.create(key, rootKey);
                List<String> subKeys = ResourceKeys.extractChildKeys(keys, rootKey);
                object.add(rootKey, toJson(translations, subKey, subKeys));
            });
            return object;
        }
        if (key == null) {
            return new JsonObject();
        }
        if (translations.get(key) == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(translations.get(key));
    }

    private static String es6ToJson(String content) {
        return content.replaceAll("export +default", "").replaceAll("} *;", "}");
    }

    private static String jsonToEs6(String content) {
        return "export default " + content + ";";
    }

    private static String createChecksum(Resource resource) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        try (InputStream is = Files.newInputStream(resource.getPath())) {
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        String result = "";
        byte[] bytes = digest.digest();
        for (int i = 0; i < bytes.length; i++) {
            result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private static boolean matchesResourceType(Path path, ResourceType type) {
        String fileExt = com.google.common.io.Files.getFileExtension(path.getFileName().toString());
        return ("." + fileExt).equals(type.getExtension());
    }
}
