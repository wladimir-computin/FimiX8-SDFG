package x8sdk.update.fwpack;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;

public class FwInfo implements Serializable {
    private byte forceType;
    private byte modelId;
    private short softwareVer;
    private byte stepVer;
    private Path filename;
    private byte typeId;
    private String downloadUrl;
    private String checksumMD5;
    private Date releaseDate;


    public byte getModelId() {
        return this.modelId;
    }

    public void setModelId(byte modelId) {
        this.modelId = modelId;
    }

    public byte getTypeId() {
        return this.typeId;
    }

    public void setTypeId(byte typeId) {
        this.typeId = typeId;
    }

    public byte getForceType() {
        return this.forceType;
    }

    public void setForceType(byte forceType) {
        this.forceType = forceType;
    }

    public short getSoftwareVer() {
        return this.softwareVer;
    }

    public void setSoftwareVer(short softwareVer) {
        this.softwareVer = softwareVer;
    }

    public byte getStepVer() {
        return this.stepVer;
    }

    public void setStepVer(byte stepVer) {
        this.stepVer = stepVer;
    }

    public Path getFilename() {
        return this.filename;
    }

    public void setFilename(Path filename) {
        this.filename = filename;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getChecksumMD5() {
        return checksumMD5;
    }

    public void setChecksumMD5(String checksumMD5) {
        this.checksumMD5 = checksumMD5;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public FirmwareType getFirmwareType(){
        if (this.typeId == 0 && this.modelId == 3) {
            return FirmwareType.FC;
        }
        if (this.typeId == 1 && this.modelId == 3) {
            return FirmwareType.RC;
        }
        if (this.typeId == 9 && this.modelId == 1) {
            return FirmwareType.OTA;
        }
        if (this.typeId == 11 && this.modelId == 3) {
            return FirmwareType.RC_RELAY;
        }
        if (this.typeId == 12 && this.modelId == 3) {
            return FirmwareType.FC_RELAY;
        }
        if (this.typeId == 14 && this.modelId == 0) {
            return FirmwareType.ESC;
        }
        if (this.typeId == 3 && this.modelId == 6) {
            return FirmwareType.GIMBAL;
        }
        if (this.typeId == 5 && this.modelId == 3) {

        }
        if (this.typeId == 10 && this.modelId == 3) {
            return FirmwareType.NFZ;
        }
        if (this.typeId == 4 && this.modelId == 2) {
            return FirmwareType.CAMERA;
        }
        if (this.typeId == 13 && this.modelId == 1) {

        }
        return FirmwareType.UNKNOWN;
    }

    public String toString() {
        return "FwInfo " + getFirmwareType().name() +
                " [modelId=" + this.modelId +
                ", typeId=" + this.typeId +
                ", forceType=" + this.forceType +
                ", softwareVer=" + this.softwareVer +
                ", stepVer=" + this.stepVer +
                "]";
    }
}
