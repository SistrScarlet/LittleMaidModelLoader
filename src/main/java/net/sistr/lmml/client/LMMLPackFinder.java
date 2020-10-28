package net.sistr.lmml.client;

import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.PackCompatibility;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

//PackFinderはリソースパックを探すクラス
//これをResourcePackListに突っ込むことで、ゲーム内リソースパックから選ぶことができる
//Forgeでも同じことをやってModのリソースを読み込んでいる
@OnlyIn(Dist.CLIENT)
public class LMMLPackFinder implements IPackFinder {

    @SuppressWarnings("unchecked")
    public <T extends ResourcePackInfo> void addPackInfosToMap(Map<String, T> nameToPackMap, ResourcePackInfo.IFactory<T> packInfoFactory) {
        PackMetadataSection packMetadataSection = new PackMetadataSection(new StringTextComponent("Little Maid Model Loader"), 5);
        T pack = (T) new ClientResourcePackInfo("lmml", true, () -> ResourceWrapper.INSTANCE,
                new StringTextComponent(ResourceWrapper.INSTANCE.getName()), packMetadataSection.getDescription(),
                PackCompatibility.getCompatibility(packMetadataSection.getPackFormat()), ResourcePackInfo.Priority.TOP,
                false, null, false);
        nameToPackMap.put("lmml", pack);
    }

}
