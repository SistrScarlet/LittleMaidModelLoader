package net.sistr.lmml.util.loader;

import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.PackCompatibility;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;

//PackFinderはリソースパックを探すクラス
//これをResourcePackListに突っ込むことで、ゲーム内リソースパックから選ぶことができる
//なお、こちらではサーバーリソースパック(DownloadingPackFinder)の処理を応用して強制的に有効としている
//Forgeでも同じことをやってModのリソースを読み込んでいる
//多分1.12ではZip内のassets全部読み込んでたから特別な対応が要らなかった？わからん
public class LMMLPackFinder implements IPackFinder {

    public LMMLPackFinder() {}

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
