package com.jvms.i18neditor.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class provides utility functions for retrieving translations from a resource bundle.<br>
 * By default it loads translations from {@value #RESOURCES_PATH}.
 *
 * <p>The locale used is the current value of the default locale for this instance of the Java Virtual Machine.</p>
 *
 * @author Jacob van Mourik
 */
public final class MessageBundle {
    private static final String RESOURCES_PATH = "bundles/messages";
    private static ResourceBundle resources;

    /**
     * Sets the preferred locale to use.
     * When calling this function resources will be reloaded from disk.
     *
     * @param locale the preferred locale to use
     */
    public static void setLocale(Locale locale) {
        resources = ResourceBundle.getBundle(RESOURCES_PATH, locale);
    }

    /**
     * Gets a value from this bundle for the given {@code key}. Any second arguments will
     * be used to format the value.
     *
     * @param key  the bundle key
     * @param args objects used to format the value.
     * @return the formatted value for the given key.
     */
    public static String get(String key, Object... args) {
        String value = resources.getString(key);
        return MessageFormat.format(value, args);
    }

    /**
     * Gets a mnemonic value from this bundle.
     *
     * @param key the bundle key.
     * @return the mnemonic value for the given key.
     */
    public static Character getMnemonic(String key) {
        String value = resources.getString(key);
        return value.charAt(0);
    }

    /**
     * Loads the resources.
     */
    public static void loadResources() {
        resources = ResourceBundle.getBundle(RESOURCES_PATH);
    }
}