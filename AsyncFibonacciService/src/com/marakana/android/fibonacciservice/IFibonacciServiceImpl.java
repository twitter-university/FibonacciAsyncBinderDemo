package com.marakana.android.fibonacciservice;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.marakana.android.fibonaccicommon.FibonacciRequest;
import com.marakana.android.fibonaccicommon.FibonacciResponse;
import com.marakana.android.fibonaccicommon.IFibonacciService;
import com.marakana.android.fibonaccicommon.IFibonacciServiceResponseListener;
import com.marakana.android.fibonaccinative.FibLib;

public class IFibonacciServiceImpl extends IFibonacciService.Stub {
	private static final String TAG = "IFibonacciServiceImpl";

	@Override
	public void fib(final FibonacciRequest request,
			final IFibonacciServiceResponseListener listener)
			throws RemoteException {
		Log.d(TAG, "fib(" + request + ")");
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				final long n = request.getN();
				final long result;
				long timeInMillis = SystemClock.uptimeMillis();
				switch (request.getType()) {
				case ITERATIVE_JAVA:
					result = FibLib.fibJI(n);
					break;
				case RECURSIVE_JAVA:
					result = FibLib.fibJR(n);
					break;
				case ITERATIVE_NATIVE:
					result = FibLib.fibNI(n);
					break;
				case RECURSIVE_NATIVE:
					result = FibLib.fibNR(n);
					break;
				default:
					result = 0;
				}
				timeInMillis = SystemClock.uptimeMillis() - timeInMillis;
				Log.d(TAG, String.format("Got fib(%d) = %d in %d ms", n,
						result, timeInMillis));
				try {
					FibonacciResponse fibonacciResponse = new FibonacciResponse(
							result, timeInMillis);
					listener.onResponse(fibonacciResponse);
					Log.d(TAG, "Sent to listener: " + fibonacciResponse);
				} catch (RemoteException e) {
					Log.wtf(TAG, "Failed to notify listener", e);
				}
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
}
