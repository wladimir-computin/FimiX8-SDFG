package x8sdk.update;

public class FwContenti18N extends BaseModel {
    private String en_US;
    private String zh_CN;
    private String zh_TW;

    public String getZh_CN() {
        return this.zh_CN;
    }

    public void setZh_CN(String zh_CN) {
        this.zh_CN = zh_CN;
    }

    public String getZh_TW() {
        return this.zh_TW;
    }

    public void setZh_TW(String zh_TW) {
        this.zh_TW = zh_TW;
    }

    public String getEn_US() {
        return this.en_US;
    }

    public void setEn_US(String en_US) {
        this.en_US = en_US;
    }
}
