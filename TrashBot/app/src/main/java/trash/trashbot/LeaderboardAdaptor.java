package trash.trashbot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class LeaderboardAdaptor extends BaseAdapter {

    private LinkedList<LeaderboardUser> userList;
    private Context context;

    public LeaderboardAdaptor(LinkedList<LeaderboardUser> list, Context context) {
        this.userList = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context)
                    .inflate(R.layout.leaderboard_item_layout, parent, false);
        ImageView image = (ImageView)convertView.findViewById(R.id.userImage);
        TextView name = (TextView)convertView.findViewById(R.id.userName);
        TextView carbonpoints = (TextView)convertView.findViewById(R.id.userCarbonpoints);
        image.setBackgroundResource(userList.get(position).getIcon());
        name.setText(userList.get(position).getUsername());
        carbonpoints.setText(userList.get(position).getCarbonPoints());
        return convertView;
    }
}
