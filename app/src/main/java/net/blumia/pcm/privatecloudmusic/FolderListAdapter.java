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
 * Created by wzc78 on 2017/10/31.
 */

public class FolderListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    public void setInfoArrayList(ArrayList<MusicListInfo> infoArrayList) {
        mInfoArrayList = infoArrayList;
    }

    private ArrayList<MusicListInfo> mInfoArrayList;

    public FolderListAdapter(Context context, ArrayList<MusicListInfo> infolist) {
        super();

        mLayoutInflater = LayoutInflater.from(context);
        mInfoArrayList = infolist;
    }

    @Override
    public int getCount() {
        return mInfoArrayList.size();
    }

    @Override
    public MusicListInfo getItem(int position) {
        return mInfoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FolderListAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.drawer_folderlist_item, null);
            convertView.setTag(holder);
        } else {
            holder = (FolderListAdapter.ViewHolder) convertView.getTag();
        }

        holder.mTextView = convertView.findViewById(R.id.drawer_folderlist_item);
        if (getItem(position) != null && !getItem(position).displayName.isEmpty()) {
            holder.mTextView.setText(getItem(position).displayName);
        }

        return convertView;
    }

    class ViewHolder {
        TextView mTextView;
    }
}
