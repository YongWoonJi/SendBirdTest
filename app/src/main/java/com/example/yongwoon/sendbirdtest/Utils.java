package com.example.yongwoon.sendbirdtest;

import android.util.Patterns;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by YongWoon on 2017-03-17.
 */

public class Utils {
    static SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm");
    static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy.MM.dd a hh:mm");
    static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy.MM.dd(E)");

    public static String convertTimeToString(long date) {
        return sdf.format(date);
    }

    public static String convertTimeToString2(long date) {
        return sdf2.format(date);
    }

    public static String convertTimeToString3(long date) {
        return sdf3.format(date);
    }

    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    public static String getGroupChannelTitle(GroupChannel channel) {
        List<User> members = channel.getMembers();

        if (members.size() < 2) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuffer names = new StringBuffer();
            for (User member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                names.append(", " + member.getNickname());
            }
            return names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuffer names = new StringBuffer();
            for (User member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                count++;
                names.append(", " + member.getNickname());

                if(count >= 10) {
                    break;
                }
            }
            return names.delete(0, 2).toString();
        }
    }

    public static List<String> extractUrls(String input)
    {
        List<String> result = new ArrayList<String>();

        String[] words = input.split("\\s+");


        Pattern pattern = Patterns.WEB_URL;
        for(String word : words)
        {
            if(pattern.matcher(word).find())
            {
                if(!word.toLowerCase().contains("http://") && !word.toLowerCase().contains("https://"))
                {
                    word = "http://" + word;
                }
                result.add(word);
            }
        }

        return result;
    }

    public static String generateMD5(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(data.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

        return hexString.toString();
    }

    public static void saveToFile(File file, String data) throws IOException {
        File tempFile = File.createTempFile("sendbird", "temp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(data.getBytes());
        fos.close();

        if(!tempFile.renameTo(file)) {
            throw new IOException("Error to rename file to " + file.getAbsolutePath());
        }
    }

    public static String loadFromFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }

}
