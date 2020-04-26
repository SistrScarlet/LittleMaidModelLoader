package net.sistr.lmml.client.maidmodel;

/**
 * サンプルマルチモデル定義
 *
 * ModelMultiBaseを継承したクラスをマルチモデルと判断する
 *
 * 通常のメイドさんの改良
 * 　基準メイドさんを改変する場合はModelLittleMaidBaseを継承する
 * 　すでに存在するメイドさんを改変する場合はModelLittleMaid_SR2などを継承する
 * 　本クラスはSR2モデルを継承している
 *
 * 命名規則（モデル）
 * 　ModelLittleMaid_xxxxxxxxをクラス名とすること
 * 　xxxxxxxxがモデル名となりテクスチャとのリンクに利用される
 * 　本クラスの場合はSampleMaidModelがモデル名となる
 *
 * 命名規則（テクスチャ）
 * 　assets.minecraft.textures.entity.littleMaidが基準フォルダとなる
 * 　[パッケージ].[テクスチャ名]_[モデル名]形式でフォルダを作成する
 * 　　※[パッケージ]は省略可能
 * 　上記フォルダ内にメイドさんテクスチャを格納する
 * 　メイドさんテクスチャはxxxxxxxx_0.png～xxxxxxxx_f.pngまで
 * 　xxxxxxxxは任意の文字列となり末尾の英数字が16進数で16色の色情報を意味する
 *
 *
 * 本サンプルのテクスチャが格納されているフォルダは下記を意味している
 * 　Tutorial.DemoModel_SampleMaidModel
 *
 * 　Tutorial [パッケージ]
 * 　DemoModel [テクスチャ名]
 * 　SampleMaidModel [モデル名]
 *
 * 　[モデル名]はモデルクラスと同一の名前になるように設定すること
 * 　本サンプルの場合は[SampleMaidModel]がモデル名となる。
 *
 * 同一モデルで別テクスチャを使用する場合は
 * 別名称のパッケージまたはテクスチャ名のフォルダを用意すれば
 * モデルは同じでテクスチャは別のものを設定できる
 *
 * メイドさん選択画面でTutorial.DemoModel_SampleMaidModelが表示される
 *
 */
public class ModelLittleMaid_WingSR2 extends ModelLittleMaid_SR2 {
	public ModelRenderer wingR;
	public ModelRenderer wingL;

	public ModelLittleMaid_WingSR2() {
		super();
	}

	public ModelLittleMaid_WingSR2(float psize) {
		super(psize);
	}

	public ModelLittleMaid_WingSR2(float psize, float pyoffset, int pTextureWidth, int pTextureHeight) {
		super(psize, pyoffset, pTextureWidth, pTextureHeight);
	}

	public void initModel(float psize, float pyoffset) {
		super.initModel(psize, pyoffset);
		this.wingR = new ModelRenderer(this, 0, 16);
		this.wingR.addPlate(-9.0F, 0.0F, 2.1F, 8, 8, 0, psize);
		this.wingR.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedBody.addChild(this.wingR);
		this.wingL = new ModelRenderer(this, 0, 0);
		this.wingL.addPlate(1.0F, 0.0F, 2.1F, 8, 8, 0, psize);
		this.wingL.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedBody.addChild(this.wingL);
	}

	public void setLivingAnimations(IModelCaps pEntityCaps, float par2, float par3, float pRenderPartialTicks) {
		super.setLivingAnimations(pEntityCaps, par2, par3, pRenderPartialTicks);
		//羽をパタパタさせる。3秒で一往復
		float percent = (this.entityTicksExisted % 60 + pRenderPartialTicks) / 60F;//0~1直線
		boolean minus = 60 < this.entityTicksExisted % 120;
		if (0.5F < percent) {
			percent = (1 - percent);//0 ~ 0.5 往復
		}
		percent *= 2;//0 ~ 1 往復
		float maxAngle = 15;
		float offset = maxAngle;
		maxAngle = minus ? -maxAngle : maxAngle;
		float rad = (PI / 180);
		float angle = offset * rad + mh_sin((percent * 90) * rad) * maxAngle * rad;
		this.wingR.rotateAngleY = angle;
		this.wingL.rotateAngleY = -angle;
	}



}
