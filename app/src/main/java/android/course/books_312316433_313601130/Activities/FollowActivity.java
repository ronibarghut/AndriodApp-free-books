package android.course.books_312316433_313601130.Activities;

import android.course.books_312316433_313601130.Fragments.FollowFragment;
import android.course.books_312316433_313601130.R;
import android.course.books_312316433_313601130.Utils.Constants;
import android.course.books_312316433_313601130.Fragments.Listeners.DataLoadingListener;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;

public class FollowActivity extends AppCompatActivity implements  DataLoadingListener {
    private String username;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters_list);
        username = getIntent().getStringExtra(Constants.USERNAME);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(R.string.socials);

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));
        TabLayout tabs = (TabLayout) findViewById(R.id.mainFragmentTabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class PageAdapter extends FragmentPagerAdapter {
        private final String[] header = {"Following", "Followers"};

        public PageAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0)
                return FollowFragment.newInstance(FollowFragment.FOLLOWING, username);
            else if (i == 1)
                return FollowFragment.newInstance(FollowFragment.FOLLOWERS, username);
            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return header[position];
        }

        @Override
        public int getCount() {
            return header.length;
        }
    }

    @Override
    public void onLoadingStart() {
        ProgressBar progressBar = toolbar.findViewById(R.id.chaptersBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

    }

    @Override
    public void onnLoadingDone() {
        ProgressBar progressBar = toolbar.findViewById(R.id.chaptersBar);
        progressBar.setVisibility(ProgressBar.GONE);


    }
}
