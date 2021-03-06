package com.oil.action;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.oil.tool.CommAction;
import com.oil.tool.Tool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/tool")
public class ToolAction extends CommAction {

    @RequestMapping(value = "/ip.do",method = RequestMethod.POST)
    @ResponseBody
    Object ip() {
        String ip = getIp();
        String ua = getUserAgent();
        String ipName = Tool.ipToLocation(ip);

        return success(ImmutableMap.of("ip", ip, "ipName", ipName));
    }

    @RequestMapping(value = "/ddl2bean.do",method = RequestMethod.POST)
    @ResponseBody
    Object ddl2bean(String ddl) {
        if(ddl == null) {
            return failure("ddl不能为空");
        }
        ddl = ddl.toLowerCase();
        ddl = ddl.replaceAll("\\(([0-9]+),([0-9]+)\\)", "($1_$2)");
        String[] ddls = ddl.split(",");
        StringBuilder bean = new StringBuilder();
        StringBuilder mapper = new StringBuilder();
        for (int i = 0; i < ddls.length; i++) {
            String str = ddls[i];
            str = str.trim();
            String[] str2 = str.split(" ");
            if(str2.length < 2) {
                continue;
            }
            String col = str2[0];
            String type = str2[1];
            if(type.matches("^number\\([0-9]+\\)")) {
                Matcher m = Pattern.compile("number\\(([0-9]+)\\)").matcher(type);
                if(m.find()) {
                    if(Integer.parseInt(m.group(1)) > 10) {
                        type = "Long";
                    } else {
                        type = "Integer";
                    }
                }
            } else if(type.matches("^number\\(([0-9]+)_([0-9]+)\\)")) {
                Matcher m = Pattern.compile("number\\(([0-9]+)_([0-9]+)\\)").matcher(type);
                if(m.find()) {
                    if(Integer.parseInt(m.group(2)) == 0) {
                        if(Integer.parseInt(m.group(1)) <= 10) {
                            type = "Integer";
                        } else {
                            type = "Long";
                        }
                    } else {
                        type = "Double";
                    }
                }
            } else if(type.startsWith("number")) {
                type = "Long";
            } else if(type.startsWith("timestamp") || type.startsWith("date")) {
                type = "Date";
            } else {
                type = "String";
            }
            String col2 = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, col);
            if("Date".equals(type)) {
                bean.append("@Temporal(TemporalType.TIMESTAMP)\r\n");
            }
            if("id".equals(col)) {
                bean.append("@Id\r\n");
                bean.append("@GeneratedValue(strategy = GenerationType.IDENTITY,generator = \"select xxx.nextval from dual\")\r\n");
            }
            bean.append("private "+type+" " + col2 + ";\r\n");

            if(col.equalsIgnoreCase("id")) {
                mapper.append("<id column=\""+col+"\" property=\""+col2+"\"/>\r\n");
            } else {
                mapper.append("<result column=\""+col+"\" property=\""+col2+"\"/>\r\n");
            }
        }
        return success( ImmutableMap.of("bean", bean.toString(), "mapper", mapper.toString()));
    }

    public static void main(String[] args) {

        String x = "number(15_4)";
        Matcher m = Pattern.compile("number\\(([0-9]+)_([0-9]+)\\)").matcher(x);
//        System.out.println(m.find());
        System.out.println(m.groupCount());
        System.out.println(m.group());
//        String a1 = m.group(0);
//        System.out.println(a1);



    }
}
