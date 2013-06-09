package org.gittner.rec2ftp.activities;

import org.gittner.rec2ftp.R;
import org.gittner.rec2ftp.common.FtpLog;
import org.gittner.rec2ftp.common.LogAdapter;
import org.gittner.rec2ftp.statics.SettingsManager;
import org.gittner.rec2ftp.tasks.UploadTask;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Rec2ftpActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener {

    public static final int REQCODEPICTURE = 1;
    public static final int REQCODEVIDEO = 2;
    public static final int REQCODEAUDIO = 3;
    public static final int REQCODETEXT = 4;

    private File tmpFile_;

    private LogAdapter logAdapter_;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /* Init the Static classes */
        SettingsManager.init(getApplicationContext());

        /* Setup the 4 main Buttons */
        ((ImageButton)findViewById(R.id.btnPicture)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.btnVideo)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.btnAudio)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.btnText)).setOnClickListener(this);

        logAdapter_ = new LogAdapter(this);
        logAdapter_.setNotifyOnChange(true);

        ListView lvLog = (ListView) findViewById(R.id.listvUploads);
        lvLog.setAdapter(logAdapter_);
        lvLog.setOnItemClickListener(this);

        lvLog.setOnItemLongClickListener(this);

        /* If this is the first run then Open Settings Activity */
        if(SettingsManager.IsFirstLaunch()){
            Intent i = new Intent(this, SettingsActivity.class);
            Toast.makeText(this, getString(R.string.please_enter_data_in_the_server_section), Toast.LENGTH_LONG).show();
            startActivity(i);
            SettingsManager.setFirstLaunch(false);
        }

        /* Check if Autorun is enabled and launch corresponding Activity */
        if(SettingsManager.getAutorunOption().equals("picture")){
            startPictureActivity();
        }
        else if(SettingsManager.getAutorunOption().equals("video")){
            startVideoActivity();
        }
        else if(SettingsManager.getAutorunOption().equals("audio")){
            startAudioActivity();
        }
        else if(SettingsManager.getAutorunOption().equals("text")){
            startTextActivity();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPicture:
                startPictureActivity();
                break;
            case R.id.btnVideo:
                startVideoActivity();
                break;
            case R.id.btnAudio:
                startAudioActivity();
                break;
            case R.id.btnText:
                startTextActivity();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQCODEPICTURE:
                /* Process a Picture */
                if(resultCode == Activity.RESULT_OK && tmpFile_ != null){
                    new UploadTask(this, logAdapter_, tmpFile_, SettingsManager.getDirectory(), getTargetFilename(), ".jpg").execute();
                }

                break;
            case REQCODEVIDEO:
                /* Process a Video */

                /* If the Video Capture Tweak is activated, copy the returned file into the Temp file */
                if(resultCode == Activity.RESULT_OK && tmpFile_ != null){
                    if(SettingsManager.getVideoCaptureTweak()){
                        if(data == null)
                            return;

                        try {
                            AssetFileDescriptor asset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                            FileInputStream is = asset.createInputStream();
                            FileOutputStream os = new FileOutputStream(tmpFile_);

                            byte[] buffer = new byte[1024];
                            int len;
                            while((len = is.read(buffer)) > 0)
                                os.write(buffer, 0, len);

                            is.close();
                            os.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }

                    /* Start Video Upload */

                    new UploadTask(this, logAdapter_, tmpFile_, SettingsManager.getDirectory(), getTargetFilename(), ".mpg").execute();
                }

                break;
            case REQCODEAUDIO:
                /* Process an Audio */
                if(resultCode == Activity.RESULT_OK && tmpFile_ != null){
                    new UploadTask(this, logAdapter_, tmpFile_, SettingsManager.getDirectory(), getTargetFilename(), ".m4a").execute();
                }

                break;
            case REQCODETEXT:
                /* Process a Text */
                if(resultCode == Activity.RESULT_OK && tmpFile_ != null){
                    new UploadTask(this, logAdapter_, tmpFile_, SettingsManager.getDirectory(), getTargetFilename(), ".txt").execute();
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_settings:
                /* Start the Settings Activity */
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_remove_completed_uploads:
                for(int i = logAdapter_.getCount() - 1; i > -1; --i) {
                    FtpLog log = logAdapter_.getItem(i);

                    if(log.isCompleted() || log.isFailed())
                        logAdapter_.remove(log);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FtpLog log = logAdapter_.getItem(position);
        /* Remove the clicked Item if its Log Text is invisible or hide it else*/
        if(log.getLogTextVisibility() == View.GONE){
            /* Only remove if the Upload is completed or has failed*/
            if(log.isCompleted() || log.isFailed())
                logAdapter_.remove(log);
        }
        else
            log.setLogTextVisibility(View.GONE);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        /* Toggle visibility of the Log Text */
        FtpLog log = logAdapter_.getItem(position);

        if(log.getLogTextVisibility() == View.GONE)
            log.setLogTextVisibility(View.VISIBLE);
        else
            log.setLogTextVisibility(View.GONE);

        return true;
    }

    private String getTargetFilename() {

        /* Returns the current Timestamp as Filename or the chosen Prefix */
        if(SettingsManager.getFilePrefix().equals("")){
            Time t = new Time();
            t.setJulianDay(Time.EPOCH_JULIAN_DAY);
            t.setToNow();
            return t.format("%Y%m%d%H%M%S");
        }
        else
            return SettingsManager.getFilePrefix();
    }

    private void createTempFile() {
        /* Create a Temporary File for the Camera Intent */
        try {
            tmpFile_ = File.createTempFile("tmp", null);
        } catch (IOException e) {
            Log.e("", e.getMessage());
            return;
        }

        /* The Temporary File needs to be Write and Readable for the Camera Intent */
        tmpFile_.setWritable(true, false);
        tmpFile_.setReadable(true, false);
    }

    private void startPictureActivity() {
        createTempFile();

        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile_));

        startActivityForResult(i, REQCODEPICTURE);
    }

    private void startVideoActivity() {
        createTempFile();

        Intent i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

        /* Set the Output Extra only if the Tweak is not activated */
        if(!SettingsManager.getVideoCaptureTweak())
            i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile_));

        i.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);

        startActivityForResult(i, REQCODEVIDEO);
    }

    private void startAudioActivity() {
        createTempFile();

        Intent i = new Intent(this, AudioRecordActivity.class);
        i.putExtra(AudioRecordActivity.EXTRA_OUTPUT, tmpFile_.getAbsolutePath());

        startActivityForResult(i, REQCODEAUDIO);
    }

    private void startTextActivity() {
        createTempFile();

        Intent i = new Intent(this, TextEditActivity.class);
        i.putExtra(TextEditActivity.EXTRA_OUTPUT, tmpFile_.getAbsolutePath());

        startActivityForResult(i, REQCODETEXT);
    }
}