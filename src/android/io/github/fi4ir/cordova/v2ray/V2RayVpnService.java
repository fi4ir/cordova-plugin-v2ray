package io.github.fi4ir.cordova.v2ray;

import static io.github.fi4ir.cordova.v2ray.Utils.packagePath;

import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;

import go.Seq;
import libv2ray.Libv2ray;
import libv2ray.V2RayPoint;
import libv2ray.V2RayVPNServiceSupportsSet;

public class V2RayVpnService extends VpnService {
    public static final String TAG = V2RayVpnService.class.getName();
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_CHANGE_STATUS = 3;
    public static final int MSG_START = 4;
    public static final int MSG_STOP = 5;
    public static final int MSG_GET_STATUS = 6;


    ArrayList<Messenger> clients = new ArrayList<>();
    final Messenger messenger = new Messenger(new IncomingHandler());

    protected V2RayPoint v2rayPoint;
    protected ParcelFileDescriptor mInterface;
    protected VpnStatus status = VpnStatus.STOPPED;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();
        StrictMode.ThreadPolicy policy = builder.permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        this.stop();
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        this.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        V2RayConfig config = intent.getParcelableExtra("v2ray-config");
        ServerConfig serverConfig = intent.getParcelableExtra("server-config");
        if (config != null && serverConfig != null) {
            start(config, serverConfig);
        }
        return START_STICKY;
    }

    protected void start(V2RayConfig config, ServerConfig serverConfig) {
        this.setStatus(VpnStatus.STARTING);
        this.v2rayPoint = Libv2ray.newV2RayPoint(new V2RayCallback());
        Seq.setContext(getApplicationContext());
        this.v2rayPoint.setForwardIpv6(config.isForwardIpv6());
        this.v2rayPoint.setEnableLocalDNS(config.isEnableLocalDNS());
        this.v2rayPoint.setProxyOnly(config.isProxyOnly());
        this.v2rayPoint.setDomainName(serverConfig.getDomainName());
        this.v2rayPoint.setPackageName(packagePath(getApplicationContext()));
        this.v2rayPoint.setPackageCodePath(this.getApplicationContext().getApplicationInfo().nativeLibraryDir + "/");
        this.v2rayPoint.setConfigureFileContent(serverConfig.getRaw().toString());
        try {
            this.v2rayPoint.runLoop();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        if (!v2rayPoint.getIsRunning()) {
            this.setStatus(VpnStatus.STOPPED);
        }
    }

    protected void stop() {
        if (this.status != VpnStatus.STOPPED) {
            setStatus(VpnStatus.STOPING);
            if (this.v2rayPoint.getIsRunning()) {
                try {
                    this.v2rayPoint.stopLoop();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            setStatus(VpnStatus.STOPPED);
            stopSelf();
        }
    }

    protected void setStatus(VpnStatus status) {
        if (status != this.status) {
            Bundle data = new Bundle();
            data.putInt("oldStatus", this.status.ordinal());
            data.putInt("newStatus", status.ordinal());
            this.status = status;
            Message message = Message.obtain(null, MSG_CHANGE_STATUS);
            message.setData(data);
            this.notifyClients(message);
        }
    }

    protected void sendFd() {
        FileDescriptor fd = mInterface.getFileDescriptor();
        String path = (new File(packagePath(getApplicationContext()), "sock_path")).getAbsolutePath();
        new Thread(() -> {
            int tries = 0;
            while (true) {
                try {
                    if (tries > 0) {
                        Thread.sleep(1000L);
                    }
                    LocalSocket localSocket = new LocalSocket();
                    localSocket.connect(new LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM));
                    localSocket.setFileDescriptorsForSend(new FileDescriptor[]{fd});
                    localSocket.getOutputStream().write(42);
                    break;

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    if (tries > 5) {
                        break;
                    }
                    tries++;
                }
            }
        }).start();
    }

    protected void notifyClients(Message message) {
        for (Messenger client : this.clients) {
            try {
                message.replyTo = messenger;
                client.send(message);
            } catch (RemoteException e){
                // ignored
            }
        }
    }

    protected void notifyClients(int what) {
        notifyClients(Message.obtain(null, what));
    }

    protected class V2RayCallback implements V2RayVPNServiceSupportsSet {
        public long prepare() {
            return 0;
        }

        public long onEmitStatus(long l, String s) {
            return 0;
        }

        public long setup(String parameters) {
            setStatus(VpnStatus.SETUPING);
            Intent intent = VpnService.prepare(getApplicationContext());
            if (intent != null) {
                Log.e(TAG, "intent is not null");
                return -1;
            }
            Builder builder = new Builder();
            for (String option : parameters.split(" ")){
                String[] parts = option.split(",");
                switch (parts[0]) {
                    case "m":
                        builder.setMtu(Integer.parseInt(parts[1]));
                        break;
                    case "s":
                        builder.addSearchDomain(parts[1]);
                        break;
                    case "a":
                        builder.addAddress(parts[1], Integer.parseInt(parts[2]));
                        break;
                    case "r":
                        builder.addRoute(parts[1], Integer.parseInt(parts[2]));
                        break;
                    case "d":
                        builder.addDnsServer(parts[1]);
                        break;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false);
            }
            mInterface = builder.establish();
            sendFd();
            setStatus(VpnStatus.STARTED);
            return 0;
        }

        public long shutdown() {
            setStatus(VpnStatus.STOPPED);
            return 0;
        }
        public long protect(long socket) {
            if (V2RayVpnService.this.protect(Long.valueOf(socket).intValue())) {
                return 0;
            }
            return 1;
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    clients.remove(msg.replyTo);
                    break;
                case MSG_START:
                    V2RayConfig config = msg.getData().getParcelable("v2ray-config");
                    ServerConfig serverConfig = msg.getData().getParcelable("server-config");
                    if (config != null && serverConfig != null) {
                        start(config, serverConfig);
                    }
                    break;
                case MSG_STOP:
                    stop();
                    break;
                case MSG_GET_STATUS:
                    Message reply = Message.obtain(null, MSG_CHANGE_STATUS);
                    Bundle data = new Bundle();
                    data.putInt("oldStatus", status.ordinal());
                    data.putInt("newStatus", status.ordinal());
                    reply.setData(data);
                    reply.replyTo = messenger;
                    try {
                        msg.replyTo.send(reply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
