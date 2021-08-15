package io.github.fi4ir.cordova.v2ray;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class V2RayConfig implements Parcelable {
    public static final Parcelable.Creator<V2RayConfig> CREATOR = new Parcelable.Creator<V2RayConfig>() {
        public V2RayConfig createFromParcel(Parcel in) {
            try {
                V2RayConfig config = new V2RayConfig();

                boolean[] params = new boolean[3];
                in.readBooleanArray(params);
                config.setForwardIpv6(params[0]);
                config.setEnableLocalDNS(params[1]);
                config.setProxyOnly(params[2]);
                return config;
            } catch (Exception e) {
                // ignored
            }
            return null;
        }

        public V2RayConfig[] newArray(int size) {
            return new V2RayConfig[size];
        }
    };

    protected boolean forwardIpv6;
    protected boolean enableLocalDNS;
    protected boolean proxyOnly;

    public V2RayConfig() {}
    public V2RayConfig (JSONObject raw) throws JSONException {
        if (raw.has("forwardIpv6")) {
            this.forwardIpv6 = raw.getBoolean("forwardIpv6");
        }
        if (raw.has("enableLocalDNS")) {
            this.enableLocalDNS = raw.getBoolean("enableLocalDNS");
        }
        if (raw.has("proxyOnly")) {
            this.proxyOnly = raw.getBoolean("proxyOnly");
        }
    }

    public boolean isForwardIpv6() {
        return forwardIpv6;
    }

    public void setForwardIpv6(boolean forwardIpv6) {
        this.forwardIpv6 = forwardIpv6;
    }

    public boolean isEnableLocalDNS() {
        return enableLocalDNS;
    }

    public void setEnableLocalDNS(boolean enableLocalDNS) {
        this.enableLocalDNS = enableLocalDNS;
    }

    public boolean isProxyOnly() {
        return proxyOnly;
    }

    public void setProxyOnly(boolean proxyOnly) {
        this.proxyOnly = proxyOnly;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBooleanArray(new boolean[]{this.forwardIpv6, this.enableLocalDNS, this.proxyOnly});
    }

}
