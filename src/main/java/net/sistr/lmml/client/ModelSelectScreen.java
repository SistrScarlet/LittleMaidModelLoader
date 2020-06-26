package net.sistr.lmml.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelMultiBase;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.util.manager.ModelManager;

import java.util.List;

//todo 防具の発光モデル、染料消費、汎用化、保持染料ハイライト、スクロールバー、モデルリスト、選択モデル描画、現在モデルハイライト
@OnlyIn(Dist.CLIENT)
public class ModelSelectScreen extends Screen {
    protected static final int WIDTH = 256;
    protected static final int HEIGHT = 151;
    protected final LivingEntity owner;
    protected final IHasMultiModel hasMultiModel;
    //usableColorBitsには1bitごとに、その色が使用可能であるか入っている。0ならfalse、1ならtrue。一桁目がColor0で、最上段がColor15
    protected boolean isVisibleModelSelect;
    protected final Screen modelGUI;
    protected final Screen armorGUI;

    public ModelSelectScreen(ITextComponent titleIn, LivingEntity owner, IHasMultiModel hasMultiModel, int usableColorBits) {
        super(titleIn);
        this.owner = owner;
        this.hasMultiModel = hasMultiModel;
        this.isVisibleModelSelect = true;
        List<TextureBox> modelBoxes = Lists.newArrayList();
        List<TextureBox> armorBoxes = Lists.newArrayList();
        //TextureBoxをmodelとarmorで振り分ける。どちらにも入るボックスもある
        ModelManager.getTextureList().forEach(box -> {
            //契約色があるならば
            if (0 < box.getContractColorBits()) {
                modelBoxes.add(box);
            }
            //防具を持っていれば
            if (box.hasArmor()) {
                armorBoxes.add(box);
            }
        });
        ModelSelectorDummyEntity dummy = new ModelSelectorDummyEntity(owner.world, new SequentiallyMultiModelImpl());
        modelGUI = new MainModelSelectGUI(WIDTH, HEIGHT, 15, 3, titleIn, owner, hasMultiModel, usableColorBits, dummy, modelBoxes);
        armorGUI = new ArmorModelSelectGUI(WIDTH, HEIGHT, 15, 3, titleIn, owner, hasMultiModel, dummy, armorBoxes);
    }

    @Override
    public void func_231158_b_(Minecraft minecraft, int width, int height) {
        super.func_231158_b_(minecraft, width, height);
        modelGUI.func_231158_b_(minecraft, width, height);
        armorGUI.func_231158_b_(minecraft, width, height);
        this.func_230480_a_(new Button(field_230708_k_ / 2 - 40, field_230709_l_ - (field_230709_l_ - HEIGHT) / 2 + 10, 80, 20,
                new TranslationTextComponent("screen.littlemaidmodelloader.model_select_screen.change_screen"),
                button -> this.changeVisible()));
    }

    public void changeVisible() {
        this.isVisibleModelSelect = !this.isVisibleModelSelect;
    }

    @Override
    public void func_231152_a_(Minecraft minecraft, int width, int height) {
        super.func_231152_a_(minecraft, width, height);
        modelGUI.func_231152_a_(minecraft, width, height);
        armorGUI.func_231152_a_(minecraft, width, height);
    }

    @Override
    public void func_231175_as__() {
        modelGUI.func_231175_as__();
        armorGUI.func_231175_as__();
        super.func_231175_as__();
    }

    @Override
    public boolean func_231043_a_(double x, double y, double scrollAmount) {
        super.func_231043_a_(x, y, scrollAmount);
        if (isVisibleModelSelect) {
            return modelGUI.func_231043_a_(x, y, scrollAmount);
        } else {
            return armorGUI.func_231043_a_(x, y, scrollAmount);
        }
    }

    @Override
    public boolean func_231048_c_(double x, double y, int button) {
        super.func_231048_c_(x, y, button);
        if (isVisibleModelSelect) {
            return modelGUI.func_231048_c_(x, y, button);
        } else {
            return armorGUI.func_231048_c_(x, y, button);
        }
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        if (isVisibleModelSelect) {
            modelGUI.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        } else {
            armorGUI.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    public static class MainModelSelectGUI extends ModelSelectGUI {
        //usableColorBitsには1bitごとに、その色が使用可能であるか入っている。0ならfalse、1ならtrue。一桁目がColor0で、最上段がColor15
        protected final int usableColorBits;
        protected byte selectColor = -1;

        protected MainModelSelectGUI(int sizeWidth, int sizeHeight, int scale, int vertical, ITextComponent titleIn, LivingEntity owner, IHasMultiModel hasMultiModel, int usableColorBits, ModelSelectorDummyEntity dummy, List<TextureBox> modelBoxes) {
            super(sizeWidth, sizeHeight, scale, vertical, titleIn, owner, hasMultiModel, dummy, modelBoxes);
            this.usableColorBits = usableColorBits;
        }

        @Override
        public void func_231175_as__() {
            boolean shouldSync = false;
            if (0 <= selectModelNumber && selectModelNumber < modelBoxes.size()) {
                TextureBox mainBox = modelBoxes.get(selectModelNumber);
                if (mainBox != hasMultiModel.getTextureBox()[0]) {
                    hasMultiModel.setTextureBox(0, mainBox);
                    hasMultiModel.setTextureBox(1, mainBox);
                    shouldSync = true;
                }
            }
            if (0 <= selectColor && hasMultiModel.getColor() != selectColor && hasMultiModel.getTextureBox()[0].hasColor(selectColor)) {
                hasMultiModel.setColor(selectColor);
                shouldSync = true;
            }
            if (shouldSync) {
                hasMultiModel.updateTextures();
                hasMultiModel.sync();
            }
            super.func_231175_as__();
        }

        @Override
        public boolean func_231048_c_(double x, double y, int button) {
            int relX = (field_230708_k_ - this.sizeWidth) / 2;
            int relY = (field_230709_l_ - this.sizeHeight) / 2;
            //モデル選択
            if (relX + offsetX < x && x < relX + offsetX + scale * 16
                    && relY + offsetY < y && y < relY + offsetY + layerSize * layerPile) {
                //相対クリックX / 相対左端から相対右端までの長さ = 0~15
                byte color = (byte) (((int) x - (relX + offsetX)) / scale);
                int modelNumber = ((int) y - (relY + offsetY)) / layerSize - scroll;
                if (0 <= modelNumber && modelNumber < modelBoxes.size()) {
                    TextureBox box = modelBoxes.get(modelNumber);
                    if (((usableColorBits >> color) & 1) == 1 && box.hasColor(color)) {
                        this.selectColor = color;
                        this.selectModelNumber = modelNumber;
                    }
                }
            }
            return true;
        }

        @Override
        public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

            dummy.setCanRenderArmor(0, false);
            dummy.setCanRenderArmor(1, false);
            dummy.setCanRenderArmor(2, false);
            dummy.setCanRenderArmor(3, false);

            int relX = (field_230708_k_ - this.sizeWidth) / 2;
            int relY = (field_230709_l_ - this.sizeHeight) / 2;
            int posY = layerSize * scroll + offsetY;
            for (TextureBox textureBox : modelBoxes) {
                //上下いずれかの端が画面外である場合は描画しない
                if (offsetY <= posY && posY < layerSize * layerPile + offsetY) {
                    renderModelAllColor(relX + offsetX, relY + posY,
                            field_230708_k_ / 2F - mouseX, field_230709_l_ / 2F - mouseY,
                            dummy, dummy, textureBox, true);
                    field_230712_o_.func_238421_b_(matrixStack, textureBox.textureName, relX + offsetX, relY + posY, 0xFFFFFF);
                }
                posY += layerSize;
            }

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();

            if (0 <= selectModelNumber && -scroll <= selectModelNumber && selectModelNumber < -scroll + layerPile) {
                posY = (selectModelNumber + scroll) * layerSize + offsetY;
                drawColor(relX + offsetX, relY + posY,
                        relX + offsetX + scale * 16, relY + posY + layerSize,
                        0, 0xFFFFFF40);
            }
            if (0 <= selectColor) {
                drawColor(relX + offsetX + scale * selectColor, relY + offsetY,
                        relX + offsetX + scale * (selectColor + 1), relY - offsetY + this.sizeHeight,
                        0, 0xFF000040);
            }

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public void renderModelAllColor(int posX, int posY, float mouseX, float mouseY,
                                        LivingEntity entity, IHasMultiModel hasMultiModel, TextureBox box, boolean isContract) {
            hasMultiModel.setMultiModels(0, box.models[0]);
            for (int color = 0; color < 16; color++) {
                int mainColor = (color & 0x00ff) + (isContract ? 0 : ModelManager.tx_wild);
                int growColor = (color & 0x00ff) + (isContract ? ModelManager.tx_eyecontract : ModelManager.tx_eyewild);
                if (box.hasColor(mainColor)) {
                    ResourceLocation[] locations = new ResourceLocation[2];
                    locations[0] = box.getTextureName(mainColor);
                    locations[1] = box.getTextureName(growColor);
                    hasMultiModel.setTextures(0, locations);
                    //描画開始位置が左上に来るように調整
                    InventoryScreen.drawEntityOnScreen(posX + scale / 2, posY + layerSize,
                            scale, mouseX, mouseY, entity);
                }
                posX += scale;
            }
        }

    }

    public static class ArmorModelSelectGUI extends ModelSelectGUI {

        protected ArmorModelSelectGUI(int sizeWidth, int sizeHeight, int scale, int vertical, ITextComponent titleIn, LivingEntity owner, IHasMultiModel hasMultiModel, ModelSelectorDummyEntity dummy, List<TextureBox> modelBoxes) {
            super(sizeWidth, sizeHeight, scale, vertical, titleIn, owner, hasMultiModel, dummy, modelBoxes);
        }

        @Override
        public void func_231175_as__() {
            boolean shouldSync = false;
            if (0 <= selectModelNumber && selectModelNumber < modelBoxes.size()) {
                TextureBox armorBox = modelBoxes.get(selectModelNumber);
                if (armorBox != hasMultiModel.getTextureBox()[1]) {
                    hasMultiModel.setTextureBox(1, armorBox);
                    shouldSync = true;
                }
            }
            if (shouldSync) {
                hasMultiModel.updateTextures();
                hasMultiModel.sync();
            }
            super.func_231175_as__();
        }

        @Override
        public boolean func_231048_c_(double x, double y, int button) {
            int relX = (field_230708_k_ - this.sizeWidth) / 2;
            int relY = (field_230709_l_ - this.sizeHeight) / 2;
            //モデル選択
            if (relX + offsetX < x && x < relX + offsetX + scale * 16
                    && relY + offsetY < y && y < relY + offsetY + layerSize * layerPile) {
                int modelNumber = ((int) y - (relY + offsetY)) / layerSize - scroll;
                if (0 <= modelNumber && modelNumber < modelBoxes.size()) {
                    this.selectModelNumber = modelNumber;
                }
            }
            return true;
        }

        @Override
        public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

            dummy.setCanRenderArmor(0, true);
            dummy.setCanRenderArmor(1, true);
            dummy.setCanRenderArmor(2, true);
            dummy.setCanRenderArmor(3, true);

            int relX = (field_230708_k_ - this.sizeWidth) / 2;
            int relY = (field_230709_l_ - this.sizeHeight) / 2;
            int posY = layerSize * scroll + offsetY;
            for (TextureBox textureBox : modelBoxes) {
                //画面外は描画しない
                if (offsetY <= posY && posY < layerSize * layerPile + offsetY) {
                    renderArmorModelAllMaterial(relX + offsetX, relY + posY,
                            field_230708_k_ / 2F - mouseX, field_230709_l_ / 2F - mouseY,
                            dummy, dummy, textureBox);
                    field_230712_o_.func_238421_b_(matrixStack, textureBox.textureName, relX + offsetX, relY + posY, 0xFFFFFF);
                }
                posY += layerSize;
            }

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();

            if (0 <= selectModelNumber && -scroll <= selectModelNumber && selectModelNumber < -scroll + layerPile) {
                posY = (selectModelNumber + scroll) * layerSize + offsetY;
                drawColor(relX + offsetX, relY + posY, relX + offsetX + (scale * 16),
                        relY + posY + layerSize, 0, 0xFFFFFF40);
            }

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

        public void renderArmorModelAllMaterial(int posX, int posY, float mouseX, float mouseY,
                                                LivingEntity entity, IHasMultiModel hasMultiModel, TextureBox box) {
            hasMultiModel.setMultiModels(1, box.models[1]);
            hasMultiModel.setMultiModels(2, box.models[2]);
            for (int i = 0; i < 5; i++) {
                ResourceLocation[] mainModel = new ResourceLocation[4];
                mainModel[0] = mainModel[1] = mainModel[2] = mainModel[3] = ModelManager.NULL_TEXTURE;
                hasMultiModel.setTextures(0, mainModel);
                ResourceLocation[] innerArmor = new ResourceLocation[4];
                innerArmor[0] = innerArmor[1] = innerArmor[2] = innerArmor[3] =
                        box.getArmorTextureName(ModelManager.tx_armor1, ModelManager.armorFilenamePrefix[i], 0);
                hasMultiModel.setTextures(1, innerArmor);
                ResourceLocation[] outerArmor = new ResourceLocation[4];//頭~足
                outerArmor[0] = outerArmor[1] = outerArmor[2] = outerArmor[3] =
                        box.getArmorTextureName(ModelManager.tx_armor2, ModelManager.armorFilenamePrefix[i], 0);
                hasMultiModel.setTextures(2, outerArmor);

                //描画開始位置が左上に来るように調整
                InventoryScreen.drawEntityOnScreen(posX + scale / 2, posY + layerSize,
                        scale, mouseX, mouseY, entity);
                posX += scale;
            }
        }

    }

    public static class ModelSelectGUI extends Screen {
        protected static final ResourceLocation MODEL_SELECT_GUI_TEXTURE = new ResourceLocation(LittleMaidModelLoader.MODID, "textures/gui/model_select.png");
        protected final int sizeWidth;
        protected final int sizeHeight;
        protected final int scale;
        protected final int offsetX = 8;
        protected final int offsetY = 8;
        protected final int layerSize;
        protected final int layerPile;
        protected final LivingEntity owner;
        protected final IHasMultiModel hasMultiModel;
        protected final ModelSelectorDummyEntity dummy;
        protected final List<TextureBox> modelBoxes = Lists.newArrayList();
        protected int scroll;
        protected int selectModelNumber = -1;

        protected ModelSelectGUI(int sizeWidth, int sizeHeight, int scale, int layerPile, ITextComponent titleIn, LivingEntity owner, IHasMultiModel hasMultiModel, ModelSelectorDummyEntity dummy, List<TextureBox> modelBoxes) {
            super(titleIn);
            this.sizeWidth = sizeWidth;
            this.sizeHeight = sizeHeight;
            this.scale = scale;
            this.layerPile = layerPile;

            this.owner = owner;
            this.hasMultiModel = hasMultiModel;
            this.dummy = dummy;
            this.modelBoxes.addAll(modelBoxes);

            this.layerSize = scale * 3;
        }

        @Override
        public boolean func_231043_a_(double x, double y, double scrollAmount) {
            boolean scrollUp = 0 < scrollAmount;
            scroll = MathHelper.clamp(scroll + (scrollUp ? 1 : -1), 2 - modelBoxes.size(), 0);
            return true;
        }

        @Override
        public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_230706_i_.getTextureManager().bindTexture(MODEL_SELECT_GUI_TEXTURE);
            int relX = (field_230708_k_ - WIDTH) / 2;
            int relY = (field_230709_l_ - HEIGHT) / 2;
            this.func_238474_b_(matrixStack, relX, relY, 0, 0, WIDTH, HEIGHT);
        }

        protected static void drawColor(int x1, int y1, int x2, int y2, int z, int RGBA) {
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x1, y1, z).color(RGBA >> 24, (RGBA >> 16) & 0xFF, (RGBA >> 8) & 0xFF, RGBA & 0xFF).endVertex();
            bufferbuilder.pos(x2, y1, z).color(RGBA >> 24, (RGBA >> 16) & 0xFF, (RGBA >> 8) & 0xFF, RGBA & 0xFF).endVertex();
            bufferbuilder.pos(x2, y2, z).color(RGBA >> 24, (RGBA >> 16) & 0xFF, (RGBA >> 8) & 0xFF, RGBA & 0xFF).endVertex();
            bufferbuilder.pos(x1, y2, z).color(RGBA >> 24, (RGBA >> 16) & 0xFF, (RGBA >> 8) & 0xFF, RGBA & 0xFF).endVertex();
            bufferbuilder.finishDrawing();
            RenderSystem.enableAlphaTest();
            WorldVertexBufferUploader.draw(bufferbuilder);
        }

    }

    //下のエンティティとあえて分ける必要はそんなにない
    public static class SequentiallyMultiModelImpl implements IHasMultiModel {
        private final ResourceLocation[][] textures = new ResourceLocation[][]{
                //基本、発光
                {null, null},
                //アーマー内：頭、胴、腰、足
                {null, null, null, null},
                //アーマー外：頭、胴、腰、足
                {null, null, null, null},
                //アーマー内発光：頭、胴、腰、足
                {null, null, null, null},
                //アーマー外発光：頭、胴、腰、足
                {null, null, null, null}
        };
        private final ModelMultiBase[] models = new ModelMultiBase[]{
                //基本 防具内 防具外
                null, null, null
        };
        private byte color;
        private boolean contract;
        private final boolean[] canRenderArmors = new boolean[]{false, false, false, false};

        @Override
        public void sync() {

        }

        @Override
        public void updateTextures() {

        }

        @Override
        public void setTextures(int index, ResourceLocation[] names) {
            textures[index] = names;
        }

        @Override
        public ResourceLocation[] getTextures(int index) {
            return textures[index];
        }

        @Override
        public void setMultiModels(int index, ModelMultiBase models) {
            this.models[index] = models;
        }

        @Override
        public ModelMultiBase[] getMultiModels() {
            return models;
        }

        @Override
        public void setColor(byte color) {
            this.color = color;
        }

        @Override
        public byte getColor() {
            return color;
        }

        @Override
        public void setContract(boolean isContract) {
            contract = isContract;
        }

        @Override
        public boolean getContract() {
            return contract;
        }

        @Override
        public void setTextureBox(int index, TextureBox textureBox) {

        }

        @Override
        public TextureBox[] getTextureBox() {
            return null;
        }

        @Override
        public boolean canRenderArmor(int index) {
            return canRenderArmors[index];
        }

        @Override
        public void setCanRenderArmor(int index, boolean canRender) {
            canRenderArmors[index] = canRender;
        }
    }

    public static class ModelSelectorDummyEntity extends LivingEntity implements IHasMultiModel {
        private final IHasMultiModel data;

        public ModelSelectorDummyEntity(EntityType<ModelSelectorDummyEntity> type, World worldIn) {
            super(type, worldIn);
            this.data = null;
        }

        public ModelSelectorDummyEntity(World worldIn) {
            this(Registration.MODEL_SELECTOR_DUMMY_ENTITY.get(), worldIn);
        }

        public ModelSelectorDummyEntity(World worldIn, IHasMultiModel data) {
            super(Registration.MODEL_SELECTOR_DUMMY_ENTITY.get(), worldIn);
            this.data = data;
        }

        @Override
        public void sync() {
            data.sync();
        }

        @Override
        public void updateTextures() {
            data.updateTextures();
        }

        @Override
        public void setTextures(int index, ResourceLocation[] names) {
            data.setTextures(index, names);
        }

        @Override
        public ResourceLocation[] getTextures(int index) {
            return data.getTextures(index);
        }

        @Override
        public void setMultiModels(int index, ModelMultiBase models) {
            data.setMultiModels(index, models);
        }

        @Override
        public ModelMultiBase[] getMultiModels() {
            return data.getMultiModels();
        }

        @Override
        public void setColor(byte color) {
            data.setColor(color);
        }

        @Override
        public byte getColor() {
            return data.getColor();
        }

        @Override
        public void setContract(boolean isContract) {
            data.setContract(isContract);
        }

        @Override
        public boolean getContract() {
            return data.getContract();
        }

        @Override
        public void setTextureBox(int index, TextureBox textureBox) {
            data.setTextureBox(index, textureBox);
        }

        @Override
        public TextureBox[] getTextureBox() {
            return data.getTextureBox();
        }

        @Override
        public boolean canRenderArmor(int index) {
            return data.canRenderArmor(index);
        }

        @Override
        public void setCanRenderArmor(int index, boolean canRender) {
            data.setCanRenderArmor(index, canRender);
        }

        @Override
        public Iterable<ItemStack> getArmorInventoryList() {
            return Lists.newArrayList(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        }

        @Override
        public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {

        }

        @Override
        public HandSide getPrimaryHand() {
            return HandSide.RIGHT;
        }
    }


}
