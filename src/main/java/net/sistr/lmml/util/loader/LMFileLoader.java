package net.sistr.lmml.util.loader;

import net.minecraftforge.fml.loading.FMLPaths;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.config.LMMLConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * メイドさんの外部読み込みファイルのLoader
 * ILMFileLoaderHandler実装クラスで読込対象のチェックと読込処理を行う
 *
 * @author firis-games
 */
public class LMFileLoader {

    public static LMFileLoader instance = new LMFileLoader();

    private final List<ILMFileLoaderHandler> loaderHandler = new ArrayList<>();

    /**
     * 初期化処理
     */
    private LMFileLoader() {

        //マルチモデルHandler
        loaderHandler.add(LMMultiModelHandler.instance);

        //テクスチャHandler
        loaderHandler.add(LMTextureHandler.instance);

        //サウンドHandler
        loaderHandler.add(LMSoundHandler.instance);

    }

    /**
     * ファイルロード処理
     * <p>
     * メイドさん関連ファイルを読み込む共通処理を行う
     */
    public void load() {

        LittleMaidModelLoader.LOGGER.info("LMFileLoader-load : start");

        //LoaderHandlerの初期化処理
        for (ILMFileLoaderHandler handler : loaderHandler) {
            handler.init();
        }

        //Loaderの動作チェック
        List<ILMFileLoaderHandler> procLoaderHandler = new ArrayList<>();
        for (ILMFileLoaderHandler handler : loaderHandler) {
            if (handler.isFileLoad()) {
                //処理対象のHandler登録
                procLoaderHandler.add(handler);
            }
        }

        //Loaderのファイル読込処理をスキップ
        if (procLoaderHandler.size() == 0) {
            LittleMaidModelLoader.LOGGER.info("LMFileLoader-load : cache-load end");
            return;
        }

        //ファイルパスリストを生成
        List<ModPath> filePathList;

        //ファイルのリスト作成
        try {
            filePathList = getLoadPath();
        } catch (Exception e) {
            LittleMaidModelLoader.LOGGER.error("LMFileLoader-Exception : getLoadPath");
            if (LMMLConfig.cfg_PrintDebugMessage) e.printStackTrace();
            return;
        }

        //パス走査
        for (ModPath loaderPath : filePathList) {
            try {
                //拡張子判定
                if (loaderPath.isExtension(".jar", ".zip")) {
                    //zip or jarファイル
                    ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(loaderPath.path));
                    ZipEntry zipEntry;
                    Path zipPath = loaderPath.path;
                    while ((zipEntry = zipStream.getNextEntry()) != null) {
                        String zPath = zipEntry.getName();
                        //handlerに処理を委譲
                        for (ILMFileLoaderHandler handler : procLoaderHandler) {
                            if (handler.isLoadHandler(zPath, zipPath)) {
                                handler.loadHandler(zPath, zipPath, zipStream);
                                break;
                            }
                        }
                    }
                } else {
                    //通常ファイル
                    String lPath = loaderPath.getLoaderPath();
                    //handlerに処理を委譲
                    for (ILMFileLoaderHandler handler : procLoaderHandler) {
                        if (handler.isLoadHandler(lPath, null)) {
                            handler.loadHandler(lPath, null, null);
                            break;
                        }
                    }

                }
            } catch (Exception e) {
                LittleMaidModelLoader.LOGGER.error(String.format("LMFileLoader-LoadException : %s", loaderPath.path.toString()));
                if (LMMLConfig.cfg_PrintDebugMessage) e.printStackTrace();
            }
        }

        //読込後処理を呼び出し
        for (ILMFileLoaderHandler handler : procLoaderHandler) {
            handler.postLoadHandler();
        }

        LittleMaidModelLoader.LOGGER.info("LMFileLoader-load : end");
    }

    /**
     * ファイル読込対象のパスリストを生成する
     * <p>
     * 通常：modsフォルダ配下
     * 開発：modsフォルダ配下・classloaderパス
     *
     * @return
     */
    private List<ModPath> getLoadPath() throws IOException {

        List<ModPath> pathList = new ArrayList<>();

        //開発環境専用パス
        if (true/*DevMode.DEVELOPMENT_DEBUG_MODE*/) {
            String classPath = System.getProperty("java.class.path");
            String separator = System.getProperty("path.separator");

            String[] classPathList = classPath.split(separator);

            //対象パスからファイルを取得する
            for (String path : classPathList) {
                //Pathリストを追加
                Path basePath = Paths.get(path);
                pathList.addAll(
                        Files.walk(basePath)
                                .filter(p -> !Files.isDirectory(p))
                                .map(p -> new ModPath(basePath, p))
                                .collect(Collectors.toList()));
            }
        }

        //通常パス
        Path mcHomePath = getMinecraftHomePath();
        //ひとまずはmodsフォルダの下のみ
        Path modsPath = Paths.get(mcHomePath.toString(), "mods");
        //Pathリストを追加
        pathList.addAll(
                Files.walk(modsPath)
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> new ModPath(modsPath, p))
                        .collect(Collectors.toList()));

        return pathList;
    }

    /**
     * MinecraftのHomeパスを取得する
     *
     * @return
     */
    private static Path getMinecraftHomePath() {
        return FMLPaths.GAMEDIR.get();
    }

    /**
     * FileLoader用パス管理クラス
     */
    private static class ModPath {

        private final Path basePath;
        private final Path path;

        public ModPath(Path basePath, Path path) {
            this.basePath = basePath;
            this.path = path;
        }

        /**
         * ClassLoaderアクセス用のパスに変換
         */
        public String getLoaderPath() {
            String loaderPath = this.path.toString().replace(this.basePath.toString(), "");
            loaderPath = loaderPath.replace("\\", "/");
            loaderPath = loaderPath.startsWith("/") ? loaderPath.substring(1) : loaderPath;
            return loaderPath.replace("\\", "/");
        }

        /**
         * 拡張子のチェック処理
         *
         * @return
         */
        public boolean isExtension(String... exts) {
            for (String ext : exts) {
                if (this.path.toString().endsWith(ext)) return true;
            }
            return false;
        }
    }


}
