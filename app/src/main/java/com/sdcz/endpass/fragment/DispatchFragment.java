package com.sdcz.endpass.fragment;

import android.view.View;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;
import com.google.android.material.tabs.TabLayout;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.FragmentPageAdapter;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.presenter.DispatchPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IDispatchView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/28 15:37
 * Description: @
 */
public class DispatchFragment extends BaseFragment<DispatchPresenter> implements IDispatchView {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<BaseFragment> fragmentList;
    private FragmentPageAdapter adapter;
    private TextView tvTitle;
    private SlidingTabLayout tabLayout2;;

    @Override
    protected DispatchPresenter createPresenter() {
        return new DispatchPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_dispatch;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
//        tabLayout = rootView.findViewById(R.id.tab);
        tabLayout2 = rootView.findViewById(R.id.tablayout);
        viewPager = rootView.findViewById(R.id.viewPager);
        tvTitle = rootView.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.tab_dispath);
    }

    @Override
    public void initData() {
        super.initData();
        fragmentList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        String role = SharedPrefsUtil.getRoleId();

        if (role.equals("manage") || role.equals("admin")){
            titleList.add("调度任务");
            titleList.add("任务管理");
            fragmentList.add(new TaskListFragment());
            fragmentList.add(new TaskManageFragment());
            tabLayout2.setVisibility(View.VISIBLE);
        }else {
            titleList.add("调度任务");
            fragmentList.add(new TaskListFragment());
            tabLayout2.setVisibility(View.GONE);
        }

        adapter = new FragmentPageAdapter(getActivity(),getFragmentManager(), fragmentList,titleList);
        viewPager.setAdapter(adapter);
        tabLayout2.setViewPager(viewPager,titleList.toArray(new String[titleList.size()]));

//        //将TabLayout与ViewPager绑定在一起
//        tabLayout.setupWithViewPager(viewPager);
//        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//             TabLayout.Tab tab = tabLayout.getTabAt(i);
//             //设置要用于对应tab的自定义视图。
//             if (tab != null) {
//                 tab.setCustomView(adapter.getTabView(i));
//             }
//         }


        //将TabLayout与ViewPager绑定在一起
//        tabLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                IndicatorLineUtil.setIndicator(tabLayout, 40, 40);
//            }
//        });
//        tabLayout.setupWithViewPager(viewPager);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void refarechLayout(EventRefarechLayout event){
//        if (SharedPrefsUtil.getValue(getContext(), KeyStore.ROLE, null).equals("3")){
//            tabLayout2.setVisibility(View.GONE);
//        }else {
//            tabLayout2.setVisibility(View.VISIBLE);
//        }
//    }


}
