package com.yaxingguo.imclient.command;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

/**
 * 此命令负责收集类负责从Scanner控制台实例获取聊天的信息，格式为id:message
 * @author Yaxing_Guo
 */

@Data
@Service("ChatConsoleCommand")
public class ChatConsoleCommand implements BaseCommand{

    private String toUserId;
    private String message;
    public static final String KEY = "2";

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String getTip() {
        return null;
    }

    @Override
    public void exec(Scanner scanner) {
        System.out.println("请输入聊天消息(id：message): ");
        String[] info = null;
        while (true){
            String input = scanner.next();
            info = input.split(":");
            if (info.length!=2){
                System.out.println("请按以下格式输入聊天消息(id：message): ");
            }else {
                break;
            }
        }
        toUserId = info[0];
        message = info[1];
    }
}
