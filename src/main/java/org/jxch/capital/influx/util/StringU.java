package org.jxch.capital.influx.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringU {

    public static String parameterExpression(String expression, Object... args) {
        Matcher matcher = Pattern.compile("\\?(\\d+)").matcher(expression);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1)) - 1;
            String replacement = (index >= 0 && index < args.length) ? args[index].toString() : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
