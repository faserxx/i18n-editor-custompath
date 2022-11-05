package com.jvms.i18neditor.util;

import java.io.File;
import java.util.Optional;

public class Utils {
    public static Optional<String> getExtension(File file) {
        String filename = file.getName();
        return Optional.of(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
