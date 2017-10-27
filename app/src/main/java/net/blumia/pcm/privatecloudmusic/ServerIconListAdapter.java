package net.blumia.pcm.privatecloudmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by wzc78 on 2017/10/26.
 */

public class ServerIconListAdapter extends BaseAdapter {

    LayoutInflater mLayoutInflater;
    ArrayList<PCMServerInfo> mInfoArrayList;

    public ServerIconListAdapter(Context context, ArrayList<PCMServerInfo> infolist) {
        super();

        mLayoutInflater = LayoutInflater.from(context);
        mInfoArrayList = infolist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = mLayoutInflater.inflate(R.layout.drawer_server_icon_item, null);
        ViewHolder mViewHolder = new ViewHolder();
        mViewHolder.mImageView = view.findViewById(R.id.iv_server_icon);

        return view;
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

    class ViewHolder {
        ImageView mImageView;
    }
}
