package edu.coen268.myproject.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.coen268.myproject.DBAdapter;
import edu.coen268.myproject.R;

/* Forget this class
I give up this class because I finally move the feature of taking/choosing photo to NoteActivity class.
 */

public class PhotoActivity extends AppCompatActivity {
    private String photoPath = "";
    private String sketchPath = "";
    private String flag = "flag_unlock";
    static final int REQUEST_TAKE_PHOTO = 1;
    private DBAdapter myDb;
    private Button takePhoto, choosePhoto;
    private EditText title, note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        takePhoto = (Button) findViewById(R.id.button_photo);
        choosePhoto = (Button) findViewById(R.id.button_photo2);
        title = (EditText) findViewById(R.id.title_photo);
        note = (EditText) findViewById(R.id.text_photo);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        photoPath = image.getAbsolutePath();
        return image;
    }


    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            String saveDate = new SimpleDateFormat("MM'/'dd'/'yyyy").format(new Date());
            openDB();
            Log.d("test", "Photo:" + photoPath + ": :" + sketchPath + ":");
            myDb.addNote(title.getText().toString(), note.getText().toString(), photoPath, sketchPath, saveDate, flag);
            finish();
            return true;
        }

        if (id == R.id.action_share) {
            Intent i = new Intent(Intent.ACTION_DELETE);
            i.setData(Uri.parse("package:edu.coen268.myproject"));
            startActivity(i);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
