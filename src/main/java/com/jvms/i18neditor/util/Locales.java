package com.jvms.i18neditor.util;

import com.google.common.base.Strings;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides utility functions for locales.
 *
 * @author Jacob van Mourik
 */
public final class Locales {
    public static final String LOCALE_REGEX = "([^_-]*)(?:[_-]([^_-]*)(?:[_-]([^_-]*))?)?";
    public static final Pattern LOCALE_PATTERN = Pattern.compile(LOCALE_REGEX);

    public static Locale parseLocale(String localeString) {
        if (Strings.isNullOrEmpty(localeString)) {
            return null;
        }
        Matcher matcher = LOCALE_PATTERN.matcher(localeString);
        if (matcher.matches()) {
            String language = matcher.group(1);
            language = (language == null) ? "" : language;
            String country = matcher.group(2);
            country = (country == null) ? "" : country;
            String variant = matcher.group(3);
            variant = (variant == null) ? "" : variant;
            return new Locale(language, country, variant);
        }
        return null;
    }
}
