package com.isport.tracker.keeplive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.isport.tracker.IRemoteConnection;
import com.isport.tracker.bluetooth.BootstrapService;
import com.isport.tracker.bluetooth.MainService;


/**
 * Created by Administrator on 2017/9/22.
 */

public class RemoteService extends Service {

    private RemoteBinder myBinder;
    private RemoteServiceConnection myServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        if (myBinder == null) {
            myBinder = new RemoteBinder();
        }
        myServiceConnection = new RemoteServiceConnection();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BootstrapService.startForeground(this);
        RemoteService.this.bindService(new Intent(RemoteService.this, MainService.class), myServiceConnection, Context.BIND_AUTO_CREATE);
        return super.onStartCommand(intent, flags, startId);
    }

    public class RemoteBinder extends IRemoteConnection.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getProcessName() throws RemoteException {
            return getProcessName();
        }
    }

    private class RemoteServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            RemoteService.this.bindService(new Intent(RemoteService.this, MainService.class), myServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
