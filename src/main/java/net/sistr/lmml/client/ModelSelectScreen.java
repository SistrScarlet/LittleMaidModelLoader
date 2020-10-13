package net.sistr.lmml.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.entity.IHasMultiModel;
import net.sistr.lmml.maidmodel.EntityCaps;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelMultiBase;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.network.PacketSyncMultiModel;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.util.*;
import net.sistr.lmml.resource.manager.ModelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//todo 染料消費、保持染料ハイライト、現在モデルハイライト、isContract、スクロールを個別化
//GUIが小さくならない画面の最低サイズ640x480を想定して組む
//ただし、描画は320x240でやって倍にされるっぽい
@OnlyIn(Dist.CLIENT)
public class ModelSelectScreen extends Screen {
    public static final ResourceLocation EMPTY_TEXTURE =
            new ResourceLocation(LittleMaidModelLoader.MODID, "textures/empty.png");
    public static final TexturePair EMPTY_TEXTURE_PAIR = new TexturePair(EMPTY_TEXTURE, null);
    public static final ArmorPartModelData EMPTY_ARMOR_DATA =
            new ArmorPartModelData(null, null, null, null,
                    null, null);
    public static final ResourceLocation MODEL_SELECT_GUI_TEXTURE =
            new ResourceLocation(LittleMaidModelLoader.MODID, "textures/gui/model_select.png");
    private static final ItemStack ARMOR = Items.DIAMOND_CHESTPLATE.getDefaultInstance();
    protected static final int WIDTH = 256;
    protected static final int HEIGHT = 151;
    private final ModelGUI modelGUI;
    private final GUI armorGUI;
    private final IHasMultiModel owner;
    private int scroll;
    private boolean guiSwitch = true;

    public ModelSelectScreen(ITextComponent titleIn, World world, IHasMultiModel owner) {
        super(titleIn);
        this.owner = owner;
        DummyModelEntity dummy = new DummyModelEntity(world);
        Collection<TextureHolder> textureHolders =
                LittleMaidModelLoader.getInstance().getTextureManager().getAllTextures();
        Map<String, TextureHolder> map = new HashMap<>();
        textureHolders.forEach(textureHolder -> map.put(textureHolder.getTextureName().toLowerCase(), textureHolder));
        ImmutableList<TextureHolder> textures =
                ImmutableList.copyOf(textureHolders.stream()
                        .map(TextureHolder::getTextureName)
                        .map(String::toLowerCase)
                        .sorted(Comparator.naturalOrder())
                        .map(map::get)
                        .collect(Collectors.toList()));
        ScreenInfo screenInfo = new ScreenInfo(() -> width, () -> height, scroll -> this.scroll = scroll, () -> scroll);
        modelGUI = new ModelGUI(WIDTH - 16, HEIGHT - 16, screenInfo, dummy, owner, textures);
        armorGUI = new ArmorGUI(WIDTH - 16, HEIGHT - 16, screenInfo, dummy, owner, textures);
    }

    public static void renderColor(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int rgba) {
        fill(matrixStack, minX, minY, maxX, maxY, rgba);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(MODEL_SELECT_GUI_TEXTURE);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, WIDTH, HEIGHT);

        Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(ARMOR, relX, relY + HEIGHT + 8);
        this.minecraft.getTextureManager().bindTexture(MODEL_SELECT_GUI_TEXTURE);
        this.blit(matrixStack, relX, relY + HEIGHT + 8, 0, 240, 16, 16);
        
        if (guiSwitch)
            modelGUI.render(matrixStack, mouseX, mouseY, partialTicks);
        else
            armorGUI.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int minX = (this.width - WIDTH) / 2;
        int minY = (this.height - HEIGHT) / 2 + HEIGHT + 8;
        if (minX <= x && x < minX + 16 && minY <= y && y < minY + 16) {
            guiSwitch = !guiSwitch;
            playDownSound();
            return true;
        }
        return guiSwitch ? modelGUI.mouseClicked(x, y, button) : armorGUI.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        return guiSwitch ? modelGUI.mouseDragged(x, y, button, p_mouseDragged_6_, p_mouseDragged_8_)
                : armorGUI.mouseDragged(x, y, button, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmount) {
        boolean scrollUp = scrollAmount < 0;
        scroll = Math.max(scroll + (scrollUp ? 1 : -1), 0);
        return true;
    }

    @Override
    public void tick() {
        if (guiSwitch)
            modelGUI.tick();
        else
            armorGUI.tick();
    }

    @Override
    public void onClose() {
        super.onClose();
        modelGUI.onClose();
        armorGUI.onClose();
        if (owner instanceof Entity) {
            ArmorsHolder<String> armorNames = new ArmorsHolder<>();
            for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                armorNames.setArmor(owner.getTextureHolder(IHasMultiModel.Layer.INNER, part).getTextureName(), part);
            }
            Networking.INSTANCE.sendToServer(new PacketSyncMultiModel(((Entity) owner).getEntityId(),
                    owner.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).getTextureName(),
                    armorNames, owner.getColor(), owner.isContract()));
        }
    }

    public static void playDownSound() {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static class ModelGUI implements GUI {
        private final int width;
        private final int height;
        private final ScreenInfo screenInfo;
        private final DummyModelEntity dummy;
        private final List<TextureHolder> loadedTexture = new ArrayList<>();
        private final IHasMultiModel owner;
        private final ScrollBar scrollBar;
        private final boolean isContract = true;
        private int scrollLine = 0;
        private int selectLine = -1;
        private TextureColor selectColor = null;
        private final int maxAnimation = 10;
        private int animation;
        private int prevAnimation;
        private boolean animating;

        public ModelGUI(int width, int height, ScreenInfo screenInfo, DummyModelEntity dummy, IHasMultiModel owner,
                        Collection<TextureHolder> textures) {
            this.width = width;
            this.height = height;
            this.screenInfo = screenInfo;
            this.dummy = dummy;
            this.owner = owner;
            ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
            textures.stream().filter(textureHolder ->
                    textureHolder.hasSkinTexture(isContract) &&
                            modelManager.getModel(textureHolder.getModelName(), IHasMultiModel.Layer.SKIN).isPresent())
                    .forEach(loadedTexture::add);
            Percent scrollPercent = new Percent(() -> (float) ModelGUI.this.scrollLine / (loadedTexture.size() - 1),
                    percent -> {
                        int line = (int) MathHelper.clamp(
                                percent * loadedTexture.size(), 0, loadedTexture.size() - 1);
                        ModelGUI.this.scrollLine = line;
                        screenInfo.setScroll(line);
                    });
            scrollBar =
                    new ScrollBar(16, height, (width + 32) / 2, -height / 2, screenInfo, scrollPercent);
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            if (scrollLine != screenInfo.getScroll()) {
                scrollLine = MathHelper.clamp(screenInfo.getScroll(), 0, loadedTexture.size() - 1);
                screenInfo.setScroll(scrollLine);
            }
            scrollBar.render(matrixStack, mouseX, mouseY, partialTicks);
            if (selectLine != -1 && 0 <= selectLine - scrollLine && selectLine - scrollLine < 3) {
                int minX = (screenInfo.getWidth() - width) / 2;
                int maxX = (screenInfo.getWidth() + width) / 2;
                int minY = (screenInfo.getHeight() - height) / 2 + (selectLine - scrollLine) * (height / 3);
                int maxY = minY + height / 3;
                renderColor(matrixStack, minX, minY, maxX, maxY, 0x60FFFFFF);
            }
            if (selectColor != null) {
                int minX = (screenInfo.getWidth() - width) / 2 + selectColor.getIndex() * (width / 16);
                int maxX = minX + (width / 16);
                int minY = (screenInfo.getHeight() - height) / 2;
                int maxY = (screenInfo.getHeight() + height) / 2;
                renderColor(matrixStack, minX, minY, maxX, maxY, selectColor.getColorCode() | 0x40000000);
            }
            for (int i = 0; i < 3; i++) {
                if (loadedTexture.size() <= scrollLine + i) {
                    return;
                }
                TextureHolder holder = loadedTexture.get(scrollLine + i);
                int baseX = (screenInfo.getWidth() - width) / 2;
                int baseY = (screenInfo.getHeight() - height) / 2;
                int appendY = (height / 3) * (i + 1);
                ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
                ModelMultiBase model = modelManager.getModel(holder.getModelName(), IHasMultiModel.Layer.SKIN)
                        .orElseThrow(IllegalStateException::new);
                renderAllColorModel(matrixStack, baseX, baseY + appendY, 15,
                        screenInfo.getWidth() / 2F - mouseX,
                        screenInfo.getHeight() / 2F - mouseY, model, holder);
            }
            animating = (screenInfo.getWidth() + width) / 2F - 45 <= mouseX
                    && mouseX < (screenInfo.getWidth() + width) / 2F + 45
                    && (screenInfo.getHeight() + height) / 2F <= mouseY;
            if (selectLine != -1) {
                ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
                ModelMultiBase model =
                        modelManager.getModel(loadedTexture.get(selectLine).getModelName(), IHasMultiModel.Layer.SKIN)
                                .orElseThrow(() -> new IllegalStateException("モデルが存在しません。"));
                float animate = MathHelper.lerp(partialTicks, prevAnimation, animation) / maxAnimation;
                animate = MathHelper.sin(animate);
                renderModel((screenInfo.getWidth() + width) / 2,
                        screenInfo.getHeight()
                                + (int) ((model.getHeight(dummy.getCaps()) * 45F
                                - ((screenInfo.getHeight() - height) / 2F - 22.5F)) * (1 - animate)),
                        45, (screenInfo.getWidth() + width) / 2F - mouseX,
                        (screenInfo.getHeight() + height) / 2F + 22.5F - mouseY,
                        model,
                        getTexturePair(loadedTexture.get(selectLine), selectColor, isContract)
                                .orElseThrow(() -> new IllegalStateException("テクスチャが存在しません。")));
            }

        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return clickModel(x, y, button) || scrollBar.mouseClicked(x, y, button);
        }

        protected boolean clickModel(double x, double y, int button) {
            if (button != 0 && button != 1) {
                return false;
            }
            int minX = (screenInfo.getWidth() - width) / 2;
            int maxX = (screenInfo.getWidth() + width) / 2;
            int minY = (screenInfo.getHeight() - height) / 2;
            int maxY = (screenInfo.getHeight() + height) / 2;
            if (!(minX <= x && x < maxX && minY <= y && y < maxY)) {
                return false;
            }
            int selectLine = (int) ((y - minY) / (height / 3));
            if (loadedTexture.size() <= scrollLine + selectLine) {
                return false;
            }
            TextureHolder textureHolder = loadedTexture.get(scrollLine + selectLine);
            TextureColor selectColor = TextureColor.getColor((int) ((x - minX) / (width / 16)));
            if (!textureHolder.getTexture(selectColor, isContract, false).isPresent()
                    && !textureHolder.getTexture(selectColor, isContract, true).isPresent()) {
                return false;
            }
            if (button == 1 || this.selectLine == scrollLine + selectLine && this.selectColor == selectColor) {
                this.selectLine = -1;
                this.selectColor = null;
            } else {
                this.selectLine = scrollLine + selectLine;
                this.selectColor = selectColor;
            }
            playDownSound();
            return true;
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double d, double d2) {
            return scrollBar.mouseDragged(x, y, button, d, d2);
        }

        @Override
        public void onClose() {
            //選択しているなら
            if (0 <= selectLine && selectLine <= loadedTexture.size()) {
                //カラーと契約を更新
                owner.setColor(selectColor);
                owner.setContract(isContract);
                //スキンを更新
                TextureHolder skin = loadedTexture.get(selectLine);
                owner.setTextureHolder(skin, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD);
                //防具をスキンと同様に更新
                for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                    TextureHolder armorTexture = loadedTexture.get(selectLine);
                    owner.setTextureHolder(armorTexture, IHasMultiModel.Layer.INNER, part);
                }
            }
        }

        @Override
        public void tick() {
            prevAnimation = animation;
            animation = MathHelper.clamp(animation + (animating ? 1 : -1), 0, maxAnimation);
        }

        public void renderAllColorModel(MatrixStack matrixStack, int posX, int posY, int scale, float mouseX, float mouseY,
                                        ModelMultiBase model, TextureHolder holder) {
            for (TextureColor color : TextureColor.values()) {
                getTexturePair(holder, color, isContract).ifPresent(texturePair ->
                        renderModel(posX + (color.getIndex() + 1) * scale - scale / 2, posY, scale,
                                mouseX, mouseY, model, texturePair));
            }
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            fontRenderer.drawString(matrixStack, holder.getTextureName(),
                    posX, posY - 45, 0xFFFFFFFF);
        }

        public Optional<TexturePair> getTexturePair(TextureHolder holder, TextureColor color, boolean isContract) {
            Optional<ResourceLocation> optional = holder.getTexture(color, isContract, false);
            return optional.map(resourceLocation ->
                    new TexturePair(resourceLocation,
                            holder.getTexture(color, isContract, true).orElse(null)));
        }

        public void renderModel(int posX, int posY, int scale, float mouseX, float mouseY,
                                ModelMultiBase model, TexturePair texturePair) {
            dummy.setSkinModel(model);
            dummy.setSkinTexture(texturePair);
            for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                dummy.setArmorData(ModelSelectScreen.EMPTY_ARMOR_DATA, part);
            }
            dummy.setAllArmorVisible(false);
            InventoryScreen.drawEntityOnScreen(posX, posY, scale, mouseX, mouseY, dummy);
        }

    }

    //ModelGUIと似通った部分が多いが、微妙に違って実装に悩む…
    public static class ArmorGUI implements GUI {
        private static final ArmorsHolder<ItemStack> ARMOR_ICONS = new ArmorsHolder<>();
        private final int width;
        private final int height;
        private final ScreenInfo screenInfo;
        private final DummyModelEntity dummy;
        private final IHasMultiModel owner;
        private final List<TextureHolder> loadedTexture = new ArrayList<>();
        private final ScrollBar scrollBar;
        private final ArmorsHolder<Integer> armors = new ArmorsHolder<>();
        private int scrollLine = 0;
        private int selectLine = -1;
        private final int maxAnimation = 10;
        private int animation;
        private int prevAnimation;
        private boolean animating;

        static {
            ARMOR_ICONS.setArmor(Items.DIAMOND_HELMET.getDefaultInstance(), IHasMultiModel.Part.HEAD);
            ARMOR_ICONS.setArmor(Items.DIAMOND_CHESTPLATE.getDefaultInstance(), IHasMultiModel.Part.BODY);
            ARMOR_ICONS.setArmor(Items.DIAMOND_LEGGINGS.getDefaultInstance(), IHasMultiModel.Part.LEGS);
            ARMOR_ICONS.setArmor(Items.DIAMOND_BOOTS.getDefaultInstance(), IHasMultiModel.Part.FEET);
        }

        public ArmorGUI(int width, int height, ScreenInfo screenInfo, DummyModelEntity dummy, IHasMultiModel owner,
                        Collection<TextureHolder> textures) {
            this.width = width;
            this.height = height;
            this.screenInfo = screenInfo;
            this.dummy = dummy;
            this.owner = owner;
            ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
            textures.stream().filter(textureHolder ->
                    textureHolder.hasArmorTexture() &&
                            modelManager.getModel(textureHolder.getModelName(), IHasMultiModel.Layer.INNER).isPresent())
                    .forEach(loadedTexture::add);
            Percent scrollPercent = new Percent(() -> (float) ArmorGUI.this.scrollLine / (loadedTexture.size() - 1),
                    percent -> {
                        int line = (int) MathHelper.clamp(
                                percent * loadedTexture.size(), 0, loadedTexture.size() - 1);
                        ArmorGUI.this.scrollLine = line;
                        screenInfo.setScroll(line);
                    });
            scrollBar = new ScrollBar(16, height, (width + 32) / 2, -height / 2, screenInfo, scrollPercent);
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            if (scrollLine != screenInfo.getScroll()) {
                scrollLine = MathHelper.clamp(screenInfo.getScroll(), 0, loadedTexture.size() - 1);
                screenInfo.setScroll(scrollLine);
            }
            scrollBar.render(matrixStack, mouseX, mouseY, partialTicks);
            if (selectLine != -1 && 0 <= selectLine - scrollLine && selectLine - scrollLine < 3) {
                int minX = (screenInfo.getWidth() - width) / 2;
                int maxX = (screenInfo.getWidth() + width) / 2;
                int minY = (screenInfo.getHeight() - height) / 2 + (selectLine - scrollLine) * (height / 3);
                int maxY = minY + height / 3;
                renderColor(matrixStack, minX, minY, maxX, maxY, 0x60FFFFFF);
            }
            for (int i = 0; i < 3; i++) {
                if (loadedTexture.size() <= scrollLine + i) {
                    return;
                }
                TextureHolder holder = loadedTexture.get(scrollLine + i);
                int baseX = (screenInfo.getWidth() - width) / 2;
                int baseY = (screenInfo.getHeight() - height) / 2;
                int appendY = (height / 3) * (i + 1);
                ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
                ModelMultiBase model = modelManager.getModel(holder.getModelName(), IHasMultiModel.Layer.SKIN)
                        .orElseThrow(IllegalStateException::new);
                renderAllArmorModel(matrixStack, baseX, baseY + appendY, 15,
                        screenInfo.getWidth() / 2F - mouseX,
                        screenInfo.getHeight() / 2F - mouseY, model, holder);
            }
            animating = (screenInfo.getWidth() + width) / 2F - 45 <= mouseX
                    && mouseX < (screenInfo.getWidth() + width) / 2F + 45
                    && (screenInfo.getHeight() + height) / 2F <= mouseY;
            if (selectLine != -1) {
                ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
                ModelMultiBase model =
                        modelManager.getModel(loadedTexture.get(selectLine).getModelName(), IHasMultiModel.Layer.SKIN)
                                .orElseThrow(() -> new IllegalStateException("モデルが存在しません。"));
                float animate = MathHelper.lerp(partialTicks, prevAnimation, animation) / maxAnimation;
                animate = MathHelper.sin(animate);
                TextureHolder textureHolder = loadedTexture.get(selectLine);
                List<String> armorNames = textureHolder.getArmorNames().stream()
                        .map(String::toLowerCase).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
                String armorName;
                if (armorNames.contains("default"))
                    armorName = "default";
                else
                    armorName = armorNames.get(0);
                ResourceLocation innerTex = textureHolder.getArmorTexture(IHasMultiModel.Layer.INNER, armorName,
                        0, false).orElse(null);
                ResourceLocation innerLightTex = textureHolder.getArmorTexture(IHasMultiModel.Layer.INNER, armorName,
                        0, true).orElse(null);
                ResourceLocation outerTex = textureHolder.getArmorTexture(IHasMultiModel.Layer.OUTER, armorName,
                        0, false).orElse(null);
                ResourceLocation outerLightTex = textureHolder.getArmorTexture(IHasMultiModel.Layer.OUTER, armorName,
                        0, true).orElse(null);
                ModelMultiBase innerModel = modelManager.getModel(textureHolder.getModelName(), IHasMultiModel.Layer.INNER)
                        .orElseThrow(() -> new IllegalStateException("モデルが存在しません"));
                ModelMultiBase outerModel = modelManager.getModel(textureHolder.getModelName(), IHasMultiModel.Layer.OUTER)
                        .orElseThrow(() -> new IllegalStateException("モデルが存在しません"));
                ArmorPartModelData armorData = new ArmorPartModelData(innerTex, innerLightTex, outerTex, outerLightTex,
                        innerModel, outerModel);
                renderArmorPart((screenInfo.getWidth() + width) / 2,
                        screenInfo.getHeight()
                                + (int) ((model.getHeight(dummy.getCaps()) * 45F
                                - ((screenInfo.getHeight() - height) / 2F - 22.5F)) * (1 - animate)),
                        45, (screenInfo.getWidth() + width) / 2F - mouseX,
                        (screenInfo.getHeight() + height) / 2F + 22.5F - mouseY, model, armorData);
            }

            if ((screenInfo.getWidth() - width) / 2 <= mouseX && mouseX < (screenInfo.getWidth() + width) / 2
                    && (screenInfo.getHeight() - height) / 2 <= mouseY && mouseY < (screenInfo.getHeight() + height) / 2) {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                int left = (screenInfo.getWidth() + width) / 2 - 32;
                int right = left + 16;
                int up = (screenInfo.getHeight() - height) / 2
                        + (int) ((mouseY - (screenInfo.getHeight() - height) / 2F) / (height / 3F)) * 45;
                int down = up + 16;
                itemRenderer.renderItemIntoGUI(ARMOR_ICONS.getArmor(IHasMultiModel.Part.HEAD)
                        .orElseThrow(IllegalStateException::new), left, up);
                itemRenderer.renderItemIntoGUI(ARMOR_ICONS.getArmor(IHasMultiModel.Part.BODY)
                        .orElseThrow(IllegalStateException::new), left, down);
                itemRenderer.renderItemIntoGUI(ARMOR_ICONS.getArmor(IHasMultiModel.Part.LEGS)
                        .orElseThrow(IllegalStateException::new), right, up);
                itemRenderer.renderItemIntoGUI(ARMOR_ICONS.getArmor(IHasMultiModel.Part.FEET)
                        .orElseThrow(IllegalStateException::new), right, down);
            }

            for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                int selectArmor = armors.getArmor(part).orElse(-1);
                if (selectArmor != -1 && 0 <= selectArmor - scrollLine && selectArmor - scrollLine < 3) {
                    int left = (screenInfo.getWidth() + width) / 2 - 32;
                    int right = left + 16;
                    int up = (screenInfo.getHeight() - height) / 2 + (selectArmor - scrollLine) * 45;
                    int down = up + 16;
                    int x;
                    int y;
                    if (part == IHasMultiModel.Part.HEAD || part == IHasMultiModel.Part.BODY) {
                        x = left;
                    } else {
                        x = right;
                    }
                    if (part == IHasMultiModel.Part.HEAD || part == IHasMultiModel.Part.LEGS) {
                        y = up;
                    } else {
                        y = down;
                    }
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    itemRenderer.renderItemIntoGUI(ARMOR_ICONS.getArmor(part)
                            .orElseThrow(IllegalStateException::new), x, y);
                }
            }

        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            int line = (int) ((y - (screenInfo.getHeight() - height) / 2F) / (height / 3F));
            int left = (screenInfo.getWidth() + width) / 2 - 32;
            int right = left + 16;
            int up = (screenInfo.getHeight() - height) / 2 + line * 45;
            int down = up + 16;
            if (left <= x && x < right + 16 && up <= y && y < down + 16) {
                boolean isLeft = x < right;
                boolean isUp = y < down;
                if (isLeft && isUp) {
                    if (armors.getArmor(IHasMultiModel.Part.HEAD).orElse(-1) == line + scrollLine) {
                        armors.setArmor(-1, IHasMultiModel.Part.HEAD);
                    } else {
                        armors.setArmor(line + scrollLine, IHasMultiModel.Part.HEAD);
                    }
                } else if (isLeft) {
                    if (armors.getArmor(IHasMultiModel.Part.BODY).orElse(-1) == line + scrollLine) {
                        armors.setArmor(-1, IHasMultiModel.Part.BODY);
                    } else {
                        armors.setArmor(line + scrollLine, IHasMultiModel.Part.BODY);
                    }
                } else if (isUp) {
                    if (armors.getArmor(IHasMultiModel.Part.LEGS).orElse(-1) == line + scrollLine) {
                        armors.setArmor(-1, IHasMultiModel.Part.LEGS);
                    } else {
                        armors.setArmor(line + scrollLine, IHasMultiModel.Part.LEGS);
                    }
                } else {
                    if (armors.getArmor(IHasMultiModel.Part.FEET).orElse(-1) == line + scrollLine) {
                        armors.setArmor(-1, IHasMultiModel.Part.FEET);
                    } else {
                        armors.setArmor(line + scrollLine, IHasMultiModel.Part.FEET);
                    }
                }
                playDownSound();
                return true;
            }
            return clickModel(x, y, button) || scrollBar.mouseClicked(x, y, button);
        }

        protected boolean clickModel(double x, double y, int button) {
            if (button != 0 && button != 1) {
                return false;
            }
            int minX = (screenInfo.getWidth() - width) / 2;
            int maxX = (screenInfo.getWidth() + width) / 2;
            int minY = (screenInfo.getHeight() - height) / 2;
            int maxY = (screenInfo.getHeight() + height) / 2;
            if (!(minX <= x && x < maxX && minY <= y && y < maxY)) {
                return false;
            }
            int selectLine = (int) ((y - minY) / (height / 3));
            if (loadedTexture.size() <= scrollLine + selectLine) {
                return false;
            }
            TextureHolder textureHolder = loadedTexture.get(scrollLine + selectLine);
            if (!textureHolder.hasArmorTexture()) {
                return false;
            }
            if (button == 1 || this.selectLine == scrollLine + selectLine) {
                this.selectLine = -1;
            } else {
                this.selectLine = scrollLine + selectLine;
            }
            playDownSound();
            return true;
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double d, double d2) {
            return scrollBar.mouseDragged(x, y, button, d, d2);
        }

        @Override
        public void onClose() {
            if (0 <= selectLine && selectLine <= loadedTexture.size()) {
                for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                    TextureHolder armorTexture = loadedTexture.get(selectLine);
                    owner.setTextureHolder(armorTexture, IHasMultiModel.Layer.INNER, part);
                }
            }
            for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                int selectArmor = armors.getArmor(part).orElse(-1);
                if (selectArmor != -1) {
                    TextureHolder armorTexture = loadedTexture.get(selectArmor);
                    owner.setTextureHolder(armorTexture, IHasMultiModel.Layer.INNER, part);
                }
            }
        }

        @Override
        public void tick() {
            prevAnimation = animation;
            animation = MathHelper.clamp(animation + (animating ? 1 : -1), 0, maxAnimation);
        }

        public void renderAllArmorModel(MatrixStack matrixStack, int posX, int posY, int scale, float mouseX, float mouseY,
                                        ModelMultiBase model, TextureHolder holder) {
            int index = 0;
            ModelManager modelManager = LittleMaidModelLoader.getInstance().getModelManager();
            ModelMultiBase innerModel = modelManager.getModel(holder.getModelName(), IHasMultiModel.Layer.INNER)
                    .orElseThrow(() -> new IllegalStateException("モデルが存在しません"));
            ModelMultiBase outerModel = modelManager.getModel(holder.getModelName(), IHasMultiModel.Layer.OUTER)
                    .orElseThrow(() -> new IllegalStateException("モデルが存在しません"));
            List<String> armorNames = holder.getArmorNames().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            for (String armorName : armorNames) {
                index++;
                ResourceLocation innerTex = holder.getArmorTexture(IHasMultiModel.Layer.INNER, armorName,
                        0, false).orElse(null);
                ResourceLocation innerLightTex = holder.getArmorTexture(IHasMultiModel.Layer.INNER, armorName,
                        0, true).orElse(null);
                ResourceLocation outerTex = holder.getArmorTexture(IHasMultiModel.Layer.OUTER, armorName,
                        0, false).orElse(null);
                ResourceLocation outerLightTex = holder.getArmorTexture(IHasMultiModel.Layer.OUTER, armorName,
                        0, true).orElse(null);
                ArmorPartModelData armorData =
                        new ArmorPartModelData(innerTex, innerLightTex, outerTex, outerLightTex,
                                innerModel, outerModel);
                renderArmorPart(posX + index * scale - scale / 2, posY, scale, mouseX, mouseY,
                        model, armorData);
            }
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            fontRenderer.drawString(matrixStack, holder.getTextureName(),
                    posX, posY - 45, 0xFFFFFFFF);
        }

        public void renderArmorPart(int posX, int posY, int scale, float mouseX, float mouseY,
                                    ModelMultiBase model, ArmorPartModelData data) {
            dummy.setSkinModel(model);
            dummy.setSkinTexture(ModelSelectScreen.EMPTY_TEXTURE_PAIR);
            for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
                dummy.setArmorVisible(true, part);
                dummy.setArmorData(data, part);
            }
            InventoryScreen.drawEntityOnScreen(posX, posY, scale, mouseX, mouseY, dummy);
        }
    }

    public static class ScrollBar implements GUI {
        private final int width;
        private final int height;
        private final int x;
        private final int y;
        private final ScreenInfo screenInfo;
        private final Percent scrollPercent;

        public ScrollBar(int width, int height, int x, int y, ScreenInfo screenInfo, Percent scrollPercent) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
            this.screenInfo = screenInfo;
            this.scrollPercent = scrollPercent;
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            {
                int minX = screenInfo.getWidth() / 2 + x;
                int maxX = minX + width;
                int minY = screenInfo.getHeight() / 2 + y;
                int maxY = minY + height;
                renderColor(matrixStack, minX, minY, maxX, maxY,  0xFF000000);
            }
            {
                int minX = screenInfo.getWidth() / 2 + x;
                int maxX = minX + width;
                int minY = (int) (screenInfo.getHeight() / 2F + y + ((scrollPercent.getPercent()) * (height - width)));
                int maxY = minY + width;
                renderColor(matrixStack, minX, minY, maxX, maxY, 0xFFFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            int minX = screenInfo.getWidth() / 2 + this.x;
            int maxX = minX + width;
            int minY = screenInfo.getHeight() / 2 + this.y;
            int maxY = minY + height;
            if (button == 0 && minX <= x && x < maxX && minY <= y && y <= maxY) {
                //範囲の上限下限をwidth/2ほど狭め、範囲の下限を0に合わせ、それをパーセントに直す
                float percent = (float) (MathHelper.clamp(y - minY, width / 2F, height - width / 2F)
                        - width / 2F) / (height - width);
                scrollPercent.setPercent(percent);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double d, double d2) {
            return mouseClicked(x, y, button);
        }

        @Override
        public void onClose() {

        }

        @Override
        public void tick() {

        }
    }

    public static class Percent {
        private final Supplier<Float> getPercent;
        private final Consumer<Float> setPercent;

        public Percent(Supplier<Float> getPercent, Consumer<Float> setPercent) {
            this.getPercent = getPercent;
            this.setPercent = setPercent;
        }

        float getPercent() {
            return getPercent.get();
        }

        void setPercent(float percent) {
            setPercent.accept(percent);
        }
    }

    public static class ScreenInfo {
        private final Supplier<Integer> width;
        private final Supplier<Integer> height;
        private final Consumer<Integer> setScroll;
        private final Supplier<Integer> getScroll;

        public ScreenInfo(Supplier<Integer> width, Supplier<Integer> height,
                          Consumer<Integer> setScroll, Supplier<Integer> getScroll) {
            this.width = width;
            this.height = height;
            this.setScroll = setScroll;
            this.getScroll = getScroll;
        }

        public int getWidth() {
            return width.get();
        }

        public int getHeight() {
            return height.get();
        }

        public void setScroll(int scroll) {
            setScroll.accept(scroll);
        }

        public int getScroll() {
            return getScroll.get();
        }
    }

    public interface GUI {
        void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

        boolean mouseClicked(double x, double y, int button);

        boolean mouseDragged(double x, double y, int button, double d, double d2);

        void onClose();

        void tick();
    }

    public static class DummyModelEntity extends LivingEntity implements IHasMultiModel {
        private final EntityCaps caps = new EntityCaps(this);
        private ModelMultiBase skinModel;
        private TexturePair skinTexture;
        private final ArmorsHolder<ArmorPartModelData> armorsData = new ArmorsHolder<>();
        private final ArmorsHolder<Boolean> armorsVisible = new ArmorsHolder<>();

        public DummyModelEntity(EntityType<DummyModelEntity> type, World worldIn) {
            super(type, worldIn);
        }

        public DummyModelEntity(World worldIn) {
            super(Registration.DUMMY_MODEL_ENTITY.get(), worldIn);
        }

        public void setSkinModel(ModelMultiBase model) {
            skinModel = model;
        }

        public void setSkinTexture(TexturePair skinTexture) {
            this.skinTexture = skinTexture;
        }

        public void setArmorData(ArmorPartModelData data,  Part part) {
            armorsData.setArmor(data, part);
        }

        public void setArmorVisible(boolean visible, Part part) {
            this.armorsVisible.setArmor(visible, part);
        }

        public void setAllArmorVisible(boolean visible) {
            for (Part part : Part.values()) {
                this.armorsVisible.setArmor(visible, part);
            }
        }


        @Nonnull
        @Override
        public Iterable<ItemStack> getArmorInventoryList() {
            return Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        }


        @Nonnull
        @Override
        public ItemStack getItemStackFromSlot(@Nonnull EquipmentSlotType slotIn) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItemStackToSlot(@Nonnull EquipmentSlotType slotIn, @Nonnull ItemStack stack) {

        }

        @Nonnull
        @Override
        public HandSide getPrimaryHand() {
            return HandSide.RIGHT;
        }

        @Deprecated
        @Override
        public void setTextureHolder(TextureHolder textureHolder,  Layer layer,  Part part) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public TextureHolder getTextureHolder( Layer layer,  Part part) {
            return null;
        }

        @Deprecated
        @Override
        public void setColor(TextureColor color) {

        }

        @Deprecated
        @Override
        public TextureColor getColor() {
            return null;
        }

        @Deprecated
        @Override
        public void setContract(boolean isContract) {

        }

        @Deprecated
        @Override
        public boolean isContract() {
            return false;
        }

        @Override
        public Optional<ModelMultiBase> getModel( Layer layer,  Part part) {
            if (layer == Layer.SKIN) {
                return Optional.ofNullable(skinModel);
            } else {
                ModelMultiBase model = armorsData.getArmor(part)
                        .orElseThrow(() -> new IllegalStateException("防具データが存在しません"))
                        .getModel(layer);
                return Optional.ofNullable(model);
            }
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public Optional<ResourceLocation> getTexture( Layer layer,  Part part, boolean isLight) {
            if (layer == Layer.SKIN) {
                return Optional.ofNullable(skinTexture.getTexture(isLight));
            } else {
                ResourceLocation resourceLocation = armorsData.getArmor(part)
                        .orElseThrow(() -> new IllegalStateException("防具データが存在しません"))
                        .getTexture(layer, isLight);
                return Optional.ofNullable(resourceLocation);
            }
        }

        @Nonnull
        @Override
        public IModelCaps getCaps() {
            return caps;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public boolean isArmorVisible(Part part) {
            return armorsVisible.getArmor(part).orElse(false);
        }

        @Override
        public boolean isArmorGlint(Part part) {
            return false;
        }

        @Deprecated
        @Override
        public boolean isAllowChangeTexture(@Nullable Entity changer, TextureHolder textureHolder,
                                             Layer layer,  Part part) {
            return true;
        }
    }


}
