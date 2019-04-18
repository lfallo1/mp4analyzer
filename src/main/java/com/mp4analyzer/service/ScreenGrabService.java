package com.mp4analyzer.service;

import com.mp4analyzer.model.FileSearchType;
import com.mp4analyzer.repository.ScreenGrabRepository;
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
import java.util.*;
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
        new File(folderLocation).mkdir();

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

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename))) {

            for (File file : filesToZip) {

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }

                    zos.closeEntry();
                } catch (IOException e) {
                    log.warning(e.toString());
                }
            }
        } catch (IOException e) {
            log.warning(e.toString());
        }
    }

    public void concatVideos() {

        List<String> tempFiles = new ArrayList<>();
        String videoFileListName = "videofileList.txt";
        File videoFileList = new File(videoFileListName);
        try(FileWriter writer = new FileWriter(videoFileList)) {
            for (int i = 0; i < 3; i++) {
                String filename = "output_crop_trim" + i;
                String timecode = i == 0 ? "00:00:05" : i == 1 ? "00:00:25" : "00:00:45";
                String cmd = "ffmpeg -ss " + timecode + " -i vids//SampleVideo_1280x720_20mb.mp4 -to 00:00:20 -vf crop=640:480:34:90,setpts=PTS/3 -an " + filename + ".mp4";

                Process process = Runtime.getRuntime().exec(cmd);
                process.waitFor();



                if (process.exitValue() != 0) {
                    String result = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                            .lines().collect(Collectors.joining("\n"));
                    log.warning(result);
                } else{
                    writer.write("file '" + filename + "'");
                    tempFiles.add(filename);
                }
            }
        } catch(IOException | InterruptedException e){
            log.warning(e.toString());
        }

        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -f concat -safe 0 -i " + videoFileListName + " -c copy " + UUID.randomUUID().toString().replaceAll("-","") + ".mp4");
            process.waitFor();
            if (process.exitValue() != 0) {
                String result = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                log.warning(result);
            }

            for(String file : tempFiles){
                new File(file).delete();
            }
        } catch(InterruptedException | IOException e){
            log.warning(e.toString());
        }
    }

    private static boolean runScript(String path, String cmd) throws IOException, InterruptedException {
        List<String> commands = new ArrayList<String>();
        commands.add(cmd);
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(path));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        flushInputStreamReader(process);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String result = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            log.warning(result);
        }

        return exitCode == 0;
    }

    private static void flushInputStreamReader(Process process) throws IOException, InterruptedException {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        StringBuilder s = new StringBuilder();
        while ((line = input.readLine()) != null) {
            s.append(line);
        }
    }

}
