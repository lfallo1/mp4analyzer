package com.mp4analyzer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PdfService {

    class Player {
        String name;
        String school;
    }

    class Workout {
        Player player;
        String source;
        double height;
        double weight;
        double arm;
        double hand;
        double wing;
        double forty;
        double twenty;
        double ten;
        double vertical;
        double broadJump;
        double shortShuttle;
        double threeCone;
        int benchPress;
    }

    private static final Logger log = Logger.getLogger(PdfService.class.getName());

    public void parsePdf() throws Exception {

        PDDocument document = null;
        try {

            document = PDDocument.load(new File("docs//Dane-Bruglers-2019-NFL-Draft-Guide-Athletic.pdf"), "draftguide2019");
            StandardDecryptionMaterial dm = new   StandardDecryptionMaterial("draftguide2019");

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            String pdfFileInText = tStripper.getText(document);
            //System.out.println("Text:" + st);

            // split by whitespace
            String lines[] = pdfFileInText.split("\\r?\\n");
            Player currentPlayer = new Player();
            List<Workout> workouts = new ArrayList<>();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if(line.startsWith("BACKGROUND") || (line.startsWith("YEAR (GP/GS)") && currentPlayer.name == null)){
                    boolean found = false;
                    int rowOffset = 2;
                    while(!found) {
                        try {
                            String[] partsName = lines[i - rowOffset].split("\\|")[0].split(" ");
                            currentPlayer.name = combineArray(partsName, 1, partsName.length);
                            String[] partsSchool = lines[i - rowOffset].split("\\|")[1].split(" ");
                            currentPlayer.school = combineArray(partsSchool, 0, partsSchool.length - 1);
                            found = true;
                        } catch (IndexOutOfBoundsException e) {
                            log.warning(e.toString());
                            rowOffset++;
                        }
                    }
                }
                else if(line.startsWith("COMBINE")){
                    List<String> parts = parseWorkoutLine(line);
                    if(parts.size() > 13) {
                        workouts.add(parseWorkout(currentPlayer, parts, "COMBINE"));
                    }

                    if(lines[i+1].startsWith("PRO DAY") && !lines[i+1].startsWith("PRO DAY N/A")){
                        parts = parseWorkoutLine(lines[i+1]);
                        if(parts.size() > 13) {
                            workouts.add(parseWorkout(currentPlayer, parts, "PRO_DAY"));
                        }
                    }
                    currentPlayer = new Player();
                }
            }

            log.info(String.valueOf(workouts.size()));

        } catch(IOException e){
            log.warning(e.toString());
        } finally{
            if(null != document){
                document.close();
            }
        }
    }

    private List<String> parseWorkoutLine(String line) {
        List<String> parts = new ArrayList<>(Arrays.asList((line.split(" "))));
        for(int j = parts.size() - 1; j >= 0; j--){
            String part = parts.get(j);
            if(part.indexOf("/") > -1){
                parts.set(j-1, parts.get(j-1) + " " + part);
                parts.remove(j);
                j--;
            }
        }

        return parts;
    }

    private String combineArray(String[] parts, int startIndex, int endIndex) {
        return IntStream.range(startIndex, endIndex).mapToObj(i -> parts[i]).collect(Collectors.joining(" "));
    }

    private Workout parseWorkout(Player player, List<String> parts, String source) {
        Workout workout = new Workout();
        workout.player = player;
        workout.source = source;
        workout.height = parseDouble(parts.get(1));
        workout.weight = parseDouble(parts.get(2));
        workout.arm = parseDouble(parts.get(3));
        workout.hand = parseDouble(parts.get(4));
        workout.wing = parseDouble(parts.get(5));
        workout.forty = parseDouble(parts.get(6));
        workout.twenty = parseDouble(parts.get(7));
        workout.ten = parseDouble(parts.get(8));
        workout.vertical = parseDouble(parts.get(9));
        workout.broadJump = parseDouble(parts.get(10));
        workout.shortShuttle = parseDouble(parts.get(11));
        workout.threeCone = parseDouble(parts.get(12));
        workout.benchPress = parseInt(parts.get(13));
        return workout;
    }

    private int parseInt(String value){
        if(null == value || value.length() == 0 || "-".equals(value)){
            try{
                return Integer.parseInt(value);
            } catch(NumberFormatException e){
                log.info(e.toString());
            }
        }
        return 0;
    }

    private double parseDouble(String value){
        if(null != value && value.length() > 0 && !"-".equals(value)){
            if(value.indexOf("/") > -1){
                double ret = 0;
                String[] parts = value.split(" ");
                try {
                    for (String part : parts) {
                        if (part.indexOf("/") > -1) {
                            ret += Double.parseDouble(part.split("/")[0]) / Double.parseDouble(part.split("/")[1]);
                        } else {
                            ret += Double.parseDouble(part);
                        }
                    }
                } catch(NumberFormatException e){
                    log.info(e.toString());
                }
                return ret;
            }
            try{
                return Double.parseDouble(value);
            } catch(NumberFormatException e){
                log.info(e.toString());
            }
        }
        return 0;
    }

}
