package es.uca.vedils.xr;

import android.Manifest;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.File;
import com.google.appinventor.components.runtime.errors.PermissionException;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.*;
import java.util.*;
import java.util.Map;


@DesignerComponent(version = 20200915, description = "Component for advance operation with list", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/xrutils.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class XRScenes extends AndroidNonvisibleComponent implements Component {
    private final ComponentContainer container;



    public XRScenes(ComponentContainer container) {
        super(container.$form());
        this.container = container;

    }


    @SimpleFunction
    public YailList SelectColumnValues (YailList list, String column_name){

        LinkedHashSet<String> result_list=new LinkedHashSet<String>();

        int column_position=-1;


        for (Object i : list.toArray()) {

            String [] row_values=((YailList) i).toStringArray();

            for(int x=0;x<row_values.length;x++){

                if(column_position==x){

                    result_list.add(row_values[x]);
                }

                if(row_values[x].equals(column_name.toLowerCase())){

                    column_position=x;
                }

            }


        }

        return YailList.makeList(result_list);

    }

    @SimpleFunction
    public void SelectRowsValues (YailList list, YailList contexts){

        YailList result=YailList.makeEmptyList();
         int context_founds=0;
         boolean first=false;
        List<Object> array_list = new ArrayList<>();
        //recorro las row
        for (Object i : list.toArray()) {

            if(!first){
                array_list.add(i);
                first=true;
            }
            for (String y:((YailList)i).toStringArray()){

                for(String x:contexts.toStringArray()){

                    if(x.equals(y)){

                        context_founds++;
                    }

                }
            }

            if(context_founds==contexts.size()){
                array_list.add(i);
                context_founds=0;

            }else{
                context_founds=0;
            }


        }

        if(array_list.size()==1){
            ResultSelectRowsValues(result.makeList(array_list),true);
        }else{

            ResultSelectRowsValues(result.makeList(array_list),false);
        }




    }


    @SimpleEvent
    public void ResultSelectRowsValues(YailList list_result,boolean singleResult){

        EventDispatcher.dispatchEvent(this, "ResultSelectRowsValues",list_result,singleResult);


    }


}