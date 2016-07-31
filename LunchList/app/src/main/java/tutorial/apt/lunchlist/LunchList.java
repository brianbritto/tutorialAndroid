package tutorial.apt.lunchlist;

import android.app.ListActivity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import tutorial.apt.lunchlist.R;
import tutorial.apt.lunchlist.RestaurantHelper;

public class LunchList extends ListActivity {
    Cursor model = null;
    RestaurantAdapter adapter = null;
    EditText name = null;
    EditText address = null;
    EditText notes = null;
    RadioGroup types = null;
    RestaurantHelper helper = null;
    public final static String ID_EXTRA="tutorial.apt.lunchlist._ID";
    SharedPreferences prefs =  null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch_list);

        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        helper=new RestaurantHelper(this);
        initList();
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    private void initList() {
        if (model!=null) {
            stopManagingCursor(model);
            model.close();
        }
        model=helper.getAll(prefs.getString("sort_order", "name"));
        startManagingCursor(model);
        adapter=new RestaurantAdapter(model);
        setListAdapter(adapter);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("sort_order")) {
                initList();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        helper.close();
    }

    private View.OnClickListener onSave = new View.OnClickListener() {
        public void onClick(View v) {
            String type = null;
            switch (types.getCheckedRadioButtonId()) {
                case R.id.sit_down:
                    type = "sit_down";
                    break;
                case R.id.take_out:
                    type = "take_out";
                    break;
                case R.id.delivery:
                    type = "delivery";
                    break;
            }
            helper.insert(name.getText().toString(),
                    address.getText().toString(), type,
                    notes.getText().toString(),null);//prefs.getString("feed","feed")
            model.requery();
        }
    };

    @Override
    public void onListItemClick(ListView list, View view,
                                int position, long id) {
        Intent i=new Intent(LunchList.this, DetailForm.class);
        i.putExtra(ID_EXTRA, String.valueOf(id));
        startActivity(i);
    }

    class RestaurantAdapter extends CursorAdapter {
        RestaurantAdapter(Cursor c) {
            super(LunchList.this, c);
        }

        @Override
        public void bindView(View row, Context ctxt,
                             Cursor c) {
            RestaurantHolder holder = (RestaurantHolder) row.getTag();
            holder.populateFrom(c, helper);
        }

        @Override
        public View newView(Context ctxt, Cursor c,
                            ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.row, parent, false);
            RestaurantHolder holder = new RestaurantHolder(row);
            row.setTag(holder);
            return (row);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.option, menu);
        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.add) {
            startActivity(new Intent(LunchList.this, DetailForm.class));
            return(true);
        }
        else if (item.getItemId()==R.id.prefs) {
            startActivity(new Intent(this, EditPreferences.class));
            return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    static class RestaurantHolder {
        private TextView name = null;
        private TextView address = null;
        private ImageView icon = null;

        RestaurantHolder(View row) {
            name = (TextView) row.findViewById(R.id.title);
            address = (TextView) row.findViewById(R.id.address);
            icon = (ImageView) row.findViewById(R.id.icon);
        }

        void populateFrom(Cursor c, RestaurantHelper helper) {
            name.setText(helper.getName(c));
            address.setText(helper.getAddress(c));
            if (helper.getType(c).equals("sit_down")) {
                icon.setImageResource(R.drawable.ball_red);
            } else if (helper.getType(c).equals("take_out")) {
                icon.setImageResource(R.drawable.ball_yellow);
            } else {
                icon.setImageResource(R.drawable.ball_green);
            }
        }
    }
}