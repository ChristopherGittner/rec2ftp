package org.gittner.rec2ftp.common;


import org.gittner.rec2ftp.R;

import android.view.View;

public class FtpLog{

    /* Progressbar */
    int pBarProgress_;
    boolean pBarIndeterminate_;
    int pBarVisibility_;

    /* Log TextView */
    String logText_;
    int logTextVisibility_;

    /* Filename TextView */
    String filename_;

    /* Status Icon */
    int statusIcon_;

    /* General */
    boolean completed_;
    boolean failed_;

    /* We need the logAdapter to which this Log belongs to invoke notifyDataSetChanged */
    LogAdapter adapter_;

    public FtpLog(String filename, LogAdapter adapter) {

        pBarProgress_ = 0;
        pBarIndeterminate_ = true;
        pBarVisibility_ = View.VISIBLE;

        logText_ = "";
        logTextVisibility_ = View.GONE;

        filename_ = filename;

        statusIcon_ = R.drawable.ic_upload;

        completed_ = false;
        failed_ = false;

        adapter_ = adapter;
    }

    private void update() {
        adapter_.notifyDataSetChanged();
    }

    public void setProgress(int progress) {
        pBarProgress_ = progress;
        update();
    }

    public int getProgress() {
        return pBarProgress_;
    }

    public void setIndeterminate(boolean indeterminate) {
        pBarIndeterminate_ = indeterminate;
        update();
    }

    public boolean getIndeterminate() {
        return pBarIndeterminate_;
    }

    public void setProgressBarVisibility(int visibility) {
        pBarVisibility_ = visibility;
    }

    public int getProgressBarVisibility() {
        return pBarVisibility_;
    }

    public void appendTextToLog(String text) {
        logText_ += text;
        update();
    }

    public String getLogText() {
        return logText_;
    }

    public void setLogTextVisibility(int show) {
        logTextVisibility_ = show;
        update();
    }

    public int getLogTextVisibility() {
        return logTextVisibility_;
    }

    public void setFilename(String filename) {
        filename_ = filename;
        update();
    }

    public String getFilename() {
        return filename_;
    }

    public int getStatusIcon() {
        return statusIcon_;
    }

    public void setCompleted() {
        completed_ = true;
        failed_ = false;
        pBarVisibility_ = View.GONE;
        statusIcon_ = R.drawable.ic_ok;
        update();
    }

    public boolean isCompleted() {
        return completed_;
    }

    public void setFailed() {
        completed_ = false;
        failed_ = true;
        pBarVisibility_ = View.GONE;
        statusIcon_ = R.drawable.ic_cancel;
        update();
    }

    public boolean isFailed() {
        return failed_;
    }
}
