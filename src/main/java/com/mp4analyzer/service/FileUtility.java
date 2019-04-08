package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileUtility {

    public void deleteFiles(File[] files) {
        for(File file : files){
            file.delete();
        }
    }

    public File[] searchFiles(String folder, String filename, FileSearchType fileSearchType) {
        switch(fileSearchType) {
            case STARTS_WITH:
                return new File(folder).listFiles(f -> f.getName().startsWith(filename));
            case ENDS_WITH:
                return new File(folder).listFiles(f -> f.getName().endsWith(filename));
            case CONTAINS:
                return new File(folder).listFiles(f -> f.getName().contains(filename));
            default:
                return new File(folder).listFiles(f -> f.getName().contains(filename));
        }
    }

    public String getDisplayName(File file) {
        return file.getName().split("0")[0];
    }
}
