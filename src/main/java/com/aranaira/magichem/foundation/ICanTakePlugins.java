package com.aranaira.magichem.foundation;

import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;

import java.util.List;

public interface ICanTakePlugins {
    void linkPluginsDeferred();
    void linkPlugins();
    void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin);
    List<AbstractDirectionalPluginBlockEntity> getPlugins();
}
