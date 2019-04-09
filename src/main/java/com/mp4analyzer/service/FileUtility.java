package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileUtility {

    private static final Logger log = Logger.getLogger(FileUtility.class.getName());

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
        return file.getName().split("\\.")[0];
    }

    public void addToZipFile(String zipAbsFilePath, List<File> filesToZip) {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipAbsFilePath))) {
            for (File fileToZip : filesToZip) {
                zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                Files.copy(fileToZip.toPath(), zipOut);
            }
        } catch(IOException e){
            log.warning(e.toString());
        }
    }
}
