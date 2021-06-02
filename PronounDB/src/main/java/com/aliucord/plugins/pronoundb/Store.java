/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins.pronoundb;

import com.aliucord.Http;
import com.aliucord.Main;
import com.aliucord.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public final class Store {
    public static Map<Long, String> cache = new HashMap<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, Long.class, String.class).getType();
    private static final List<Long> buffer = new ArrayList<>();
    private static Thread timerThread = new Thread(Store::runThread);
    public static void fetchPronouns(Long id) {
        if (!timerThread.isAlive()) {
            if (timerThread.getState() == Thread.State.TERMINATED) timerThread = new Thread(Store::runThread);
            timerThread.start();
        }
        if (!buffer.contains(id)) buffer.add(id);
        try {
            timerThread.join();
        } catch (Throwable ignored) {}
    }

    private static void runThread() {
        try {
            Thread.sleep(50);
            Long[] bufferCopy = buffer.toArray(new Long[0]);
            buffer.clear();
            Map<Long, String> res = Utils.fromJson(Http.simpleGet(Constants.Endpoints.LOOKUP_BULK(bufferCopy)), resType);
            cache.putAll(res);
            for (Long id : bufferCopy) {
                if (!cache.containsKey(id)) cache.put(id, "unspecified");
            }
        } catch (Throwable e) {
            Main.logger.error("PronounDB error", e);
        }
    }
}