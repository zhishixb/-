package com.example.afinal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.afinal.application.MyApplication;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        MyApplication myApp = (MyApplication) getApplication();

        // 初始化ViewPager2和BottomNavigationView
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 创建并设置适配器
        MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // 设置BottomNavigationView的默认选中项以匹配初始页面
        bottomNav.setSelectedItemId(R.id.navigation_home); // 或者根据需要选择其他项

        // 处理BottomNavigationView项的选择，以改变ViewPager2的当前页面
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                viewPager.setCurrentItem(0, false); // 禁用动画
                return true;
            } else if (item.getItemId() == R.id.navigation_post) {
                viewPager.setCurrentItem(1, false); // 禁用动画
                return true;
            }else if (item.getItemId() == R.id.navigation_space){
                viewPager.setCurrentItem(2, false); // 禁用动画
                return true;
            }
            return false;
        });

        // 当用户滑动ViewPager2时同步更新BottomNavigationView的选中状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNav.setSelectedItemId(R.id.navigation_home);
                } else if (position == 1) {
                    bottomNav.setSelectedItemId(R.id.navigation_post);
                }else if (position == 2) {
                    bottomNav.setSelectedItemId(R.id.navigation_space);
                }
            }
        });
    }

    // 定义一个内部静态类来作为ViewPager2的适配器
    private static class MyFragmentPagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments = new ArrayList<>();

        public MyFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            // 添加要显示的片段到列表中
            fragments.add(new HomeFragment());
            fragments.add(new PostFragment());
            fragments.add(new SpaceFragment());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // 根据位置返回对应的片段实例
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            // 返回片段的数量
            return fragments.size();
        }
    }
}