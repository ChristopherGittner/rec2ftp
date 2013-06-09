package org.gittner.rec2ftp.common;

import org.gittner.rec2ftp.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class LogAdapter extends ArrayAdapter<FtpLog> {

    Context context_;

    public LogAdapter(Context context) {
        super(context, R.layout.logview);
        context_ = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getItem(position).getView();

        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.logview, null, false);
        }

        FtpLog log = super.getItem(position);

        ProgressBar pBar = (ProgressBar) view.findViewById(R.id.pBar);
        pBar.setIndeterminate(log.getIndeterminate());
        pBar.setProgress(log.getProgress());
        pBar.setVisibility(log.getProgressBarVisibility());

        TextView txtvLogText = (TextView) view.findViewById(R.id.txtvLogText);
        txtvLogText.setText(log.getLogText());
        txtvLogText.setVisibility(log.getLogTextVisibility());

        TextView txtvFilename = (TextView) view.findViewById(R.id.txtvFilename);
        txtvFilename.setText(log.getFilename());

        ImageView statusIcon = (ImageView) view.findViewById(R.id.imgvStatus);
        statusIcon.setImageDrawable(context_.getResources().getDrawable(log.getStatusIcon()));

        return view;
    }
}
