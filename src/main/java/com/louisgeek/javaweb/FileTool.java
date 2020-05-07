package com.louisgeek.javaweb;

import java.io.*;

public class FileTool {
    public static void copyFile(File srcFile, File destFile) {
        if (!srcFile.exists()) {
            return;
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        //
        InputStream is;
        OutputStream os;
        try {
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            //循环来读取该文件中的数据
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();// 刷新缓冲区
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String srcFilePath, String destFilePath) {
        copyFile(new File(srcFilePath), new File(destFilePath));
    }

    public static void moveFile(File srcFile, String destFilePath) {
        File filePath = new File(destFilePath);
        File destFile = new File(filePath, srcFile.getName());
        srcFile.renameTo(destFile);
    }

    public static void deleteAllFiles(File file) {
        if (!file.exists())
            return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAllFiles(files[i]);
        }
        file.delete();
    }
}
