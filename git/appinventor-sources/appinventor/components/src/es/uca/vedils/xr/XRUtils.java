package es.uca.vedils.xr;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.*;


@DesignerComponent(version = 20200915, description = "Component for advance operation with list", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/sharing.png")
@SimpleObject(external = true)

public class XRUtils extends AndroidNonvisibleComponent implements Component {
    private final ComponentContainer container;
    private static Hashtable<Object, Object> data = new Hashtable<>();


    public XRUtils(ComponentContainer container) {
        super(container.$form());
        this.container = container;
    }

    @SimpleFunction
    public YailList ShuffleRowsList(YailList list) {


        List<Object> array_list = new ArrayList<>();
        for (Object i : list.toArray()) {
            array_list.add(i);
        }
        Collections.shuffle(array_list);
        return YailList.makeList(array_list);

    }
    @SimpleFunction
    public YailList ColumnListValues(YailList list,int column_number){

        LinkedHashSet<String> result_list=new LinkedHashSet<String>();

        List<Object> array_list = new ArrayList<>();
        for (Object i : list.toArray()) {
            array_list.add(i);
        }
        for (int i = 0; i < array_list.size(); i++) {

            result_list.add( ((YailList)array_list.get(i)).toStringArray()[column_number-1].toString());


        }
        return YailList.makeList(result_list);
    }

    @SimpleFunction
    public YailList FilterRowList (YailList list,String lang, String room){

        List<Object> result_list = new ArrayList<>();

        List<Object> array_list = new ArrayList<>();
        for (Object i : list.toArray()) {
            array_list.add(i);
        }
        for (int i = 0; i < array_list.size(); i++) {

            if(((YailList)array_list.get(i)).toStringArray()[0].toString().toLowerCase().equals(lang.toLowerCase())&&
                    ((YailList)array_list.get(i)).toStringArray()[2].toString().toLowerCase().equals(room.toLowerCase())){

                result_list.add( array_list.get(i));
            }



        }
        return YailList.makeList(result_list);
    }

    //todo queda pendiente como hace el rellenado de valores de cada fila de forma amigable
    //maybe lanzando un evento para cuando rellene todos los valores, y despues en este rellenar las variables globales
    @SimpleFunction
    public void RowListValues (YailList list,int row_number){
        String lang;
        String concept;
        String room;
        List<Object> start_explain=  new ArrayList<>();
        List<Object> end_explain=  new ArrayList<>();
        List<Object> start_right=  new ArrayList<>();
        List<Object> end_right=  new ArrayList<>();
        List<Object> start_wrong=  new ArrayList<>();
        List<Object> end_wrong =  new ArrayList<>();
        List<Object> start_question=  new ArrayList<>();
        List<Object> end_question=  new ArrayList<>();
        String video_url;


        List<Object> array_list = new ArrayList<>();
        for (Object i : list.toArray()) {
            array_list.add(i);
        }


        lang=((YailList) array_list.get(row_number-1)).toStringArray()[0].toString();
        concept=((YailList) array_list.get(row_number-1)).toStringArray()[1].toString();
        room=((YailList) array_list.get(row_number-1)).toStringArray()[2].toString();
        start_explain=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[3].toString());
        end_explain=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[4].toString());
        start_right=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[5].toString());
        end_right=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[6].toString());
        start_wrong=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[7].toString());
        end_wrong=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[8].toString());
        start_question=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[9].toString());
        end_question=multiValuesField(((YailList) array_list.get(row_number-1)).toStringArray()[10].toString());
        video_url=((YailList) array_list.get(row_number-1)).toStringArray()[11].toString();

      RowListValuesObtained(lang,concept,room,start_explain,end_explain,
              start_right, end_right, start_wrong, end_wrong, start_question,end_question, video_url);

    }

    public List<Object> multiValuesField(String raw_value ){

        List<Object> result= new ArrayList<>();

        String[] values= raw_value.split(":");

        result.add(values[0]);
        result.add(values[1]);

        return result;
    }
    @SimpleEvent
    public void  RowListValuesObtained (String lang, String concept, String room, List start_explain, List end_explain,
                                        List start_right, List end_right, List start_wrong, List end_wrong, List start_question, List end_question, String video_url) {

        EventDispatcher.dispatchEvent(this, "RowListValuesObtained",lang,concept,room,start_explain,end_explain,start_right,end_right,start_wrong,end_wrong,start_question,end_question,video_url);
    }
}