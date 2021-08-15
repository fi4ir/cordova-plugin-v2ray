package io.github.fi4ir.cordova.v2ray;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfig implements Parcelable {
    public static final Parcelable.Creator<ServerConfig> CREATOR = new Parcelable.Creator<ServerConfig>() {
        public ServerConfig createFromParcel(Parcel in) {
            try {
                JSONObject raw = new JSONObject(in.readString());
                return new ServerConfig(raw);
            } catch (Exception e) {
                // ignored
            }
            return null;
        }

        public ServerConfig[] newArray(int size) {
            return new ServerConfig[size];
        }
    };


    protected JSONObject raw;

    public ServerConfig(JSONObject raw) {
        this.raw = raw;
    }

    public JSONObject getRaw() {
        return this.raw;
    }

    protected JSONObject getProxyOutbound() throws JSONException {
        JSONArray outbounds = this.raw.getJSONArray("outbounds");
        for (int x = 0, l = outbounds.length(); x < l; x++) {
            JSONObject outbound = outbounds.getJSONObject(x);
            String protocol = outbound.getString("protocol");
            if (protocol.equals("vmess") || protocol.equals("shadowsocks") || protocol.equals("socks")) {
                return outbound;
            }
        }
        return null;
    }

    public String getDomainName() {
        try {
            JSONObject outbound = this.getProxyOutbound();
            if (outbound == null) {
                return "";
            }
            String protocol = outbound.getString("protocol");
            JSONObject server = null;
            if (protocol.equals("vmess")) {
                server = outbound.getJSONObject("settings").getJSONArray("vnext").getJSONObject(0);
            } else if (protocol.equals("socks") || protocol.equals("shadowsocks")) {
                server = outbound.getJSONObject("settings").getJSONArray("servers").getJSONObject(0);
            }
            if (server == null) {
                return "";
            }
            String address = server.getString("address");
            String port = server.optString("port");
            if (Utils.isIpv6Address(address)) {
                address = "[" + address + "]";
            }
            return address + ":" + port;
        } catch (JSONException e) {
            // ignored
        }
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.raw.toString());
    }
}
