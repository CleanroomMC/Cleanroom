package net.minecraftforge.common.config;

public class ModConfiguration extends Configuration {
    public final String modId;
    public final Class<?> clazz;

    public ModConfiguration(String modidIn, Class<?> clazzIn, ConfigurationFileWrapper fileWrapper){
        super(fileWrapper);
        modId=modidIn;
        clazz=clazzIn;
    }
}
