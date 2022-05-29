package com.sdcz.endpass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseFragment;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class FragmentPageAdapter extends FragmentPagerAdapter {

    private Context context;
    /**
     * fragment列表
     */
    private List<BaseFragment> list_fragment;
    /**
     * tab名的列表
     */
    private List<String> list_Title;
    /**
     * tab图标的列表
     */
    private List<Integer> list_Icon;

    public FragmentPageAdapter(Context context, FragmentManager fm, List<BaseFragment> list_fragment, List<String> list_Title){
        super(fm);
        this.context = context;
        this.list_fragment = list_fragment;
        this.list_Title = list_Title;
    }

    public FragmentPageAdapter(Context context, FragmentManager fm, List<BaseFragment> list_fragment, List<String> list_Title, List<Integer> list_Icon){
        super(fm);
        this.context = context;
        this.list_fragment = list_fragment;
        this.list_Title = list_Title;
        this.list_Icon = list_Icon;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return list_fragment.get(position);
    }

    @Override
    public int getCount() {
        return list_fragment.size();
    }

    /**
      *     此方法用来显示tab上的名字
      */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return list_Title.get(position % list_Title.size());
    }

    /**
     * 渲染列表item
     * 使用方法：
     * adapter = new FragmentPageAdapter(this,getSupportFragmentManager(), fragmentList,titleList,iconList);
     *         viewPager.setAdapter(adapter);
     *         //将TabLayout与ViewPager绑定在一起
     *         tableLayout.setupWithViewPager(viewPager);
     *         for (int i = 0; i < tableLayout.getTabCount(); i++) {
     *             TabLayout.Tab tab = tableLayout.getTabAt(i);
     *             //设置要用于对应tab的自定义视图。
     *             if (tab != null) {
     *                 tab.setCustomView(adapter.getTabView(i));
     *             }
     *         }
     */
    public View getTabView(int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView tv = v.findViewById(R.id.textView);
        tv.setText(list_Title.get(position));
        if (list_Icon != null){
            ImageView img = v.findViewById(R.id.imgView);
            img.setImageResource(list_Icon.get(position));
        }
        return v;
    }
}
