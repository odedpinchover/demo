import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class runner {

    private static final DecimalFormat decfor = new DecimalFormat(consts.decimalFormat);

    public static void main(String[] args) throws JSONException, IOException, URISyntaxException {

        //current day, format date
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int todayDay = now.getDay();
        SimpleDateFormat format1 = new SimpleDateFormat(consts.dateFormat);


        //init data structure for days and a collection of all magnitudes for this day
        Map<Integer, List<Double>> dayForMagnitudes = initDSforDaysToCollectionOfMagnitude();

        //init data structure for maximum magnitude for each day of the week
        Map<Integer, MyPair> dayForMaxMag = initDSforDayToMaxMagnitude();

        //init data structure for avg magnitude for each day of the week
        Map<Integer, Double> dayForAvgMag = initDSforDayToAvgMagnitude();

        // parsing data from a file
        JSONObject jo = new JSONObject(GetData.getData());
        JSONArray features = (JSONArray) jo.get(consts.features);

        //number of items
        int length = features.length();

        //for each item - search magnitude for each day, and hold maximum value for each day
        for (int index = 0; index < length; index++) {
            JSONObject properties = features.getJSONObject(index).getJSONObject(consts.properties);
            long time = properties.getLong(consts.time);

            Date date = new Date(time);
            int day = date.getDay();
            double currentMagnitude = properties.getDouble(consts.magnitude);

            //add new magnitude value for a specific day
            dayForMagnitudes.get(day).add(currentMagnitude);

            //if the current currentMagnitude is higher then the max currentMagnitude for that day - set the current currentMagnitude as the max for this day
            if (dayForMaxMag.get(day).getMax() < currentMagnitude) {
                setMaxMagnitudeForSpecificDay(dayForMaxMag, properties, day, currentMagnitude);
            }
        }

        //calculate avg mag for each day
        for (int day = 0; day < 7; day++) {
            calcAvgMagnitudeForEachDay(dayForMagnitudes, dayForAvgMag, day);
        }


        //print to console
        StringBuffer sb = new StringBuffer();
        printToConsole(cal, todayDay, format1, dayForMaxMag, dayForAvgMag, sb);

        //print to file
        printToFile(sb);
    }

    private static void printToFile(StringBuffer sb) {
        String fileName = consts.outputFileName; // File name

        // Ensure we can write to the file
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            // Create a FileWriter
            FileWriter writer = new FileWriter(file);

            // Write content to the file
            writer.write(sb.toString());

            // Close the writer
            writer.close();

            System.out.println("Data has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printToConsole(Calendar cal, int todayDay, SimpleDateFormat format1, Map<Integer, MyPair> dayForMaxMag, Map<Integer, Double> dayForAvgMag, StringBuffer sb) {
        for (int index = 0; index < 7; index++) {
            MyPair myPair = dayForMaxMag.get(todayDay);
            sb.append(format1.format(cal.getTime()) + " avg: " + decfor.format(dayForAvgMag.get(todayDay)) + " max: " + myPair.getMax() + " location: " + myPair.getLocation());
            sb.append(System.getProperty("line.separator"));
            cal.add(Calendar.DAY_OF_MONTH, -1);

            // todayDay = ((todayDay-1)%7); todo - check why this didnt worked - this should return a number 0<->6
            todayDay--;
            if (todayDay < 0) {
                todayDay = todayDay + 6;
            }
        }
        System.out.println(sb);
    }

    private static void calcAvgMagnitudeForEachDay(Map<Integer, List<Double>> dayForMagnitudes, Map<Integer, Double> dayForAvgMag, int day) {
        List<Double> doubles = dayForMagnitudes.get(day);
        int numberOfDays = doubles.size();
        double totalSum = doubles.stream().mapToDouble(d -> d.doubleValue()).sum();
        double avg = totalSum / numberOfDays;
        dayForAvgMag.put(day, avg);
    }

    private static void setMaxMagnitudeForSpecificDay(Map<Integer, MyPair> dayForMaxMag, JSONObject properties, int day, double currentMagnitude) throws JSONException {
        MyPair myPair = dayForMaxMag.get(day);
        myPair.setMax(currentMagnitude);
        myPair.setLocation(properties.getString(consts.place));
    }

    private static Map<Integer, Double> initDSforDayToAvgMagnitude() {
        Map<Integer, Double> dayForAvgMag = new HashMap<>();
        for (int day = 0; day < 7; day++) {
            dayForAvgMag.put(day, 0d);
        }
        return dayForAvgMag;
    }

    private static Map<Integer, MyPair> initDSforDayToMaxMagnitude() {
        Map<Integer, MyPair> dayForMaxMag = new HashMap<>();
        for (int day = 0; day < 7; day++) {
            dayForMaxMag.put(day, new MyPair(0d, ""));
        }
        return dayForMaxMag;
    }

    private static Map<Integer, List<Double>> initDSforDaysToCollectionOfMagnitude() {
        Map<Integer, List<Double>> dayForMagnitudes = new HashMap<>();
        for (int day = 0; day < 7; day++) {
            dayForMagnitudes.put(day, new ArrayList<>());
        }
        return dayForMagnitudes;
    }

    public static class MyPair {
        Double max;
        String location;

        public MyPair(Double max, String location) {
            this.max = max;
            this.location = location;
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}


