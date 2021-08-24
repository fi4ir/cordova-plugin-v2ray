package io.github.fi4ir.cordova.v2ray;

public interface IActionCallback {
    public void onSuccess();
    public void onSuccess(int data);
    public void onSuccess(long data);
    public void onError(int resultCode);
    public String getID();
    public void setID(String id);
}
