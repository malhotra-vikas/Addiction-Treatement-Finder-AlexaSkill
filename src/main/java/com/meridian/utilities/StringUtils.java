package main.java.com.meridian.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public StringUtils () {
    }

    public String[] splitByLength (String parentString, int chunkSize) {
        int numberOfSubstrings = (int) Math.ceil((double) parentString.length() / chunkSize);

        String[] subStringArray = new String[numberOfSubstrings];
        String subString = "";

        int index = 0;
        for (int i = 0; i < parentString.length(); i = i + chunkSize) {
            if (parentString.length() - i < chunkSize) {
                subString = parentString.substring(i);
                subStringArray[index++] = subString;
//                System.out.println(subString);
            } else {
                subString = parentString.substring(i, i + chunkSize);
                subStringArray[index++] = subString;
//                System.out.println(subString);

            }
        }
        return subStringArray;
    }

    public List<String> splitString(String parentString, int lineSize) {
        List<String> res = new ArrayList<>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(parentString);

        while(m.find()) {
//            System.out.println(m.group().trim());   // Debug
            res.add(m.group());
        }
//        System.out.println("-----------");

//        System.out.println(m.group());

        return res;
    }


    public String[] splitByWordCount (String parentString, int chunkSize) {
        int numberOfSubstrings = (int) Math.ceil((double) parentString.split(" ").length / chunkSize);
        String [] listOfAllWords = parentString.split(" ");

        String[] subStringArray = new String[numberOfSubstrings];
        String subString = "";

        int index = 0;
        for (int i = 0; i < listOfAllWords.length; i = i + chunkSize) {
            if (listOfAllWords.length - i < chunkSize) {
                subString = parentString.substring(i);
                subStringArray[index++] = subString;
//                System.out.println(subString);
            } else {
                subString = parentString.substring(i, i + chunkSize);
                subStringArray[index++] = subString;
//                System.out.println(subString);

            }
        }
        return subStringArray;

    }
}