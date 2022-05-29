package com.sdcz.endpass.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sdcz.endpass.ui.RoomListFragment;


public class RoomTypeAdapter extends FragmentStateAdapter {

    public RoomTypeAdapter(@NonNull FragmentManager fragmentManager,
                           @NonNull Lifecycle lifecycle ) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(RoomListFragment.EXTRA_TYPE,position);
        RoomListFragment fragment = new RoomListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
