package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import com.mp4analyzer.model.ScreenCapture;
import com.mp4analyzer.model.Video;
import javafx.stage.Screen;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@Service
public class VideoService {

    private static final Logger log = Logger.getLogger(VideoService.class.getName());

    @Value("${base.dir}")
    private String baseDirectory;

    @Value("${photos.per.set}")
    private Integer photosPerSet;

    @Autowired
    private FileUtility fileUtility;

    public void testSharedFolder() {
        try {
            Path path = Paths.get("\\\\hq-trf-dfs-prd\\OP1eInvoicing\\Test\\out.txt");
            BufferedReader reader = Files.newBufferedReader(path);
            String line = reader.readLine();
            log.info("Pause");
            FileWriter writer = new FileWriter("\\\\hq-trf-dfs-prd\\OP1eInvoicing\\Test\\out2.txt");
            writer.write("TEST TEST TEST");
            writer.close();
            reader.close();
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }

    //    @Scheduled(initialDelay = 2000, fixedRate = 30000)
    public void refreshImagePreviews() {
        File[] files = fileUtility.searchFiles(baseDirectory, FileSearchType.ENDS_WITH, Arrays.asList(".mp4"));
        for (File file : files) {
            this.grab(file);
        }
    }

    public Video generateVideoWithScreenCaptures() {

        Video response = new Video();

        //find a random file
        Random random = new Random();
        File[] files = fileUtility.searchFiles(baseDirectory, FileSearchType.ENDS_WITH, Arrays.asList(".mp4"));
        File file = files[random.nextInt(files.length)];
        String filename = file.getAbsolutePath().substring(this.baseDirectory.length());

        try {

            //start the screen grabber service
            List<ScreenCapture> screenCaptureList = new ArrayList<>();
            FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsoluteFile());
            g.start();

            //after frame grabber starts, set the video props on the response object
            response.setName(filename);
            response.setPath(file.getAbsolutePath());
            response.setFrameRate(g.getFrameRate());

            //generate screenshots
            for (int i = 0; i < photosPerSet; i++) {
                int frameLocation = random.nextInt(g.getLengthInFrames()); //calculate random frame
                int seconds = (int) (frameLocation / g.getFrameRate()); //store in seconds
                g.setFrameNumber(frameLocation); //set the frame to the randomly calculated location

                //perform the actual screen "grab" at the current frame
                Frame frame = g.grab();
                Java2DFrameConverter frameConverter = new Java2DFrameConverter();
                BufferedImage bufferedImage = frameConverter.convert(frame);

                //convert image to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                baos.flush();
                byte[] imageBytes = baos.toByteArray();
                baos.close();

                //add capture to screen capture list
                screenCaptureList.add(new ScreenCapture(imageBytes, frameLocation));
                log.info(String.format("Screen grabbed at %d seconds", seconds));
            }

            //add screen capture list to response & stop the screen grab service
            screenCaptureList.sort(Comparator.comparing(ScreenCapture::getFrameLocation));
            response.setScreenCaptureList(screenCaptureList);
            g.stop();

        } catch (FrameGrabber.Exception | IOException e) {
            log.warning(e.toString());
        }
        return response;
    }

    public File zipScreenshots(Video video) {
        List<File> photoList = new ArrayList<>();

        for(int i = 0; i < video.getScreenCaptureList().size(); i++){
            ScreenCapture screenCapture = video.getScreenCaptureList().get(i);
            ByteArrayInputStream bis = new ByteArrayInputStream(screenCapture.getImageBytes());
            try {
                BufferedImage bufferedImage = ImageIO.read(bis);
                File file = new File(i + ".jpg");
                ImageIO.write(bufferedImage, "png", file);
                photoList.add(file);
            } catch(IOException e){
                log.warning(e.toString());
            }
        }
        //zip files
        File zipFile = fileUtility.addToZipFile(baseDirectory + "out\\" + System.currentTimeMillis() + ".zip", photoList);

        //delete original folder
        fileUtility.deleteFiles(photoList);

        return zipFile;
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

            for (int i = 0; i < photosPerSet; i++) {
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
