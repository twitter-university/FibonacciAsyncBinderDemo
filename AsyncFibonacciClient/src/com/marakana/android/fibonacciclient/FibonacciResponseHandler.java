package com.marakana.android.fibonacciclient;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.marakana.android.fibonaccicommon.FibonacciResponse;

public class FibonacciResponseHandler extends Handler {
	private static final String TAG = "FibonacciResponseHandler";
	private final WeakReference<FibonacciFragment> fibonacciFragmentRef;

	public FibonacciResponseHandler(FibonacciFragment fibonacciFragment) {
		this.fibonacciFragmentRef = new WeakReference<FibonacciFragment>(
				fibonacciFragment);
	}

	public void handleMessage(Message message) {
		Log.d(TAG, "handleMessage(" + message + ")");
		FibonacciResponse fibonacciResponse = (FibonacciResponse) message.obj;
		FibonacciFragment fibonacciFragment = this.fibonacciFragmentRef.get();
		if (fibonacciFragment == null) {
			Log.w(TAG, "No fragment to talk to. Giving up");
		} else {
			Log.d(TAG, "Delivering response: " + fibonacciResponse);
			fibonacciFragment.onResponse(fibonacciResponse);
		}
	}
}