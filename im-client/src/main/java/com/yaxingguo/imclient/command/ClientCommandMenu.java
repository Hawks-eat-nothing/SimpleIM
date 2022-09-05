package com.yaxingguo.imclient.command;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@Service("ClientCommandMenu")
@Data
public class ClientCommandMenu implements BaseCommand{

    public static final String KEY = "0";

    private String allCommandsShow;
    private String commandInput;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String getTip() {
        return "show 所有命令";
    }

    @Override
    public void exec(Scanner scanner) {
        System.err.println("请输入某个操作指令："+allCommandsShow);
        //  获取第一个指令
        commandInput = scanner.next();
    }

    public void setAllCommand(Map<String,BaseCommand> commandMap){
        Set<Map.Entry<String,BaseCommand>> entries = commandMap.entrySet();
        Iterator<Map.Entry<String, BaseCommand>> iterator = entries.iterator();
        StringBuilder menus = new StringBuilder();
        menus.append("[menu]");
        while (iterator.hasNext()){
            BaseCommand next = iterator.next().getValue();
            menus.append(next.getKey())
                    .append("->")
                    .append(next.getTip())
                    .append("|");
        }
        allCommandsShow = menus.toString();
    }
}
