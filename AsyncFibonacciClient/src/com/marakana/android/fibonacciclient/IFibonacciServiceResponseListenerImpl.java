package com.marakana.android.fibonacciclient;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.marakana.android.fibonaccicommon.FibonacciResponse;
import com.marakana.android.fibonaccicommon.IFibonacciServiceResponseListener;

public class IFibonacciServiceResponseListenerImpl extends
		IFibonacciServiceResponseListener.Stub {

	private static final String TAG = "IFibonacciServiceResponseListenerImpl";

	private final Handler handler;

	public IFibonacciServiceResponseListenerImpl(Handler handler) {
		this.handler = handler;
	}

	// this method is executed on one of the pooled binder threads
	@Override
	public void onResponse(FibonacciResponse response) throws RemoteException {
		Log.d(TAG, "onResponse(" + response + ")");
		Message message = this.handler.obtainMessage(0, response);
		this.handler.sendMessage(message);
	}
}
