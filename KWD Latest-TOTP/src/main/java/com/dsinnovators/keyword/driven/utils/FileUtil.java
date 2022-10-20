package com.dsinnovators.keyword.driven.utils;

import java.io.File;

public class FileUtil {
    public static boolean isFileExecutable(File file) throws Exception{
        if (!file.exists()) {
            throw new Exception("given File path does not exist.");
        }

        if (!file.isFile()){
            throw new Exception("given File path is not file.");
        }

        if (!file.canRead()){
            throw new Exception("given File path is not readable.");
        }

        return file.canExecute();
    }

    public static boolean makeFileExecutable(File file) throws Exception{
        if(!isFileExecutable(file)) {
            return file.setExecutable(true);
        } else {
            return true;
        }
    }
}
