package com.marakana.android.fibonacciclient;

import java.util.Locale;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.marakana.android.fibonaccicommon.FibonacciResponse;
import com.marakana.android.fibonaccicommon.IFibonacciServiceResponseListener;

public class FibonacciFragment extends Fragment {
	private static final String TAG = "FibonacciFragment";

	// to be implemented by our activity
	public static interface OnResultListener {
		public void onResult(String result);
	}

	private Dialog dialog;
	private OnResultListener onResultListener;
	private String pendingResult;
	private Locale locale;

	// the responsibility of the responseHandler is to take messages
	// from the responseListener (defined below) and display their content
	// in the UI thread
	private final Handler responseHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			Log.d(TAG, "Handling response");
			String result = (String) message.obj;
			synchronized (FibonacciFragment.this) {
				// if there is no listener (i.e. activity)
				if (FibonacciFragment.this.onResultListener == null) {
					Log.d(TAG, "Saving pending result: " + result);
					// save for the activity when it comes back (if
					// possible?)
					FibonacciFragment.this.pendingResult = result;
				} else {
					// we are done, send the result
					Log.d(TAG, "Submitting result: " + result);
					FibonacciFragment.this.onResultListener.onResult(result);
				}
			}
		}
	};

	// the responsibility of the responseListener is to receive call-backs
	// from the service when our FibonacciResponse is available
	private final IFibonacciServiceResponseListener responseListener = new IFibonacciServiceResponseListener.Stub() {

		// this method is executed on one of the pooled binder threads
		@Override
		public void onResponse(FibonacciResponse response)
				throws RemoteException {
			String result = String.format(locale, "%d in %d ms",
					response.getResult(), response.getTimeInMillis());
			Log.d(TAG, "Got response: " + result);
			// since we cannot update the UI from a non-UI thread,
			// we'll send the result to the responseHandler (defined above)
			Message message = FibonacciFragment.this.responseHandler
					.obtainMessage(0, result);
			FibonacciFragment.this.responseHandler.sendMessage(message);
		}
	};

	public IFibonacciServiceResponseListener getResponseListener() {
		return responseListener;
	}

	// invoked when the fragment is first created (not on configuration change)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		// indicate that we want to survive configuration changes
		super.setRetainInstance(true);
		this.locale = super.getActivity().getResources().getConfiguration().locale;
	}

	// invoked on configuration changes as well as state changes
	@Override
	public void onStart() {
		super.onStart();
		// get our activity (as a listener)
		this.onResultListener = (OnResultListener) super.getActivity();
		if (this.pendingResult == null) {
			Log.d(TAG, "No pending result. Saving listener for future result");
		} else {
			synchronized (FibonacciFragment.this) {
				// send the result if we have one
				Log.d(TAG, "Submitting pending result: " + this.pendingResult);
				this.onResultListener.onResult(this.pendingResult);
				this.pendingResult = null;
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
		synchronized (FibonacciFragment.this) {
			// dismiss our dialog; it can go away along with our listener
			Log.d(TAG, "Stopped. Dismissing the listener and the dialog.");
			this.dialog.dismiss();
			this.dialog = null;
			this.onResultListener = null;
		}
	}
}