/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jvms.i18neditor.util;

/**
 * Class for collect all parameter needed for insert a message, into the log
 * file.
 */
public class LogParameters {

    /**
     * Place where the log file will be or is, if empty the log file will be
     * created in the root of the project.
     */
    private String pathLogFile;
    /**
     * Path of the translation file, this path will be written like text within
     * log file, like a message.
     */
    private String pathTranslationFile;
    /**
     * Type of operation about witch the information will be message CREATE(C),
     * MODIFY_ADD (A), MODIFY_DELETE (D) and DELETE (R)
     */
    private char typeOperation;
    /**
     * Message according of "typeOperation"
     */
    private String message;

    public LogParameters() {
    }

    /**
     *
     * @param pathLogFile Place where the log file will be or is, if empty the
     * log file will be created in the root of the project, ending without "\ or /" an not file name, correct example: "C:\FFOutput".
     * @param pathTranslationFile Path of the translation file, this path will
     * be written like text within log file, like a message.
     * @param typeOperation 
     * <ul>
     * <li>C -> CREATE, A translation file is created</li>
     * <li>A -> MODIFY_ADD, The translation file in question is modified by
     * adding something</li>
     * <li>D -> MODIFY_DELETE, The translation file in question is modified by
     * removing something</li>
     * <li>R -> DELETE, The translation file was removed</li>
     * </ul>
     * @param message Message according of "typeOperation" parameter.
     */
    public LogParameters(String pathLogFile, String pathTranslationFile, char typeOperation, String message) {
        this.pathLogFile = pathLogFile;
        this.pathTranslationFile = pathTranslationFile;
        this.typeOperation = typeOperation;
        this.message = message;
    }

    public String getPathLogFile() {
        return pathLogFile;
    }

    public void setPathLogFile(String pathLogFile) {
        this.pathLogFile = pathLogFile;
    }

    public String getPathTranslationFile() {
        return pathTranslationFile;
    }

    public void setPathTranslationFile(String pathTranslationFile) {
        this.pathTranslationFile = pathTranslationFile;
    }

    public char getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(char typeOperation) {
        this.typeOperation = typeOperation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
