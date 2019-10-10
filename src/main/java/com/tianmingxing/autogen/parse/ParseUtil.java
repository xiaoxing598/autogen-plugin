package com.tianmingxing.autogen.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表结构解析工具类
 *
 * @author tianmingxing <mx.tian@qq.com>
 * @date 2019-10-09
 */
public class ParseUtil {

    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("`(\\w+)`");

    private ParseUtil() {
    }

    public static TableSchemaDto parse(String tableSchemaStr) {
        TableSchemaDto tableSchemaDto = new TableSchemaDto();
        if (Objects.isNull(tableSchemaStr)) {
            return tableSchemaDto;
        }

        String[] lines = tableSchemaStr.split("\n");
        List<TableSchemaDto.Field> fields = new ArrayList<>(lines.length - 2);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (i == 0) {
                //首行，解析出表名
                Matcher matcher = FIELD_NAME_PATTERN.matcher(line);
                if (matcher.find()) {
                    String tableNameStr = matcher.group(1);
                    tableSchemaDto.setTableName(tableNameStr);
                }
            } else if (i == lines.length - 1) {
                //尾行，暂时无动作
            } else {
                if (line.startsWith("`")) {
                    //字段行，按照字段来进行解析
                    TableSchemaDto.Field field = new TableSchemaDto.Field();

                    String[] segments = line.split(" ");
                    for (int j = 0; j < segments.length; j++) {
                        if (segments[j].startsWith("`")) {
                            field.setName(segments[j].substring(1, segments[j].length() - 1));
                            continue;
                        }

                        if (segments[j].startsWith("bigint")) {
                            field.setType("Long");
                            continue;
                        } else if (segments[j].startsWith("int")) {
                            field.setType("Integer");
                            continue;
                        } else if (segments[j].startsWith("varchar")) {
                            field.setType("String");
                            continue;
                        } else if (segments[j].startsWith("datetime") || segments[j].startsWith("timestamp")) {
                            field.setType("Date");
                            continue;
                        }

                        if ("NOT".equals(segments[j])) {
                            field.setNull(false);
                            ++j;
                            continue;
                        } else if ("NULL".equals(segments[j])) {
                            field.setNull(true);
                            continue;
                        }

                        if ("DEFAULT".equals(segments[j])) {
                            field.setNull(true);
                            if ("NULL".equals(segments[++j])) {
                                field.setDefaultValue(null);
                            } else {
                                field.setDefaultValue(segments[j].substring(1, segments[j].length() - 1));
                            }
                            continue;
                        }

                        if ("AUTO_INCREMENT".equals(segments[j])) {
                            tableSchemaDto.setPrimaryKeyField(field.getName());
                            continue;
                        }

                        if ("COMMENT".equals(segments[j])) {
                            field.setDesc(segments[++j].substring(1, segments[j].length() - 2));
                        }
                    }

                    fields.add(field);
                } else {
                    //否则就是字段索引设置，暂时无动作
                }
            }
        }

        tableSchemaDto.setFields(fields);
        return tableSchemaDto;
    }

}
