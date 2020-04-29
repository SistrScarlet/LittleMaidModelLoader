package net.sistr.lmml.util.loader;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformingClassLoader;

import java.lang.reflect.Field;

public class MultiModelClassLoader {

    public static Class<?> loadFile(String name, byte[] classFile) throws NoSuchFieldException, IllegalAccessException, NoClassDefFoundError {
        TransformingClassLoader classLoader = getForgeClassLoader();
        return classLoader.getClass(name, classFile);
    }

    private static TransformingClassLoader getForgeClassLoader() throws NoSuchFieldException, IllegalAccessException {
        Field field = Launcher.INSTANCE.getClass().getDeclaredField("classLoader");
        field.setAccessible(true);
        return (TransformingClassLoader) field.get(Launcher.INSTANCE);
    }

}
