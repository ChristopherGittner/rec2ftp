package org.gittner.rec2ftp.activities;

import org.gittner.rec2ftp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TextEditActivity extends Activity implements OnClickListener{

    public static final String EXTRA_OUTPUT = "EXTRA_OUTPUT";

    private File tmpFile_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textedit);

        /* Create the temp File */
        tmpFile_ = new File(getIntent().getExtras().getString(EXTRA_OUTPUT));

        /* Setup the Buttons */
        ((ImageButton) findViewById(R.id.btnOk)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.btnCancel)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnOk:

                /* Write the Text into the Temporary File */
                try {
                    FileOutputStream os = new FileOutputStream(tmpFile_);

                    EditText edttxtText = (EditText) findViewById(R.id.edttxtText);
                    os.write(edttxtText.getText().toString().getBytes(), 0, edttxtText.length());

                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }

                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btnCancel:

                /* Ask if the User really wants to cancel and do so if the answer is yes */
                AlertDialog.Builder confirmation = new AlertDialog.Builder(this);

                confirmation.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }});

                confirmation.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextEditActivity.this.setResult(RESULT_CANCELED);
                        TextEditActivity.this.finish();
                    }});

                confirmation.setTitle(getString(R.string.really_abort_question));
                confirmation.show();
                break;
        }
    }
}
