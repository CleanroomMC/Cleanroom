package com.cleanroommc.boot;

import java.lang.reflect.InvocationTargetException;

public class MainServer extends Main {

    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        new MainServer().start(args);
    }

}
