package com.sdcz.endpass.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sdcz.endpass.R;
import com.sdcz.endpass.fragment.SettingsFragment;

/**
 * @Description: 会中显示设置页面的对话框
 */
public class SettingsFragmentDialog extends DialogFragment implements View.OnClickListener {

    private ImageView imgBack;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        getDialog().getWindow().setWindowAnimations(R.style.bottom_dialog);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.dialog_settings_fragment, null);
        FragmentManager fragmentManager = getChildFragmentManager();
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentManager.beginTransaction().replace(R.id.settingsFragment, settingsFragment).commit();
        imgBack = content.findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
        return content;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) {
            return;
        }
        Context context = getContext();
        Fragment settings = fragmentManager.findFragmentById(R.id.settingsFragment);
        if (settings != null) {
            fragmentManager.beginTransaction()
                    .remove(settings)
                    .commit();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imgBack) {
            dismiss();
        }
    }
}

















