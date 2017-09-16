package com.saike.system.utility;

import java.util.ArrayList;
import java.util.List;

public class CommandBuilder {

    private List<String> commandList = new ArrayList<>();

    private CommandBuilder() {

    }

    public static CommandBuilder create() {
        return new CommandBuilder();
    }

    public CommandBuilder addCommand(String... commands) {
        if (commands != null && commands.length > 0) {
            for (String command : commands) {
                commandList.add(command);
            }
        }
        return this;
    }

    public List<String> build() {
        return commandList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (commandList.size() > 0) {
            for (String command : commandList) {
                builder.append(command).append(" ");
            }
        }
        return builder.toString();
    }
}
