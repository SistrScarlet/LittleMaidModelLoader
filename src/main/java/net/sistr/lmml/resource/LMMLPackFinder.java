package net.sistr.lmml.resource;

import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.data.PackMetadataSection;

import java.util.function.Consumer;
import java.util.function.Supplier;

//PackFinderはリソースパックを探すクラス
//これをResourcePackListに突っ込むことで、ゲーム内リソースパックから選ぶことができる
//Forgeでも同じことをやってModのリソースを読み込んでいる
public class LMMLPackFinder implements IPackFinder {

    @Override
    public void findPacks(Consumer<ResourcePackInfo> infoConsumer, ResourcePackInfo.IFactory infoFactory) {
        String name = "lmml";
        boolean isAlwaysEnabled = true;
        Supplier<IResourcePack> supplier = () -> ResourceWrapper.INSTANCE;
        IResourcePack resourcePack = ResourceWrapper.INSTANCE;
        PackMetadataSection resourcePackMeta = ResourceWrapper.PACK_INFO;
        ResourcePackInfo.Priority priority = ResourcePackInfo.Priority.TOP;
        IPackNameDecorator decorator = IPackNameDecorator.PLAIN;
        ResourcePackInfo info = infoFactory.create(name, isAlwaysEnabled, supplier, resourcePack, resourcePackMeta,
                priority, decorator);
        infoConsumer.accept(info);
    }
}
