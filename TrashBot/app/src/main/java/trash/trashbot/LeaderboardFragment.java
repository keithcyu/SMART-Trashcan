package trash.trashbot;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";

    private LinkedList<LeaderboardUser> userList;
    private Context context;
    private LeaderboardAdaptor adapter;
    private ListView listView;

    private Button btnTEST;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaderboard_fragment_layout,container,false);


        context = getActivity(); // (Context) LeaderboardFragment.this;
        listView = (ListView) view.findViewById(R.id.leaderView);
        userList = new LinkedList<LeaderboardUser>();
        userList.add(new LeaderboardUser("Trump", "1021 pts", R.drawable.ic_leaderboard_trump));
        userList.add(new LeaderboardUser("Pikachu", "562 pts", R.drawable.ic_leaderboard_pikachu));
        userList.add(new LeaderboardUser("Kim", "308 pts", R.drawable.ic_leaderboard_kim));
        adapter = new LeaderboardAdaptor(userList, context);
        listView.setAdapter(adapter);


        return view;
    }
}
