package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class FileUtility {

    public void deleteFiles(List<File> files) {
        for(File file : files){
            file.delete();
        }
    }

    public File[] searchFiles(String folder, FileSearchType fileSearchType, List<String> searchText) {
        switch(fileSearchType) {
            case STARTS_WITH:
                return new File(folder).listFiles(f -> searchText.stream().filter(s -> f.getName().startsWith(s)).count() > 0);
            case ENDS_WITH:
                return new File(folder).listFiles(f -> searchText.stream().filter(s -> f.getName().endsWith(s)).count() > 0);
            case CONTAINS:
                return new File(folder).listFiles(f -> searchText.stream().filter(s -> f.getName().contains(s)).count() > 0);
            default:
                return new File(folder).listFiles(f -> searchText.stream().filter(s -> f.getName().contains(s)).count() > 0);
        }
    }

    public String getDisplayName(File file) {
        return file.getName().split("0")[0];
    }
}
