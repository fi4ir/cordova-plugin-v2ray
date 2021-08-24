package io.github.fi4ir.cordova.v2ray;

import static io.github.fi4ir.cordova.v2ray.V2RayVpnService.MSG_CHANGE_STATUS;
import static io.github.fi4ir.cordova.v2ray.V2RayVpnService.MSG_QUERY_STATS;
import static io.github.fi4ir.cordova.v2ray.V2RayVpnService.MSG_STOP;
import static io.github.fi4ir.cordova.v2ray.V2RayVpnService.MSG_UNREGISTER_CLIENT;
import static io.github.fi4ir.cordova.v2ray.V2RayVpnService.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class V2RayPlugin extends CordovaPlugin {
    protected static int VPN_SERVICE_PREPARE = 2;
    protected IActionCallback prepareCb;
    protected IActionCallback startCb;
    protected IActionCallback stopCb;
    protected IActionCallback statusCb;
    protected IActionCallback watchStatusCb;
    protected List<IActionCallback> queryStatsCb = new ArrayList<>();
    Messenger service;
    VpnStatus serviceStatus = null;
    final Messenger messenger = new Messenger(new IncomingHandler());
    boolean bound;

    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = new Messenger(binder);
            try {
                {
                    Message msg = Message.obtain(null, V2RayVpnService.MSG_REGISTER_CLIENT);
                    msg.replyTo = messenger;
                    service.send(msg);
                }

                {
                    Message msg = Message.obtain(null, V2RayVpnService.MSG_GET_STATUS);
                    msg.replyTo = messenger;
                    service.send(msg);
                }
            } catch (RemoteException e) {
                // ignored
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            service = null;
        }
    };


    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("prepare")) {
            this.prepare(new ActionCallback() {
                @Override
                public void onSuccess() {
                    callbackContext.success();
                }

                @Override
                public void onError(int resultCode) {
                    callbackContext.error(resultCode);
                }
            });
            return true;
        } else if (action.equals("start")) {
            if (args.length() != 2) {
                callbackContext.error("Invalid args");
                return false;
            }
            V2RayConfig config;
            try {
                config = new V2RayConfig(args.getJSONObject(0));
            } catch (JSONException e) {
                callbackContext.error("invalid config argument");
                return false;
            }
            ServerConfig serverConfig;
            try {
                serverConfig = new ServerConfig(args.getJSONObject(1));
            } catch (JSONException e) {
                callbackContext.error("invalid server-config argument");
                return false;
            }

            this.start(config, serverConfig, new ActionCallback() {
                @Override
                public void onSuccess() {
                    callbackContext.success();
                }

                @Override
                public void onError(int resultCode) {
                    callbackContext.error(resultCode);
                }
            });
            return true;
        } else if (action.equals("stop")) {
            this.stop(new ActionCallback(){
                @Override
                public void onSuccess() {
                    callbackContext.success();
                }

                @Override
                public void onError(int resultCode) {
                    callbackContext.error(resultCode);
                }
            });
            return true;
        } else if (action.equals("status")) {
            if (this.serviceStatus != null) {
                callbackContext.success(this.serviceStatus.ordinal());
                return true;
            }
            this.statusCb = new ActionCallback() {
                @Override
                public void onSuccess(int data) {
                    callbackContext.success(data);
                }

                @Override
                public void onError(int resultCode) {
                    callbackContext.error(resultCode);
                }
            };
            return true;
        } else if (action.equals("watch-status")) {
            this.watchStatusCb = new ActionCallback() {
                @Override
                public void onSuccess(int data) {
                    callbackContext.success(data);
                }

                @Override
                public void onError(int resultCode) {
                    callbackContext.error(resultCode);
                }
            };
            return true;
        } else if (action.equals("query-stats")) {
            if (args.length() != 2) {
                callbackContext.error("Invalid args");
                return false;
            }
            try {
                this.queryStats(args.getString(0), args.getString(1), new ActionCallback(callbackContext.getCallbackId()) {
                    @Override
                    public void onSuccess(long data) {
                        callbackContext.success(Long.valueOf(data).toString());
                    }

                    @Override
                    public void onError(int resultCode) {
                        callbackContext.error(resultCode);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public void prepare(IActionCallback cb) {
        this.prepareCb = cb;
        Intent intent = VpnService.prepare(this.cordova.getContext());
        if (intent != null) {
            this.cordova.startActivityForResult(this, intent, VPN_SERVICE_PREPARE);
        } else {
            this.prepareCb.onSuccess();
            this.prepareCb = null;
        }
    }

    public void start(V2RayConfig config, ServerConfig serverConfig, IActionCallback cb) {
        this.prepare(new ActionCallback() {
            @Override
            public void onSuccess() {
                startCb = cb;
                Intent intent = new Intent(cordova.getContext(), V2RayVpnService.class);
                intent.putExtra("server-config", serverConfig);
                intent.putExtra("v2ray-config", config);
                cordova.getActivity().startService(intent);
            }

            @Override
            public void onError(int resultCode) {
                cb.onError(resultCode);
            }
        });
    }

    public void stop(IActionCallback cb) {
        Message message = Message.obtain(null, MSG_STOP);
        message.replyTo = messenger;
        try {
            stopCb = cb;
            service.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            cb.onError(512);
        }
    }

    public void queryStats(String tag, String direct, IActionCallback cb) {
        if (cb.getID() == null) {
            throw new RuntimeException("callback must have an Id");
        }
        Bundle data = new Bundle();
        data.putString("tag", tag);
        data.putString("direct", direct);
        data.putString("callback-id", cb.getID());
        Message message = Message.obtain(null, MSG_QUERY_STATS);
        message.replyTo = messenger;
        message.setData(data);

        try {
            service.send(message);
            this.queryStatsCb.add(cb);
        } catch (RemoteException e) {
            e.printStackTrace();
            cb.onError(512);
        }
    }

    @Override
    public void onStart() {
        bindService();
    }

    @Override
    public void onStop() {
        unbindService();
        this.resetCallbacks();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == VPN_SERVICE_PREPARE) {
            if (resultCode == Activity.RESULT_OK) {
                this.prepareCb.onSuccess();
            } else {
                this.prepareCb.onError(resultCode);
            }
            this.prepareCb = null;
        }
    }

    protected void bindService() {
        Activity activity = this.cordova.getActivity();
        activity.bindService(new Intent(activity, V2RayVpnService.class), connection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    protected void unbindService() {
        if (bound) {
            if (service != null) {
                try {
                    Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messenger;
                    service.send(msg);
                } catch (RemoteException e) {
                    // ignored
                }
            }

            this.cordova.getActivity().unbindService(connection);
            bound = false;
        }
    }

    protected IActionCallback popCallbackByID(String id, List<IActionCallback> cbs) {
        if (id == null) {
            return null;
        }
        for (int x = 0, l = cbs.size(); x < l; x++) {
            IActionCallback i = cbs.get(x);
            if (i.getID() != null && i.getID().equals(id)) {
                cbs.remove(x);
                return i;
            }
        }
        return null;
    }

    protected void resetCallbacks() {
        this.prepareCb = null;
        this.queryStatsCb.clear();
        this.statusCb = null;
        this.watchStatusCb = null;
        this.startCb = null;
        this.stopCb = null;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGE_STATUS: {
                    VpnStatus newStatus = VpnStatus.fromInt(msg.getData().getInt("newStatus"));
                    serviceStatus = newStatus;
                    if (newStatus == null) {
                        return;
                    }
                    if (statusCb != null) {
                        statusCb.onSuccess(newStatus.ordinal());
                        statusCb = null;
                    }
                    if (watchStatusCb != null) {
                        watchStatusCb.onSuccess(newStatus.ordinal());
                    }
                    if (newStatus == VpnStatus.STARTED) {
                        if (startCb != null) {
                            startCb.onSuccess();
                            startCb = null;
                        }
                    } else if (newStatus == VpnStatus.STOPPED) {
                        if (stopCb != null) {
                            stopCb.onSuccess();
                            stopCb = null;
                        }
                    }
                    break;
                }
                case MSG_QUERY_STATS: {
                    Bundle data = msg.getData();
                    long result = data.getLong("stats");
                    String cbID = data.getString("callback-id");
                    IActionCallback cb = popCallbackByID(cbID, queryStatsCb);
                    if (cb != null) {
                        cb.onSuccess(result);
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
