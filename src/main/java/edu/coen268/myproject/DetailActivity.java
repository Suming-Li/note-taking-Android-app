package edu.coen268.myproject;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


// Show detail views
public class DetailActivity extends AppCompatActivity {

    private String photoPath = "";
    private int rowId;
    private DBAdapter myDb;

    private ImageView image;
    private EditText title, note;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        image = (ImageView) findViewById(R.id.d_image);
        title = (EditText) findViewById(R.id.d_title);
        note = (EditText) findViewById(R.id.d_note);

        Bundle showData = getIntent().getExtras();
        rowId = showData.getInt("keyId");

        openDB();
        Cursor c = myDb.getRow(rowId);
        if (c.moveToFirst()) {
            do {
                title.setText(c.getString(1));
                note.setText(c.getString(2));

                if (!c.getString(4).isEmpty()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    Bitmap bmp = BitmapFactory.decodeFile(c.getString(4), options);
                    image.setImageBitmap(bmp);
                    //Log.d("test", "getSketch:" + c.getString(4) + ":");
                }

                if (!c.getString(3).isEmpty()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    Bitmap bmp = BitmapFactory.decodeFile(c.getString(3), options);
                    image.setImageBitmap(bmp);
                    //Log.d("test", "getPhoto:" + c.getString(3) + ":");
                    photoPath = c.getString(3);
                }

            } while (c.moveToNext());
        }
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }


    //Take a photo
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
                startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //File image = new File(storageDir, imageFileName);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        photoPath = image.getAbsolutePath();
        return image;
    }


    //Choose a photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri selectedImageUri = data.getData();
            String[] projection = { MediaStore.MediaColumns.DATA };
            CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
            Cursor cursor =cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            photoPath = cursor.getString(column_index);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            String saveDate = new SimpleDateFormat("MM'/'dd'/'yyyy").format(new Date());
            openDB();
            myDb.updateNote(rowId, title.getText().toString(), note.getText().toString(), photoPath, saveDate);
            finish();
            return true;
        }

        if (id == R.id.action_share) {
            //Uri uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/" + "ic_launcher");
            //Uri uri = Uri.parse("file:///sdcard/image.jpg");
            //Uri uri = Uri.fromFile(new File("android.resource://edu.coen268.myproject/drawable/ic_launcher.jpg"));
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
            //sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(sendIntent, "Share To"));
            return true;
        }

        if (id == R.id.take_photo) {
            dispatchTakePictureIntent();
            return true;
        }

        if (id == R.id.choose_photo){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

        }

        return super.onOptionsItemSelected(item);
    }
}
