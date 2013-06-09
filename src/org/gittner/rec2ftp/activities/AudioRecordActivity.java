package org.gittner.rec2ftp.activities;

import org.gittner.rec2ftp.R;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import java.io.IOException;

public class AudioRecordActivity extends Activity implements OnClickListener{

    String tmpFile_;

    public static final String EXTRA_OUTPUT = "EXTRA_OUTPUT";

    MediaRecorder recorder = new MediaRecorder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiorecord);

        /* check if an Output file was Specified */
        if(getIntent().getExtras().getString(EXTRA_OUTPUT).equals("")){
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        tmpFile_ = getIntent().getExtras().getString(EXTRA_OUTPUT);

        /* Setup all Buttons */
        ((ImageButton) findViewById(R.id.btnRecord)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btnStop)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btnStop)).setEnabled(false);
        ((ImageButton) findViewById(R.id.btnPause)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btnPause)).setEnabled(false);
        ((ImageButton) findViewById(R.id.btnCancel)).setOnClickListener(this);

        /* Initialize the recorder */
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(tmpFile_);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnRecord:
                ((ImageButton) findViewById(R.id.btnRecord)).setEnabled(false);
                ((ImageButton) findViewById(R.id.btnRecord)).setImageDrawable(getResources().getDrawable(R.drawable.ic_record_active));
                ((ImageButton) findViewById(R.id.btnCancel)).setEnabled(false);
                ((ImageButton) findViewById(R.id.btnStop)).setEnabled(true);

                try {
                    recorder.prepare();
                    recorder.start();
                }
                catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btnPause:
                //TODO: Make Pause Button Work
                ((ImageButton) findViewById(R.id.btnRecord)).setEnabled(true);
                ((ImageButton) findViewById(R.id.btnCancel)).setEnabled(true);

                recorder.stop();
                break;
            case R.id.btnStop:
                recorder.stop();
                recorder.release();

                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.btnCancel:

                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
    }
}
