package io.github.fi4ir.cordova.v2ray;

public enum VpnStatus {
    STARTING,
    SETUPING,
    STARTED,
    STOPING,
    STOPPED;

    public static VpnStatus fromInt(int x) {
        switch(x) {
            case 0:
                return STARTING;
            case 1:
                return SETUPING;
            case 2:
                return STARTED;
            case 3:
                return STOPING;
            case 4:
                return STOPPED;
        }
        return null;
    }
}
