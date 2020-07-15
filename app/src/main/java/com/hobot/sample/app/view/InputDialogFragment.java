package com.hobot.sample.app.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.hobot.sample.app.R;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class InputDialogFragment extends DialogFragment {
    private static final String TAG = "InputDialogFragment";

    private WeakReference<OnEventListener> mListener;
    private EditText nameEt;

    public InputDialogFragment() {
    }

    public InputDialogFragment setListener(OnEventListener listener) {
        mListener = new WeakReference<>(listener);
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_name, null);
        nameEt = (EditText) view.findViewById(R.id.et_name);
        AlertDialog dialog = builder.setTitle("请输入ID：")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null == mListener || null == mListener.get()) {
                            Log.w(TAG, "onClick: listener is null");
                            return;
                        }
                        mListener.get().onEvent(0, null, which, String.valueOf(nameEt.getText()));
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null == mListener || null == mListener.get()) {
                            Log.w(TAG, "onClick: listener is null");
                            return;
                        }
                        mListener.get().onEvent(0, null, which, String.valueOf(nameEt.getText()));
                    }
                })
                .setView(view)
                .setCancelable(false)
                .create();

        return dialog;
    }

    public static InputDialogFragment newInstance() {
        Bundle args = new Bundle();
        InputDialogFragment fragment = new InputDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void show(FragmentManager manager) {
        show(manager, TAG);
    }

    public interface OnEventListener {
        void onEvent(int id, View view, int code, String msg);
    }
}
