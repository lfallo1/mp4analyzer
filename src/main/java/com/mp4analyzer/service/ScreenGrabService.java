package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import com.mp4analyzer.model.ScreenGrabMeta;
import com.mp4analyzer.repository.ScreenGrabRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ScreenGrabService {

    private static final Logger log = Logger.getLogger(ScreenGrabService.class.getName());

    @Value("${base.dir}")
    private String baseDirectory;

    @Value("${photos.per.set}")
    private Integer photosPerSet;

    @Autowired
    private ScreenGrabRepository screenGrabRepository;

    @Autowired
    private FileUtility fileUtility;

//    @Scheduled(initialDelay = 2000, fixedRate = 30000)
    public void refreshImagePreviews() {
        File[] files = fileUtility.searchFiles(baseDirectory, FileSearchType.ENDS_WITH, Arrays.asList(".mp4"));
        for (File file : files) {
            this.grab(file);
        }
    }

    public void grab(File file) {
        FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsoluteFile());
        String filename = fileUtility.getDisplayName(file);
        Random random = new Random();
        long millis = System.currentTimeMillis();
        String folderLocation = baseDirectory + "out\\" + filename + "_" + millis + "\\";
        File folder = new File(folderLocation);
        folder.mkdir();

        List<File> photoList = new ArrayList<>();
        try {
            g.start();

            //File[] existingImages = fileUtility.searchFiles(baseDirectory + "out", filename, FileSearchType.STARTS_WITH);

            for(int i = 0; i < photosPerSet; i++) {
                int frameLocation = random.nextInt(g.getLengthInFrames()); //calculate random frame
                int seconds = (int) (frameLocation / g.getFrameRate()); //store in seconds
                g.setFrameNumber(frameLocation); //set the frame to the randomly calculated location

                //perform the actual "grab" and image save
                Frame frame = g.grab();
                Java2DFrameConverter frameConverter = new Java2DFrameConverter();
                BufferedImage bufferedImage = frameConverter.convert(frame);

                File screenCapture = new File(folderLocation + filename + "_" + seconds + "sec_" + System.currentTimeMillis() + ".png");
                ImageIO.write(bufferedImage, "png", screenCapture);

                photoList.add(screenCapture);
                log.info(String.format("Screen grabbed at %d seconds for %s", seconds, filename));
            }

            //zip files
            fileUtility.addToZipFile(baseDirectory + "out\\" + filename + "_" + millis + ".zip", photoList);

            //delete original folder
            FileUtils.deleteDirectory(folder);

            g.stop();

        } catch (FrameGrabber.Exception | IOException e) {
            log.warning(e.toString());
        }
    }
}
