package trash.trashbot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.LinkedList;

public class LeaderboardActivity extends AppCompatActivity {

    private LinkedList<LeaderboardUser> userList;
    private Context context;
    private LeaderboardAdaptor adapter;
    private ListView listView;

    // create pages
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_leaderboard);

        // create pages
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_profile);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_leaderboard);

        /*
        context = LeaderboardActivity.this;
        listView = (ListView) findViewById(R.id.leaderView);
        userList = new LinkedList<LeaderboardUser>();
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        adapter = new LeaderboardAdaptor(userList, context);
        listView.setAdapter(adapter);
        */
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ProfileFragment());
        adapter.addFragment(new LeaderboardFragment());
        viewPager.setAdapter(adapter);
    }
}
