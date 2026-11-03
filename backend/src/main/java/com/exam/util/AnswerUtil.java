package com.exam.util;

import java.util.Arrays;

public final class AnswerUtil {

    private AnswerUtil() {
    }

    public static boolean isCorrect(String standardAnswer, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return false;
        }
        return normalize(standardAnswer).equals(normalize(studentAnswer));
    }

    public static String normalize(String answer) {
        if (answer == null) {
            return "";
        }
        String value = answer.trim().replaceAll("[\\s,，;；、]", "");
        if (value.equalsIgnoreCase("true") || value.equals("正确")) {
            return "TRUE";
        }
        if (value.equalsIgnoreCase("false") || value.equals("错误")) {
            return "FALSE";
        }
        char[] chars = value.toUpperCase().toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
