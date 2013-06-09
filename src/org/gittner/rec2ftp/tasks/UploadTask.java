package org.gittner.rec2ftp.tasks;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.gittner.rec2ftp.R;
import org.gittner.rec2ftp.common.FtpLog;
import org.gittner.rec2ftp.common.LogAdapter;
import org.gittner.rec2ftp.statics.SettingsManager;
import org.gittner.rec2ftp.tasks.ProgressData.Action;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class ProgressData {
    static enum Action{
        ACTION_MESSAGE,
        ACTION_PROGRESS,
        ACTION_INDETERMINATE,
        ACTION_FILENAME
    };

    Action action_;
    private String message_;
    private String filename_;
    private int progress_;
    private boolean indeterminate_;

    /* Optionally we can set another Filename by supplying an Action */
    public ProgressData(String message, Action action) {
        if(action == Action.ACTION_FILENAME){
            action_ = Action.ACTION_FILENAME;
            filename_ = message;
        }
        else {
            action_ = Action.ACTION_MESSAGE;
            message_ = message;
        }
    }

    public ProgressData(String message) {
        action_ = Action.ACTION_MESSAGE;
        message_ = message;
    }

    public ProgressData(int progress) {
        action_ = Action.ACTION_PROGRESS;
        progress_ = progress;
    }

    public ProgressData(boolean indeterminate) {
        action_ = Action.ACTION_INDETERMINATE;
        indeterminate_ = indeterminate;
    }

    public Action getAction() {
        return action_;
    }

    public String getMessage() {
        if(action_ == Action.ACTION_MESSAGE)
            return message_;

        return "";
    }

    public int getProgress() {
        if(action_ == Action.ACTION_PROGRESS)
            return progress_;

        return 0;
    }

    public boolean getIndeterminate() {
        if(action_ == Action.ACTION_INDETERMINATE)
            return indeterminate_;

        return true;
    }

    public String getFilename() {
        if(action_ == Action.ACTION_FILENAME)
            return filename_;

        return "";
    }
};

public class UploadTask extends AsyncTask<Void, ProgressData, Boolean> {

    FtpLog log_;
    LogAdapter adapter_;

    private Context context_;

    File tmpFile_;
    String dstDirectory_;
    String dstFilename_;
    String extension_;

    /* Mutex for the Time between retrieving the Directory and Creating the File
     * to prevent Overwriting of Indexed Files due to connection delay
     */
    static final Object mutex_ = new Object();

    public UploadTask(Context context, LogAdapter adapter, File f, String directory, String filename, String extension){
        context_ = context;
        log_ = new FtpLog(filename + extension, adapter);
        adapter_ = adapter;
        tmpFile_ = f;
        dstDirectory_ = directory;
        dstFilename_ = filename;
        extension_ = extension;
    }

    @Override
    protected void onPreExecute() {

        /* Add the FtpLog to the ArrayAdapter so it will get displayed */
        adapter_.add(log_);
    }

    @Override
    protected Boolean doInBackground(Void... v) {
        if(tmpFile_ == null)
            return false;

        /* Create a new FTP Client and register callbacks for Logging and Progress Display*/
        FTPClient client = new FTPClient();
        client.addProtocolCommandListener(new ProtocolCommandListener(){

            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                publishProgress(new ProgressData("--> " + event.getMessage()));
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                publishProgress(new ProgressData("<-- " + event.getMessage()));
            }});

        client.setCopyStreamListener(new CopyStreamListener(){

            @Override
            public void bytesTransferred(CopyStreamEvent arg0) {

            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                /* Update the Progress Bar */
                publishProgress(new ProgressData((int)((double)totalBytesTransferred / (double)tmpFile_.length() * 100.0)));
            }});

        /* Catch all Exception during upload */
        try {
            synchronized(mutex_) {

                publishProgress(new ProgressData(context_.getString(R.string.starting_upload) + "\n"));

                /* Try to Conenct to the Server. If this fails an Exception is thrown */
                client.connect(SettingsManager.getHost(), SettingsManager.getPort());

                /* Try to Login */
                if(!client.login(SettingsManager.getUsername(), SettingsManager.getPassword())){
                    publishProgress(new ProgressData(context_.getString(R.string.failed_to_login)));
                    client.disconnect();
                    return false;
                }

                client.enterLocalPassiveMode();

                /* Try to switch to Binary Filetype */
                if(!client.setFileType(FTPClient.BINARY_FILE_TYPE)) {
                    publishProgress(new ProgressData(context_.getString(R.string.failed_to_set_binary_filetype)));
                    client.logout();
                    client.disconnect();
                    return false;
                }

                /* Check if we need to switch the Directory */
                if(!SettingsManager.getDirectory().equals("")) {
                    if(!client.changeWorkingDirectory(SettingsManager.getDirectory())) {
                        publishProgress(new ProgressData(context_.getString(R.string.failed_to_set_working_directory)));
                        client.logout();
                        client.disconnect();
                        return false;
                    }
                }

                /* If we need to append an Index we first check for the next free one */
                if(SettingsManager.getAppendIndex() && !SettingsManager.getFilePrefix().equals("")){
                    FTPFile[] files = client.listFiles(".");

                    int index = 0;
                    boolean done = false;

                    while(!done) {
                        done = true;
                        for(FTPFile file : files){
                            if(file.getName().equals(dstFilename_ + "-" + index + extension_)){
                                ++index;
                                done = false;
                            }
                        }
                    }

                    dstFilename_ += "-" + index;
                }

                /* Merge filename and Extension */
                dstFilename_ += extension_;
                publishProgress(new ProgressData(dstFilename_, Action.ACTION_FILENAME));

                /* Create the File to prevent Overwriting of Files due to doubled Prefixes */
                if(!client.storeFile(dstFilename_, new ByteArrayInputStream(new byte[0]))) {
                    publishProgress(new ProgressData(context_.getString(R.string.failed_to_upload_file)));
                    client.logout();
                    client.disconnect();
                    return false;
                }

            } // End Of synchronized
            /* Upload the File */
            publishProgress(new ProgressData(0), new ProgressData(false));

            FileInputStream is = new FileInputStream(tmpFile_);

            if(!client.storeFile(dstFilename_, is)) {
                publishProgress(new ProgressData(context_.getString(R.string.failed_to_upload_file)));
                client.logout();
                client.disconnect();
                is.close();
                return false;
            }

            is.close();

            /* Set to Indeterminate as long as the upload not completed i.e. Chmodded and Disconnected */
            publishProgress(new ProgressData(100), new ProgressData(true));

            /* Check if we need to chmod the files */
            if(!SettingsManager.getRights().equals("")) {
                if(!client.doCommand("SITE CHMOD", SettingsManager.getRights() + " " +  dstFilename_)){
                    publishProgress(new ProgressData(context_.getString(R.string.failed_to_set_file_permissions)));
                    client.logout();
                    client.disconnect();
                    return false;
                }
            }

            /* Logout and Disconnect from Server */
            client.logout();
            client.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            publishProgress(new ProgressData(e.getMessage()));
            return false;
        }

        publishProgress(new ProgressData(context_.getString(R.string.upload_successfull) + "\n"));

        return true;
    }

    @Override
    protected void onProgressUpdate(ProgressData... dataArray) {

        for(ProgressData data : dataArray) {
            if(data.getAction() == ProgressData.Action.ACTION_MESSAGE)
                log_.appendTextToLog(data.getMessage());

            if(data.getAction() == ProgressData.Action.ACTION_PROGRESS)
                log_.setProgress(data.getProgress());

            if(data.getAction() == ProgressData.Action.ACTION_INDETERMINATE)
                log_.setIndeterminate(data.getIndeterminate());

            if(data.getAction() == ProgressData.Action.ACTION_FILENAME)
                log_.setFilename(data.getFilename());
        }
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(tmpFile_ != null)
            tmpFile_.delete();

        /* Set Upload state to completed or failed */
        log_.setProgress(100);
        if(result)
            log_.setCompleted();
        else
            log_.setFailed();

        /* Remove this Upload if selected in Settings */
        if(SettingsManager.getRemoveSuccessfullUploads() && result == true)
            adapter_.remove(log_);
    }
}
