package zazxxx.mapad;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * Created by nhapt on 8/23/2016.
 * Danh sách các category nằm bên trái màn hình.
 */
public class MarkerList extends ListFragment {

    String[] values;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        values = getResources().getStringArray(R.array.location_name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        passLocationName(position);
    }

    private void passLocationName(int position) {

        String demo = (String) getListAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("AcID", "from_MarkerList");
        intent.putExtra("name", demo);

        startActivity(intent);
    }
}
