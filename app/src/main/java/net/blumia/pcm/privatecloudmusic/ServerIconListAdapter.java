package net.blumia.pcm.privatecloudmusic;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by wzc78 on 2017/10/26.
 */

public class ServerIconListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private ArrayList<PCMServerInfo> mInfoArrayList;
    private int mSelectedIndex;

    public ServerIconListAdapter(Context context, ArrayList<PCMServerInfo> infolist) {
        super();

        mLayoutInflater = LayoutInflater.from(context);
        mInfoArrayList = infolist;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.drawer_server_icon_item, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
/*
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "icon listview onClick: " + position);
            }
        });
*/
        holder.mImageView = convertView.findViewById(R.id.iv_server_icon);
        if (position == mSelectedIndex) {
            holder.mImageView.setBackgroundColor(Color.BLACK);
        } else {
            holder.mImageView.setBackground(
                    mLayoutInflater
                            .getContext()
                            .getResources()
                            .getDrawable(R.drawable.circle)
            );
        }

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return mInfoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mInfoArrayList.size();
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
    }

    class ViewHolder {
        ImageView mImageView;
    }
}
