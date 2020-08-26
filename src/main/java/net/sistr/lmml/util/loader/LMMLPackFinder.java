package net.sistr.lmml.util.loader;

import net.minecraft.resources.*;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;

//PackFinderはリソースパックを探すクラス
//これをResourcePackListに突っ込むことで、ゲーム内リソースパックから選ぶことができる
//なお、こちらではサーバーリソースパック(DownloadingPackFinder)の処理を応用して強制的に有効としている
//Forgeでも同じことをやってModのリソースを読み込んでいる
//多分1.12ではZip内のassets全部読み込んでたから特別な対応が要らなかった？わからん
public class LMMLPackFinder implements IPackFinder {

    public LMMLPackFinder() {}

    @Override
    public void func_230230_a_(Consumer<ResourcePackInfo> nameToPackMap, ResourcePackInfo.IFactory packInfoFactory) {
        PackMetadataSection packMetadataSection = new PackMetadataSection(new StringTextComponent("Little Maid Model Loader"), 5);
        ResourcePackInfo resourcePackInfo = new ResourcePackInfo("lmml", true, () -> ResourceWrapper.INSTANCE,
                new StringTextComponent(ResourceWrapper.INSTANCE.getName()), packMetadataSection.getDescription(),
                PackCompatibility.getCompatibility(packMetadataSection.getPackFormat()), ResourcePackInfo.Priority.TOP,
                true, IPackNameDecorator.field_232625_a_, false);
        nameToPackMap.accept(resourcePackInfo);
    }
}
