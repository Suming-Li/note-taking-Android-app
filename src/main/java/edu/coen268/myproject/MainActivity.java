package edu.coen268.myproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import edu.coen268.myproject.note.NoteActivity;
import edu.coen268.myproject.sketch.SketchActivity;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    DBAdapter myDb;
    String lock = "flag_lock";
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView)findViewById(R.id.list_view);
        registerForContextMenu(listView);
    }

    @Override
    public void onResume() {
        super.onResume();
        openDB();
        populateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle passData = new Bundle();
                Cursor listCursor = (Cursor) parent.getItemAtPosition(position);
                final int titleId = listCursor.getInt(listCursor.getColumnIndex(myDb.COLUMN_ID));
                String flag = listCursor.getString(listCursor.getColumnIndex(myDb.COLUMN_FLAG));
                final String pd = listCursor.getString(listCursor.getColumnIndex(myDb.COLUMN_PW));

                if (flag.equals(lock)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Enter Password");

                    final EditText input = new EditText(MainActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);
                    //alertDialog.setIcon(R.drawable.flag_lock);

                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String temp = input.getText().toString();
                                    if (pd.equals(temp)) {
                                        passData.putInt("keyId", titleId);
                                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                        intent.putExtras(passData);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    alertDialog.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog.show();

                } else {
                    passData.putInt("keyId", titleId);
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtras(passData);
                    startActivity(intent);
                }
            }
        });
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void populateListView() {
        Cursor cursor = myDb.getAllRows();
        String[] fromFieldNames = new String[] {DBAdapter.COLUMN_TITLE, DBAdapter.COLUMN_DATE, DBAdapter.COLUMN_FLAG};
        int[] toViewIds = new int[] {R.id.text_view_1, R.id.text_view_2, R.id.image_view};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.row_layout, cursor, fromFieldNames,toViewIds, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            /** Binds the Cursor column defined by the specified index to the specified view */
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.image_view) {
                    ImageView iv = (ImageView) view.findViewById(R.id.image_view);
                    int resID = getApplicationContext().getResources().getIdentifier(cursor.getString(columnIndex), "drawable", getApplicationContext().getPackageName());
                    iv.setImageDrawable(getApplicationContext().getResources().getDrawable(resID));
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_view) {
            menu.setHeaderTitle("Menu");
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final Cursor c = myDb.getRow(info.id);

        int itemId = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[itemId];

        String delete = "Delete";
        String lockWithPd = "Lock";

        //Delete item operation
        if (menuItemName.equals(delete)) {
            if (c.getString(6).equals(lock)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Enter Password");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String temp = input.getText().toString();
                                if (c.getString(7).equals(temp)) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Delete Item")
                                            .setMessage("Are you sure to delete this item?")
                                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    myDb.deleteRow(info.id);
                                                    populateListView();
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .show();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();

            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure to delete this item?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myDb.deleteRow(info.id);
                                populateListView();
                                Log.d("Menu", "onContextItemSelected: delete");
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            return true;
        }

        //Lock item operation
        if (menuItemName.equals(lockWithPd)) {
            if (c.getString(6).equals(lock)) {
                Toast.makeText(MainActivity.this, "Already locked !", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Set Your Password");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                password = input.getText().toString();
                                String flag = "flag_lock";
                                myDb.lockRow(info.id, flag, password);
                                populateListView();
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
            return true;
        }

        return super.onContextItemSelected(item);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_information) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_uninstall) {
            Intent i = new Intent(Intent.ACTION_DELETE);
            i.setData(Uri.parse("package:edu.coen268.myproject"));
            startActivity(i);
            return true;
        }

        if (id == R.id.action_newNote) {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.setData(Uri.parse("package:edu.coen268.myproject.note"));
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_newSketch) {
            Intent intent = new Intent(this, SketchActivity.class);
            intent.setData(Uri.parse("package:edu.coen268.myproject.sketch"));
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_newFolder) {
            return true;
        }

        if (id == R.id.action_search) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Search");

            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String temp = input.getText().toString();
                            populateListView(myDb.getSearchRows(temp));
                        }
                    });

            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateListView(Cursor cursor) {
        String[] fromFieldNames = new String[] {DBAdapter.COLUMN_TITLE, DBAdapter.COLUMN_DATE, DBAdapter.COLUMN_FLAG};
        int[] toViewIds = new int[] {R.id.text_view_1, R.id.text_view_2, R.id.image_view};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.row_layout, cursor, fromFieldNames,toViewIds, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            /** Binds the Cursor column defined by the specified index to the specified view */
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.image_view) {
                    Log.d("textt", "int " + cursor.getString(columnIndex));
                    ImageView iv = (ImageView) view.findViewById(R.id.image_view);
                    int resID = getApplicationContext().getResources().getIdentifier(cursor.getString(columnIndex), "drawable", getApplicationContext().getPackageName());
                    Log.d("textt", "setViewValue: " + resID);
                    iv.setImageDrawable(getApplicationContext().getResources().getDrawable(resID));
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);
    }
}
