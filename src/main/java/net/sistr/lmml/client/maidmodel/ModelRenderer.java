package net.sistr.lmml.client.maidmodel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;

import java.lang.reflect.Constructor;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

//描画周りに大幅な仕様変更がされたため、互換性の喪失を伴う変更を加えた
//できる限りメソッドと変数を維持しているが、仕組み上どうしても組み込めないものは処理を削除している
public class ModelRenderer {

    // ModelRenderer互換変数群
    public float textureWidth;
    public float textureHeight;
    private int textureOffsetX;
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean mirror;
    public boolean showModel;
    public List<ModelBoxBase> cubeList = new ArrayList<>();
    public List<ModelRenderer> childModels = new ArrayList<>();

    //互換性のための変数群
    public static MatrixStack matrixStack;
    public static IVertexBuilder buffer;
    public static int packedLight;
    public static int packedOverlay;
    public static float red;
    public static float green;
    public static float blue;
    public static float alpha;

    //もはや不使用な変数群
    @Deprecated
    protected boolean compiled;
    @Deprecated
    protected int displayList;
    @Deprecated
    public boolean isHidden;
    @Deprecated
    public boolean isRendering;
    @Deprecated
    public final String boxName;
    @Deprecated
    protected ModelBase baseModel;
    @Deprecated
    public ModelRenderer pearent;
    @Deprecated
    public float offsetX;
    @Deprecated
    public float offsetY;
    @Deprecated
    public float offsetZ;
    @Deprecated
    public float scaleX;
    @Deprecated
    public float scaleY;
    @Deprecated
    public float scaleZ;


    //	public static final float radFactor = 57.295779513082320876798154814105F;
    public static final float radFactor = 180F / (float) Math.PI;
    //	public static final float degFactor = 0.01745329251994329576923690768489F;
    public static final float degFactor = (float) Math.PI / 180F;

    // SmartMovingに合わせるために名称の変更があるかもしれません。
    @Deprecated
    public int rotatePriority;
    @Deprecated
    public static final int RotXYZ = 0;
    @Deprecated
    public static final int RotXZY = 1;
    @Deprecated
    public static final int RotYXZ = 2;
    @Deprecated
    public static final int RotYZX = 3;
    @Deprecated
    public static final int RotZXY = 4;
    @Deprecated
    public static final int RotZYX = 5;

    //	public static final int ModeEquip = 0x000;
//	public static final int ModeInventory = 0x001;
//	public static final int ModeItemStack = 0x002;
//	public static final int ModeParts = 0x010;
    @Deprecated
    protected ItemStack itemstack;

    @Deprecated
    public boolean adjust;
    @Deprecated
    public FloatBuffer matrix;
    @Deprecated
    public boolean isInvertX;

    //private Render renderBlocksIr = new RenderBlocks();


    public ModelRenderer(ModelBase pModelBase, String pName) {
        textureWidth = 64.0F;
        textureHeight = 32.0F;
        mirror = false;
        showModel = true;
        isHidden = false;
        isRendering = true;
        baseModel = pModelBase;
        pModelBase.boxList.add(this);
        boxName = pName;
        setTextureSize(pModelBase.textureWidth, pModelBase.textureHeight);

        rotatePriority = RotXYZ;
        itemstack = ItemStack.EMPTY;
        adjust = true;
        matrix = BufferUtils.createFloatBuffer(16);
        isInvertX = false;

        scaleX = 1.0F;
        scaleY = 1.0F;
        scaleZ = 1.0F;

        pearent = null;

    }

    public ModelRenderer(ModelBase pModelBase, int px, int py) {
        this(pModelBase, null);
        setTextureOffset(px, py);
    }

    public ModelRenderer(ModelBase pModelBase) {
        this(pModelBase, null);
    }

    public ModelRenderer(ModelBase pModelBase, int px, int py, float pScaleX, float pScaleY, float pScaleZ) {
        this(pModelBase, px, py);
        this.scaleX = pScaleX;
        this.scaleY = pScaleY;
        this.scaleZ = pScaleZ;
    }

    public ModelRenderer(ModelBase pModelBase, float pScaleX, float pScaleY, float pScaleZ) {
        this(pModelBase);
        this.scaleX = pScaleX;
        this.scaleY = pScaleY;
        this.scaleZ = pScaleZ;
    }

    // ModelRenderer互換関数群

    public void addChild(ModelRenderer pModelRenderer) {
        if (childModels == null) {
            childModels = new ArrayList<>();
        }
        childModels.add(pModelRenderer);
        pModelRenderer.pearent = this;
    }

    public ModelRenderer setTextureOffset(int pOffsetX, int pOffsetY) {
        textureOffsetX = pOffsetX;
        textureOffsetY = pOffsetY;
        return this;
    }

    public ModelRenderer addBox(String pName, float pX, float pY, float pZ,
                                int pWidth, int pHeight, int pDepth) {
        addParts(ModelBox.class, pName, pX, pY, pZ, pWidth, pHeight, pDepth, 0.0F);
        return this;
    }

    public ModelRenderer addBox(float pX, float pY, float pZ,
                                int pWidth, int pHeight, int pDepth) {
        addParts(ModelBox.class, pX, pY, pZ, pWidth, pHeight, pDepth, 0.0F);
        return this;
    }

    public ModelRenderer addBox(float pX, float pY, float pZ,
                                int pWidth, int pHeight, int pDepth, float pSizeAdjust) {
        addParts(ModelBox.class, pX, pY, pZ, pWidth, pHeight, pDepth, pSizeAdjust);
        return this;
    }

    public ModelRenderer setRotationPoint(float pX, float pY, float pZ) {
        rotationPointX = pX;
        rotationPointY = pY;
        rotationPointZ = pZ;
        return this;
    }

    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
        this.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (this.showModel) {
            if (!this.cubeList.isEmpty() || !this.childModels.isEmpty()) {
                matrixStackIn.push();
                matrixStackIn.translate(this.rotationPointX / 16.0F, this.rotationPointY / 16.0F, this.rotationPointZ / 16.0F);
                setRotation(matrixStackIn);
                this.doRender(matrixStackIn.getLast(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

                for (ModelRenderer modelrenderer : this.childModels) {
                    modelrenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                }

                matrixStackIn.pop();
            }
        }
    }

    private void doRender(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrixEntryIn.getMatrix();
        Matrix3f matrix3f = matrixEntryIn.getNormal();

        for (ModelBoxBase modelBoxBase : this.cubeList) {
            for (ModelBoxBase.TexturedQuad quad : modelBoxBase.quadList) {
                //本来であればnormalはコンストラクタで必ず指定されるため、コピーだけで済むが、互換性のためにnormalを@Nullableにしているため
                if (quad.normal == null) {
                    Vector3f n1 = quad.vertexPositions[0].position.copy();
                    Vector3f n2 = quad.vertexPositions[2].position.copy();
                    n1.sub(quad.vertexPositions[1].position);
                    n2.sub(quad.vertexPositions[1].position);
                    n2.cross(n1);
                    n2.normalize();
                    quad.normal = n2;
                }
                Vector3f normal = quad.normal.copy();
                normal.transform(matrix3f);
                float f = normal.getX();
                float f1 = normal.getY();
                float f2 = normal.getZ();

                for (int i = 0; i < 4; ++i) {
                    ModelBoxBase.PositionTextureVertex vertex = quad.vertexPositions[i];
                    float f3 = vertex.position.getX() / 16.0F;
                    float f4 = vertex.position.getY() / 16.0F;
                    float f5 = vertex.position.getZ() / 16.0F;
                    Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
                    vector4f.transform(matrix4f);
                    bufferIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, vertex.textureU, vertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
                }
            }
        }

    }

    public void render(float par1, boolean pIsRender) {
        this.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void render(float par1) {
        this.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Deprecated
    public void renderWithRotation(float par1) {
    }

    @Deprecated
    public void postRender(float par1) {
    }

    @Deprecated
    protected void compileDisplayList(float par1) {
    }

    public ModelRenderer setTextureSize(int pWidth, int pHeight) {
        textureWidth = pWidth;
        textureHeight = pHeight;
        return this;
    }

    // 独自追加分

    /**
     * ModelBox継承の独自オブジェクト追加用
     */
    public ModelRenderer addCubeList(ModelBoxBase pModelBoxBase) {
        cubeList.add(pModelBoxBase);
        return this;
    }

    protected ModelBoxBase getModelBoxBase(Class<? extends ModelBoxBase> pModelBoxBase, Object... pArg) {
        try {
            Constructor<? extends ModelBoxBase> lconstructor =
                    pModelBoxBase.getConstructor(ModelRenderer.class, Object[].class);
            return lconstructor.newInstance(this, pArg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Object[] getArg(Object... pArg) {
        Object[] object = new Object[pArg.length + 2];
        object[0] = textureOffsetX;
        object[1] = textureOffsetY;
        System.arraycopy(pArg, 0, object, 2, pArg.length);
        return object;
    }

    public ModelRenderer addParts(Class<? extends ModelBoxBase> pModelBoxBase, String pName, Object... pArg) {
        //pName = boxName + "." + pName;
        //TextureOffset ltextureoffset = baseModel.getTextureOffset(pName);
        //setTextureOffset(ltextureoffset.textureOffsetX, ltextureoffset.textureOffsetY);
        addCubeList(getModelBoxBase(pModelBoxBase, getArg(pArg)));//.setBoxName(pName));
        return this;
    }

    public ModelRenderer addParts(Class<? extends ModelBoxBase> pModelBoxBase, Object... pArg) {
        addCubeList(getModelBoxBase(pModelBoxBase, getArg(pArg)));
        return this;
    }

    /**
     * 自分でテクスチャの座標を指定する時に使います。
     * コンストラクタへそのまま値を渡します。
     */
    public ModelRenderer addPartsTexture(Class<? extends ModelBoxBase> pModelBoxBase, String pName, Object... pArg) {
        addCubeList(getModelBoxBase(pModelBoxBase, pArg));
        return this;
    }

    /**
     * 自分でテクスチャの座標を指定する時に使います。
     * コンストラクタへそのまま値を渡します。
     */
    public ModelRenderer addPartsTexture(Class<? extends ModelBoxBase> pModelBoxBase, Object... pArg) {
        addCubeList(getModelBoxBase(pModelBoxBase, pArg));
        return this;
    }


    public ModelRenderer addPlate(float pX, float pY, float pZ,
                                  int pWidth, int pHeight, int pFacePlane) {
        addParts(ModelPlate.class, pX, pY, pZ, pWidth, pHeight, pFacePlane, 0.0F);
        return this;
    }

    public ModelRenderer addPlate(float pX, float pY, float pZ,
                                  int pWidth, int pHeight, int pFacePlane, float pSizeAdjust) {
        addParts(ModelPlate.class, pX, pY, pZ, pWidth, pHeight, pFacePlane, pSizeAdjust);
        return this;
    }

    public ModelRenderer addPlate(String pName, float pX, float pY, float pZ,
                                  int pWidth, int pHeight, int pFacePlane) {
        addParts(ModelPlate.class, pName, pX, pY, pZ, pWidth, pHeight, pFacePlane, 0.0F);
        return this;
    }

    /**
     * 描画用のボックス、子供をクリアする
     */
    public void clearCubeList() {
        cubeList.clear();
        if (childModels != null) {
            childModels.clear();
        }
    }

    @Deprecated
    public boolean renderItems(ModelMultiBase pModelMulti, IModelCaps pEntityCaps, boolean pRealBlock, int pIndex) {
        return false;
    }

    @Deprecated
    public void renderItemsHead(ModelMultiBase pModelMulti, IModelCaps pEntityCaps) {
    }

    /**
     * 回転変換を行う順序を指定。
     *
     * @param pValue Rot???を指定する
     */
    public void setRotatePriority(int pValue) {
        rotatePriority = pValue;
    }

    /**
     * 内部実行用、座標変換部
     */
    protected void setRotation(MatrixStack matrixStackIn) {
        // 変換順位の設定
        switch (rotatePriority) {
            case RotXYZ:
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                break;
            case RotXZY:
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                break;
            case RotYXZ:
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                break;
            case RotYZX:
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                break;
            case RotZXY:
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                break;
            case RotZYX:
                if (this.rotateAngleX != 0.0F) {
                    matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
                }
                if (this.rotateAngleY != 0.0F) {
                    matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
                }
                if (this.rotateAngleZ != 0.0F) {
                    matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
                }
                break;
        }
    }

    /**
     * 内部実行用、レンダリング部分。
     */
    @Deprecated
    protected void renderObject(float par1, boolean pRendering) {

    }

    /**
     * パーツ描画時点のマトリクスを設定する。 これ以前に設定されたマトリクスは破棄される。
     */
    @Deprecated
    public ModelRenderer loadMatrix() {
        return this;
    }


    // ゲッター、セッター

    public boolean getMirror() {
        return mirror;
    }

    public ModelRenderer setMirror(boolean flag) {
        mirror = flag;
        return this;
    }

    public boolean getVisible() {
        return showModel;
    }

    public void setVisible(boolean flag) {
        showModel = flag;
    }


    // Deg付きは角度指定が度数法

    public float getRotateAngleX() {
        return rotateAngleX;
    }

    public float getRotateAngleDegX() {
        return rotateAngleX * radFactor;
    }

    public float setRotateAngleX(float value) {
        return rotateAngleX = value;
    }

    public float setRotateAngleDegX(float value) {
        return rotateAngleX = value * degFactor;
    }

    public float addRotateAngleX(float value) {
        return rotateAngleX += value;
    }

    public float addRotateAngleDegX(float value) {
        return rotateAngleX += value * degFactor;
    }

    public float getRotateAngleY() {
        return rotateAngleY;
    }

    public float getRotateAngleDegY() {
        return rotateAngleY * radFactor;
    }

    public float setRotateAngleY(float value) {
        return rotateAngleY = value;
    }

    public float setRotateAngleDegY(float value) {
        return rotateAngleY = value * degFactor;
    }

    public float addRotateAngleY(float value) {
        return rotateAngleY += value;
    }

    public float addRotateAngleDegY(float value) {
        return rotateAngleY += value * degFactor;
    }

    public float getRotateAngleZ() {
        return rotateAngleZ;
    }

    public float getRotateAngleDegZ() {
        return rotateAngleZ * radFactor;
    }

    public float setRotateAngleZ(float value) {
        return rotateAngleZ = value;
    }

    public float setRotateAngleDegZ(float value) {
        return rotateAngleZ = value * degFactor;
    }

    public float addRotateAngleZ(float value) {
        return rotateAngleZ += value;
    }

    public float addRotateAngleDegZ(float value) {
        return rotateAngleZ += value * degFactor;
    }

    public ModelRenderer setRotateAngle(float x, float y, float z) {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;
        return this;
    }

    public ModelRenderer setRotateAngleDeg(float x, float y, float z) {
        rotateAngleX = x * degFactor;
        rotateAngleY = y * degFactor;
        rotateAngleZ = z * degFactor;
        return this;
    }

    public float getRotationPointX() {
        return rotationPointX;
    }

    public float setRotationPointX(float value) {
        return rotationPointX = value;
    }

    public float addRotationPointX(float value) {
        return rotationPointX += value;
    }

    public float getRotationPointY() {
        return rotationPointY;
    }

    public float setRotationPointY(float value) {
        return rotationPointY = value;
    }

    public float addRotationPointY(float value) {
        return rotationPointY += value;
    }

    public float getRotationPointZ() {
        return rotationPointZ;
    }

    public float setRotationPointZ(float value) {
        return rotationPointZ = value;
    }

    public float addRotationPointZ(float value) {
        return rotationPointZ += value;
    }

    @Deprecated
    public ModelRenderer setScale(float pX, float pY, float pZ) {
        scaleX = pX;
        scaleY = pY;
        scaleZ = pZ;
        return this;
    }

    @Deprecated
    public float setScaleX(float pValue) {
        return scaleX = pValue;
    }

    @Deprecated
    public float setScaleY(float pValue) {
        return scaleY = pValue;
    }

    @Deprecated
    public float setScaleZ(float pValue) {
        return scaleZ = pValue;
    }

}
