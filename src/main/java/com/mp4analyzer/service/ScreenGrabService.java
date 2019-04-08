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
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class ScreenGrabService {

    private static final Logger log = Logger.getLogger(ScreenGrabService.class.getName());

    @Value("${base.dir}")
    private String baseDirectory;

    @Autowired
    private ScreenGrabRepository screenGrabRepository;

    @Autowired
    private FileUtility fileUtility;

    @Scheduled(initialDelay = 2000, fixedRate = 30000)
    public void refreshImagePreviews() {
        File[] files = fileUtility.searchFiles(baseDirectory, ".mp4", FileSearchType.ENDS_WITH);
        for (File file : files) {
            this.grab(file);
        }
    }

    public void grab(File file) {
        FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsoluteFile());
        Random random = new Random();
        try {

            String filename = fileUtility.getDisplayName(file);
            File[] existingImages = fileUtility.searchFiles(baseDirectory + "out", filename, FileSearchType.STARTS_WITH);

            g.start();
            int frameLocation = random.nextInt(g.getLengthInFrames()); //calculate random frame
            double seconds = frameLocation / g.getFrameRate(); //store in seconds
            g.setFrameNumber(frameLocation); //set the frame to the randomly calculated location

            //perform the actual "grab" and image save
            Frame frame = g.grab();
            Java2DFrameConverter frameConverter = new Java2DFrameConverter();
            BufferedImage bufferedImage = frameConverter.convert(frame);

            File screenCapture = new File(baseDirectory + "out\\" + filename + "_" + System.currentTimeMillis() + ".png");
            ImageIO.write(bufferedImage, "png", screenCapture);

            //update the db record with new screenCapture path & timestamp
            screenGrabRepository.updateScreenGrab(new ScreenGrabMeta(screenCapture.getAbsolutePath(), frameLocation, g.getFrameRate()));

            g.stop();

            fileUtility.deleteFiles(existingImages); //delete the old photos

            log.info(String.format("Screen grabbed at %f seconds for %s", seconds, filename));

        } catch (FrameGrabber.Exception | IOException e) {
            log.warning(e.toString());
        }
    }
}
