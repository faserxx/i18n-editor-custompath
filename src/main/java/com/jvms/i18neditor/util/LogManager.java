/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jvms.i18neditor.util;

import com.jvms.i18neditor.editor.Editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Best Vision Cuba
 */
public class LogManager {

    public static void logMessage(LogParameters params) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime time = LocalDateTime.now();
            String typeOperation = operation(params.getTypeOperation());
            StringBuilder sbLine = new StringBuilder();
            sbLine.append(time.format(formatter)).append(" ---> ").append(params.getPathTranslationFile()).append(" ---> ").append(typeOperation).append(" ---> ").append(params.getMessage());
            //String fileLog = params.getPathLogFile().isEmpty() ? System.getProperty("user.dir") + nameFileLog : params.getPathLogFile() + nameFileLog;
            
            Path path =   Paths.get(Editor.SETTINGS_DIR, "log.txt");
            if ( !path.toFile().exists())
                path.toFile().createNewFile();
            
            try ( BufferedWriter br = Files.newBufferedWriter(path, Charset.defaultCharset(), StandardOpenOption.APPEND)) {
                br.write(sbLine.toString());
                br.newLine();
            }
        }   catch (IOException ex) {
            Logger.getLogger(LogManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String operation(char typeOperation) {
        String result;
        switch (Character.toUpperCase(typeOperation)) {
            case 'A':
                result = "MODIFY_ADD";
                break;
            case 'C':
                result = "CREATE";
                break;
            case 'D':
                result = "MODIFY_DELETE";
                break;
            case 'R':
                result = "DELETE";
                break;                
            default:
                result = new StringBuilder( "UNKNOWN OPERATION ").append(Character.toString(typeOperation)).toString();
                break;
        }
        return result;
    }

}
