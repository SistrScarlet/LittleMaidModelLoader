package net.sistr.lmml.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 古いマルチモデルのロード用。
 * 使用しているクラスを置換えて新しいものへ対応。
 */
public class MultiModelClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String newPackageString = "net/sistr/lmml/maidmodel/";

    private static final Map<String, String> replaceMap = new HashMap<String, String>() {
        {
            //継承関係で整理
            //また、未使用っぽいのはコメントアウト

            //継承多数
            addTransformTarget("IModelCaps");

            //継承無し
            addTransformTarget("ModelRenderer");
            addTransformTarget("ModelCapsHelper");
            //addTransformTarget("EquippedStabilizer");

            //ボックス
            addTransformTarget("ModelBoxBase");//名前の長さ的にここだけ順番逆にした
            addTransformTarget("ModelBox");
            addTransformTarget("ModelPlate");

            //モデル
            addTransformTarget("ModelLittleMaid_Aug");
            addTransformTarget("ModelLittleMaid_SR2");
            addTransformTarget("ModelLittleMaid_Orign");
            //addTransformTarget("ModelLittleMaid_RX0");//元コードだとRX2になってたけどこれ正しいのか？
            addTransformTarget("ModelLittleMaid_Archetype");
            addTransformTarget("ModelLittleMaidBase");
            //addTransformTarget("ModelLittleMaid_AC");
            addTransformTarget("ModelMultiMMMBase");
            addTransformTarget("ModelMultiBase");
            //addTransformTarget("ModelStabilizer_WitchHat");
            //addTransformTarget("ModelStabilizerBase");
            addTransformTarget("ModelBase");

            //Caps系
            //今のところCapsが一つしかないため移動先もまたひとつ
            put("mmmlibx/lib/MMM_EntityCaps", "net/sistr/lmml/maidmodel/EntityCaps");
            put("net/blacklab/lmr/util/EntityCapsLiving", "net/sistr/lmml/maidmodel/EntityCaps");
            put("littleMaidMobX/EntityCaps", "net/sistr/lmml/maidmodel/EntityCaps");
            put("net/blacklab/lmr/util/EntityCaps", "net/sistr/lmml/maidmodel/EntityCaps");

            //Beverly7
            put("net/blacklab/lmr/entity/EntityLittleMaid", "net/sistr/lmml/entity/EntityLittleMaid");
            put("net/minecraft/entity/EntityLivingBase", "net/minecraft/entity/LivingEntity");
            put("net/minecraft/entity/passive/EntityAnimal", "net/minecraft/entity/passive/AnimalEntity");
            put("net/minecraft/entity/player/EntityPlayer", "net/minecraft/entity/player/PlayerEntity");
            put("func_184187_bx", "getRidingEntity");

            //NM
            put("net/blacklab/lmr/entity/littlemaid/EntityLittleMaid", "net/sistr/lmml/entity/EntityLittleMaid");
        }

        private void addTransformTarget(String className) {
            put("MMM_" + className, newPackageString + className);
            put("mmmlibx/lib/multiModel/model/mc162/" + className, newPackageString + className);
            put("net/blacklab/lmr/entity/maidmodel/" + className, newPackageString + className);
        }
    };

    /**
     * バイナリを解析して古いクラスを置き換える。
     */
    public byte[] transform(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);

        boolean changed = false;

        // 親クラスの置き換え
        ClassNode cNode = new ClassNode();
        reader.accept(cNode, 0);
        String superName = replace(cNode.superName);
        if (superName != null) {
            changed = true;
            cNode.superName = superName;
        }

        // フィールドの置き換え
        for (FieldNode fNode : cNode.fields) {
            String desc = replace(fNode.desc);
            if (desc != null) {
                changed = true;
                fNode.desc = desc;
            }
        }

        // メソッドの置き換え
        for (MethodNode mNode : cNode.methods) {
            String desc = replace(mNode.desc);
            if (desc != null) {
                changed = true;
                mNode.desc = desc;
            }

            if (mNode.localVariables != null) {
                for (LocalVariableNode vNode : mNode.localVariables) {
                    if (vNode.desc != null) {
                        String vDesc = replace(vNode.desc);
                        if (vDesc != null) {
                            changed = true;
                            vNode.desc = vDesc;
                        }
                    }
                    if (vNode.name != null) {
                        String vName = replace(vNode.name);
                        if (vName != null) {
                            changed = true;
                            vNode.name = vName;
                        }
                    }
                    if (vNode.signature != null) {
                        String vSignature = replace(vNode.signature);
                        if (vSignature != null) {
                            changed = true;
                            vNode.signature = vSignature;
                        }
                    }
                }
            }

            AbstractInsnNode aNode = mNode.instructions.getFirst();
            while (aNode != null) {
                if (aNode instanceof FieldInsnNode) {//4
                    String aDesc = replace(((FieldInsnNode) aNode).desc);
                    if (aDesc != null) {
                        changed = true;
                        ((FieldInsnNode) aNode).desc = aDesc;
                    }
                    String aName = replace(((FieldInsnNode) aNode).name);
                    if (aName != null) {
                        changed = true;
                        ((FieldInsnNode) aNode).name = aName;
                    }
                    String aOwner = replace(((FieldInsnNode) aNode).owner);
                    if (aOwner != null) {
                        changed = true;
                        ((FieldInsnNode) aNode).owner = aOwner;
                    }
                } else if (aNode instanceof InvokeDynamicInsnNode) {//6
                    String aDesc = replace(((InvokeDynamicInsnNode) aNode).desc);
                    if (aDesc != null) {
                        changed = true;
                        ((InvokeDynamicInsnNode) aNode).desc = aDesc;
                    }
                    String aName = replace(((InvokeDynamicInsnNode) aNode).name);
                    if (aName != null) {
                        changed = true;
                        ((InvokeDynamicInsnNode) aNode).name = aName;
                    }
                } else if (aNode instanceof MethodInsnNode) {//5
                    String aDesc = replace(((MethodInsnNode) aNode).desc);
                    if (aDesc != null) {
                        changed = true;
                        ((MethodInsnNode) aNode).desc = aDesc;
                    }
                    String aName = replace(((MethodInsnNode) aNode).name);
                    if (aName != null) {
                        changed = true;
                        ((MethodInsnNode) aNode).name = aName;
                    }
                    String aOwner = replace(((MethodInsnNode) aNode).owner);
                    if (aOwner != null) {
                        changed = true;
                        ((MethodInsnNode) aNode).owner = aOwner;
                    }
                } else if (aNode instanceof MultiANewArrayInsnNode) {//13
                    String aDesc = replace(((MultiANewArrayInsnNode) aNode).desc);
                    if (aDesc != null) {
                        changed = true;
                        ((MultiANewArrayInsnNode) aNode).desc = aDesc;
                    }
                } else if (aNode instanceof TypeInsnNode) {//3
                    String aDesc = replace(((TypeInsnNode) aNode).desc);
                    if (aDesc != null) {
                        changed = true;
                        ((TypeInsnNode) aNode).desc = aDesc;
                    }
                }
                aNode = aNode.getNext();
            }
        }

        // バイナリコードの書き出し
        if (changed) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cNode.accept(writer);
            byte[] newClass = writer.toByteArray();
            //必要性が薄い
            //if (LMMLConfig.isDebugMode()) LOGGER.debug("Replaced : " + name);
            return newClass;
        }
        return basicClass;
    }

    /**
     * replaceMapに沿って置き換える
     */
    @Nullable
    private String replace(String text) {
        String newText = null;
        for (Entry<String, String> entry : replaceMap.entrySet()) {
            if ((text + ";").contains(entry.getKey() + ";")) {
                text = text.replace(entry.getKey(), entry.getValue());
                newText = text;
                //量がエグいので基本表示しないように
                //if (LMMLConfig.isDebugMode()) LOGGER.debug("Replacing : " + text + " to " + newText);

                //稀に1行に2つ変換対象がある場合があるため、ここでリターンはしない
                //return newText;
            }
        }
        return newText;
    }

}
