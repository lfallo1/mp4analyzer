package com.mp4analyzer.controller;

import com.mp4analyzer.model.Video;
import com.mp4analyzer.service.VideoService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

@RestController
@RequestMapping("api/video")
public class VideoController {

    @Value("${base.dir}")
    private String baseDirectory;

    @Autowired
    private VideoService videoService;

    @GetMapping("/gallery")
    public ResponseEntity<Video> getVideoGallery(){
        return new ResponseEntity<>(this.videoService.generateVideoWithScreenCaptures(), HttpStatus.OK);
    }

    @GetMapping("/play/{fileName:.+}")
    public ResponseEntity<UrlResource> playVideo(@PathVariable("fileName") String fileName) throws MalformedURLException {
        UrlResource video = new UrlResource("file:" + baseDirectory + fileName.replaceAll("\\|", "\\"));
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(video);
    }

    @PostMapping("/zip")
    public void zipScreenShots(@RequestBody Video video, HttpServletResponse response) throws IOException {

        File zipFile = videoService.zipScreenshots(video);
        response.setContentType("application/zip");
        response.setHeader("content-disposition", "attachment; filename=" + zipFile.getName());

        OutputStream out = null;
        FileInputStream in = null;

        try {
            in = new FileInputStream(zipFile);
            out = response.getOutputStream();

            // copy from in to out
            IOUtils.copy(in, out);
        } finally{
            out.close();
            in.close();
            zipFile.delete();
        }
    }

}
