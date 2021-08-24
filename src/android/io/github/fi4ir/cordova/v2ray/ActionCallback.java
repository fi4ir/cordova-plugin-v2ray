package io.github.fi4ir.cordova.v2ray;

public class ActionCallback implements IActionCallback {
    private String id;
    public ActionCallback() {}
    public ActionCallback(String id) {
        this.id = id;
    }
    public void onSuccess() {}
    public void onSuccess(int data) {}
    public void onSuccess(long data) {}
    public void onError(int resultCode) {}
    public String getID() {
        return id;
    }
    public void setID(String id) {
        this.id = id;
    }
}
