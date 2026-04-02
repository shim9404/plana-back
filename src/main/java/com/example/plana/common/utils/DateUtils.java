package com.example.plana.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    /**
     * 날짜 유효성 체크
     * @param date String 입력한 날짜
     * @return String 입력한 날짜 (없을 경우 현재 시간 반환)
     */
    public static String checkDate(String date) {
        return (date != null && !date.isBlank())
                ? date
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 날짜 텍스트 DateTime Format으로 파싱하여 반환
     * @param date 날짜 문자열
     * @return LocalDate 날짜
     */
    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
