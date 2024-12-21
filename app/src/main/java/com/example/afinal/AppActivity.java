package com.example.afinal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class AppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(this));
    }

    private static class MyFragmentPagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments = new ArrayList<>();

        public MyFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            // 添加 HomeFragment 以及其他你想要展示的 Fragment
            fragments.add(new HomeFragment());
            // 如果有更多Fragment，继续添加...
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}