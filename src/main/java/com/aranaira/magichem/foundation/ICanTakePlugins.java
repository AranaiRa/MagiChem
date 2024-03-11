package com.aranaira.magichem.foundation;

public interface ICanTakePlugins {
    void linkPluginsDeferred();
    void linkPlugins();
    void removePlugin(DirectionalPluginBlockEntity pPlugin);
}
