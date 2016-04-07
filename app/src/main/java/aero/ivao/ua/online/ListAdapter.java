package aero.ivao.ua.online;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Дмитрий on 02.04.2016.
 */
public class ListAdapter extends BaseAdapter{
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<atc> objects;

    ListAdapter(Context context, ArrayList<atc> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.atc, parent, false);
        }

        atc p = getProduct(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.pos)).setText(p.Position);
        ((TextView) view.findViewById(R.id.vid)).setText(p.VID);
        ((TextView) view.findViewById(R.id.name)).setText(p.Name);
        return view;
    }

    // товар по позиции
    atc getProduct(int position) {
        return ((atc) getItem(position));
    }

    // содержимое корзины
    ArrayList<atc> getBox() {
        ArrayList<atc> box = new ArrayList<atc>();
        for (atc p : objects) {
            box.add(p);
        }
        return box;
    }
}
