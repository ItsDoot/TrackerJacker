package io.github.xdotdash.trackerjacker.util;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;

public class Tasks {

    public static TaskChainFactory factory;

    public static <T> TaskChain<T> newChain() {
        return factory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return factory.newSharedChain(name);
    }
}