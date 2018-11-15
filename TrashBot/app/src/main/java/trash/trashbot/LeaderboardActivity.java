package trash.trashbot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import java.util.LinkedList;

public class LeaderboardActivity extends Activity {

    private LinkedList<LeaderboardUser> userList;
    private Context context;
    private LeaderboardAdaptor adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        context = LeaderboardActivity.this;
        listView = (ListView) findViewById(R.id.leaderView);
        userList = new LinkedList<LeaderboardUser>();
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        userList.add(new LeaderboardUser("Tom", "562pts", R.mipmap.ic_launcher));
        adapter = new LeaderboardAdaptor(userList, context);
        listView.setAdapter(adapter);
    }
}
