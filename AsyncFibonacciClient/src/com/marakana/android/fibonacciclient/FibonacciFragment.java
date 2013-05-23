package com.marakana.android.fibonacciclient;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.marakana.android.fibonaccicommon.FibonacciResponse;
import com.marakana.android.fibonaccicommon.IFibonacciServiceResponseListener;

public class FibonacciFragment extends Fragment {
	private static final String TAG = "FibonacciFragment";

	public static interface FibonacciActivityResponseListener {
		public void onResponse(FibonacciResponse fibonacciResponse);
	}

	private final IFibonacciServiceResponseListener iFibonacciServiceResponseListener = new IFibonacciServiceResponseListenerImpl(
			new FibonacciResponseHandler(this));
	private FibonacciActivityResponseListener fibonacciActivityResponseListener;
	private FibonacciResponse pendingFibonacciResponse;
	private Dialog dialog;

	// invoked when the fragment is first created (not on configuration change)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		// indicate that we want to survive configuration changes
		super.setRetainInstance(true);
	}

	// invoked on configuration changes as well as state changes
	@Override
	public void onStart() {
		super.onStart();
		// get our activity (as a listener)
		this.fibonacciActivityResponseListener = (FibonacciActivityResponseListener) super
				.getActivity();
		synchronized (this) {
			if (this.pendingFibonacciResponse == null) {
				Log.d(TAG, "No pending response. Saving listener.");
			} else {
				Log.d(TAG, "Deliverying pending response: "
						+ this.pendingFibonacciResponse);
				this.fibonacciActivityResponseListener
						.onResponse(this.pendingFibonacciResponse);
				this.pendingFibonacciResponse = null;
			}
		}
		// pop up a progress dialog
		Log.d(TAG, "Showing dialog");
		this.dialog = ProgressDialog.show(super.getActivity(), "", super
				.getActivity().getText(R.string.progress_text), true);
	}

	// invoked on configuration changes as well as state changes
	@Override
	public void onStop() {
		super.onStop();
		synchronized (this) {
			// dismiss our dialog; it can go away along with our listener
			Log.d(TAG, "Stopped. Dismissing the listener and the dialog.");
			this.dialog.dismiss();
			this.dialog = null;
			this.pendingFibonacciResponse = null;
		}
	}

	// called by activity to get the binder listener
	public IFibonacciServiceResponseListener getIFibonacciServiceResponseListener() {
		return this.iFibonacciServiceResponseListener;
	}

	// called by the handler
	public synchronized void onResponse(FibonacciResponse fibonacciResponse) {
		// if there is no listener (i.e. activity)
		if (this.fibonacciActivityResponseListener == null) {
			Log.d(TAG, "Saving pending response: " + fibonacciResponse);
			// save for the activity when it comes back
			this.pendingFibonacciResponse = fibonacciResponse;
		} else {
			// we are done, send the result
			Log.d(TAG, "Deliverying response: " + fibonacciResponse);
			this.fibonacciActivityResponseListener
					.onResponse(fibonacciResponse);
		}
	}
}