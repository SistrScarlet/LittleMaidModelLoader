package net.sistr.lmml.util.loader;

import com.google.common.collect.Lists;
import net.blacklab.lmr.entity.maidmodel.ModelMultiBase;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.config.LMRConfig;
import net.sistr.lmml.util.loader.resource.ResourceFileHelper;
import org.apache.commons.compress.utils.IOUtils;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * メイドさんのマルチモデルをロードする
 *
 * @author firis-games
 */
public class LMMultiModelHandler implements ILMFileLoaderHandler {

    public static final LMMultiModelHandler instance = new LMMultiModelHandler();
    private static final List<TransformedClassHolder> classHolder = Lists.newArrayList();
    private static final Transformer transformer = new Transformer();

    private LMMultiModelHandler() {
    }

    /**
     * マルチモデルクラス保管用
     */
    public static Map<String, Class<? extends ModelMultiBase>> multiModelClassMap = new HashMap<>();

    /**
     * マルチモデルのクラス名に含まれる文字列
     */
    private static final List<String> existsMultiModelNames = Arrays.asList("ModelMulti_", "ModelLittleMaid_", "Maid");

    /**
     * キャッシュフラグ
     */
    private boolean isCache = false;

    /**
     * キャッシュファイル名
     */
    private final String cacheFileName = "cache_multimodelpack.json";

    /**
     * Handlerの初期化処理
     * キャッシュ確認しキャッシュがあれば読込する
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init() {

        //キャッシュ機能の利用可否
        if (true /*!LMRConfig.cfg_loader_is_cache*/) return;

        //変換用キャッシュ
        Map<String, String> cachemultiModelClassMap;
        cachemultiModelClassMap = ResourceFileHelper.readFromJson(this.cacheFileName, Map.class);
        if (cachemultiModelClassMap == null) return;

        //内部設定へ変換する
        multiModelClassMap.clear();

        for (String className : cachemultiModelClassMap.keySet()) {
            try {
                //クラスをロード
                Class<?> modelClass;
                modelClass = Class.forName(cachemultiModelClassMap.get(className));

                //クラスを登録
                multiModelClassMap.put(className, (Class<? extends ModelMultiBase>) modelClass);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        if (multiModelClassMap.size() > 0) {
            this.isCache = true;
        }
    }

    /**
     * キャッシュがある場合は読み込み処理を行わない
     */
    @Override
    public boolean isFileLoad() {
        return !this.isCache;
    }

    /**
     * 対象ファイルがマルチモデルか判断する
     * <p>
     * ・拡張子が.class
     * 　・クラス名にexistsMultiModelNamesを含む
     */
    @Override
    public boolean isLoadHandler(String path, Path filePath) {
        //.class判定
        if (path != null && path.endsWith(".class")) {

            String className = path.substring(path.lastIndexOf("/") + 1);

            //クラスファイル名チェック
            return existsMultiModelNames.stream()
                    .anyMatch(className::contains);
        }
        return false;
    }

    /**
     * マルチモデルクラスをロード
     * <p>
     * 　ModelMultiBaseを継承しているかはこのタイミングでチェックする
     * 　クラス名から識別子を削除した名称をモデルIDとして登録する
     */
    @Override
    public void loadHandler(String path, Path filePath, InputStream inputstream) {

        //ClassLoader用パスへ変換
        String classpath = path.replace("/", ".");
        classpath = classpath.substring(0, path.lastIndexOf(".class"));

        //既に読み込まれているクラスはそのまま読み込む
        try {
            //クラスを取得
            Class<?> modelClass;
            modelClass = Class.forName(classpath);

            tryAddModel(classpath, modelClass);

        } catch (Exception e) {//読み込みに失敗 = 外部から読み込んだクラスは変換してストックする
            LittleMaidModelLoader.LOGGER.error(String.format("LMMultiModelHandler-Exception : %s", path));
            if (LMRConfig.cfg_PrintDebugMessage) e.printStackTrace();

            try {
                byte[] classFile = transformer.transform(classpath, path, IOUtils.toByteArray(inputstream));
                classHolder.add(new TransformedClassHolder(classpath, classFile));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * ファイル読込後の処理
     * キャッシュファイルを出力する
     */
    @Override
    public void postLoadHandler() {

        //地獄のクラス読み込み
        //読み込まれていないクラスを別のクラスが継承している場合に読み込まれないのを、10回読み込むことで全部読み込む
        //アホか
        //継承元クラスを抽出して読み込み順を組み立てた方がスマート
        //だけどめんどくさいしこれでも動くしせいぜい読み込み長くなる程度なので気にしない
        for (int i = 0; i < 10; i++) {
            Iterator<TransformedClassHolder> iterator = classHolder.iterator();
            while (iterator.hasNext()) {
                TransformedClassHolder classHolder = iterator.next();
                Class<?> modelClass;
                try {
                    modelClass = MultiModelClassLoader.loadFile(classHolder.name, classHolder.transformedClass);
                    boolean loaded = tryAddModel(classHolder.name, modelClass);
                    if (loaded) {
                        iterator.remove();
                    }
                } catch (Throwable t) {
                    if (LMRConfig.cfg_PrintDebugMessage) t.printStackTrace();
                }
            }
        }
        classHolder.clear();//用済み

        //キャッシュファイルを出力する
        if (LMRConfig.cfg_loader_is_cache) {

            //キャッシュファイル出力用に変換する
            Map<String, String> cachemultiModelClassMap = new HashMap<>();
            for (String key : multiModelClassMap.keySet()) {
                cachemultiModelClassMap.put(key, multiModelClassMap.get(key).getName());
            }

            //キャッシュ出力
            ResourceFileHelper.writeToJson(this.cacheFileName, cachemultiModelClassMap);
        }

    }

    //クラスがModelMultiBaseであるかチェックし、trueなら追加する
    @SuppressWarnings("unchecked")
    private static boolean tryAddModel(String classpath, Class<?> modelClass) {
        if (modelClass != null && ModelMultiBase.class.isAssignableFrom(modelClass)) {
            //モデルID
            String className = classpath.substring(classpath.lastIndexOf(".") + 1);
            //識別子削除
            for (String mmName : existsMultiModelNames) {
                int idx = className.indexOf(mmName);
                if (idx > -1) {
                    className = className.substring(idx + mmName.length());
                    break;
                }
            }
            //クラスを登録
            multiModelClassMap.put(className, (Class<? extends ModelMultiBase>) modelClass);
            return true;
        }
        return false;
    }

    //変換したクラスの保持クラス
    public static class TransformedClassHolder {
        public final String name;
        public final byte[] transformedClass;

        private TransformedClassHolder(String name, byte[] transformedClass) {
            this.name = name;
            this.transformedClass = transformedClass;
        }

    }
}
