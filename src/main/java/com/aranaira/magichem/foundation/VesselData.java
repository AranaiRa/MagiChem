package com.aranaira.magichem.foundation;

import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.mna.tools.math.Vector3;

public class VesselData {
    public final AbstractMateriaStorageBlockEntity vesselBlockEntity;
    public final Vector3 origin, tangentVessel, tangentCenter;
    public final MateriaItem type;
    public final int amount;

    public VesselData(AbstractMateriaStorageBlockEntity pEntity, Vector3 pOrigin, Vector3 pTangentVessel, Vector3 pTangentCenter, MateriaItem pType, int pAmount) {
        this.vesselBlockEntity = pEntity;
        this.origin = pOrigin;
        this.tangentVessel = pTangentVessel;
        this.tangentCenter = pTangentCenter;
        this.type = pType;
        this.amount = pAmount;
    }
}