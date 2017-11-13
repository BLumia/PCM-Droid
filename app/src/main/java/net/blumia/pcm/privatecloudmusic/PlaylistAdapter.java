package net.blumia.pcm.privatecloudmusic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wzc78 on 2017/11/2.
 */

public class PlaylistAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    public void setInfoArrayList(ArrayList<MusicItem> infoArrayList) {
        mInfoArrayList = infoArrayList;
    }
    private ArrayList<MusicItem> mInfoArrayList;

    public PlaylistAdapter(Context context, ArrayList<MusicItem> infolist) {
        super();

        mLayoutInflater = LayoutInflater.from(context);
        mInfoArrayList = infolist;
    }

    @Override
    public int getCount() {
        return mInfoArrayList.size();
    }

    @Override
    public MusicItem getItem(int position) {
        return mInfoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaylistAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_file_item, null);
            convertView.setTag(holder);
        } else {
            holder = (PlaylistAdapter.ViewHolder) convertView.getTag();
        }

        holder.mTextView = convertView.findViewById(R.id.tv_music_item);
        if (getItem(position) != null && !getItem(position).fileName.isEmpty()) {
            holder.mTextView.setText(getItem(position).fileName);
            Log.d("TAG", getItem(position).fileName);
        }


        return convertView;
    }

    class ViewHolder {
        TextView mTextView;
    }
}
