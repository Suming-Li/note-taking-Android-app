package edu.coen268.myproject.sketch;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.coen268.myproject.DBAdapter;
import edu.coen268.myproject.R;

public class SketchActivity extends AppCompatActivity {
    private String note = "";
    private String photoPath = "";
    private String sketchPath = "";
    private String flag = "flag_unlock";
    private DBAdapter myDb;
    private EditText title;
    private SketchView sv;
    String TAG = "Bitmap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (EditText) findViewById(R.id.title_sketch);
        sv = (SketchView) findViewById(R.id.draw);
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void storeBitmap(Bitmap bm) {
        File bitmapFile = null;
        try {
            bitmapFile = createBitmapFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (bitmapFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(bitmapFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Bitmap", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Bitmap", "Error accessing file: " + e.getMessage());
        }
    }

    private File createBitmapFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".png", storageDir);
        sketchPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sketch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        SketchView sv = (SketchView)findViewById(R.id.draw);
        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.stroke_1:
                sv.paint.setStrokeWidth(2);
                break;
            case R.id.stroke_2:
                sv.paint.setStrokeWidth(10);
                break;
            case R.id.green:
                sv.paint.setColor(Color.GREEN);
                item.setChecked(true);
                break;
            case R.id.red:
                sv.paint.setColor(Color.RED);
                item.setChecked(true);
                break;
            case R.id.blue:
                sv.paint.setColor(Color.BLUE);
                item.setChecked(true);
                break;
            case R.id.yellow:
                sv.paint.setColor(Color.YELLOW);
                item.setChecked(true);
                break;
            case R.id.black:
                sv.paint.setColor(Color.BLACK);
                item.setChecked(true);
                break;
        }


        if (id == R.id.sketch_done) {
            storeBitmap(sv.cacheBitmap);
            String saveDate = new SimpleDateFormat("MM'/'dd'/'yyyy").format(new Date());
            openDB();
            Log.d("test", "Sketch:" + photoPath + ": :" + sketchPath + ":");
            myDb.addNote(title.getText().toString(), note, photoPath, sketchPath, saveDate, flag);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
