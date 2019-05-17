package x8sdk.update.fwpack;

public enum FirmwareType {
    RC,
    RC_RELAY,
    FC,
    FC_RELAY,
    ESC,
    GIMBAL,
    CAMERA,
    NFZ,
    OTA,
    UNKNOWN;

    public static FirmwareType fromString(String type){
        for(FirmwareType firmwareType : FirmwareType.values()){
            if(firmwareType.name().equalsIgnoreCase(type)){
                return firmwareType;
            }
        }
        return UNKNOWN;
    }
}
