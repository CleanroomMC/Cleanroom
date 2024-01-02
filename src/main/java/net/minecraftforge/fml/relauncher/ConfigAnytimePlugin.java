package net.minecraftforge.fml.relauncher;

import net.minecraftforge.common.ForgeVersion;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("ConfigAnytime")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public class ConfigAnytimePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return "net.minecraftforge.fml.common.ConfigAnytimeContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
