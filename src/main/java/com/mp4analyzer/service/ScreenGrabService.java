package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import com.mp4analyzer.model.ScreenGrabMeta;
import com.mp4analyzer.repository.ScreenGrabRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
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
        new File(folderLocation).mkdir();

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

            //update the db record with new screenCapture path & timestamp
            //screenGrabRepository.updateScreenGrab(new ScreenGrabMeta(screenCapture.getAbsolutePath(), frameLocation, g.getFrameRate()));

            //zip files
            addToZipFile(baseDirectory + "out\\" + filename + "_" + millis + ".zip", photoList);

            //delete original folder
            new File(folderLocation).delete();

            g.stop();

            //fileUtility.deleteFiles(existingImages); //delete the old photos

        } catch (FrameGrabber.Exception | IOException e) {
            log.warning(e.toString());
        }
    }

    private void addToZipFile(String filename, List<File> filesToZip) throws FileNotFoundException, IOException {

        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename))) {

            for (File file : filesToZip) {

                try(FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }

                    zos.closeEntry();
                } catch(IOException e){
                    log.warning(e.toString());
                }
            }
        } catch(IOException e){
            log.warning(e.toString());
        }
    }
}
