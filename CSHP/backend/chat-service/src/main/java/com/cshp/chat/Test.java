package com.cshp.chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {
    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date birthDate = sdf.parse(line);
        String targetDate = sdf.format("2020-10-01");
        System.out.println(targetDate);

    }
}
