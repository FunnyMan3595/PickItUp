package pickitup.core;

import cpw.mods.fml.relauncher.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

@IFMLLoadingPlugin.TransformerExclusions("pickitup.core.")
@IFMLLoadingPlugin.MCVersion("1.5.2")
public class CoreMod implements IFMLLoadingPlugin {
    public String[] getLibraryRequestClass() {
        return new String[] {};
    }

    public String[] getASMTransformerClass() {
        return new String[] {"pickitup.core.HookFinder"};
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) { }
}
