package net.sistr.lmml.resource;

import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.PackCompatibility;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.StringTextComponent;
import net.sistr.lmml.resource.ResourceWrapper;

import java.util.Map;

//PackFinderはリソースパックを探すクラス
//これをResourcePackListに突っ込むことで、ゲーム内リソースパックから選ぶことができる
//Forgeでも同じことをやってModのリソースを読み込んでいる
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
