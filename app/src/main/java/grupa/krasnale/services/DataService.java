package grupa.krasnale.services;

import android.content.Context;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import grupa.krasnale.R;
import grupa.krasnale.models.DataModel;
import grupa.krasnale.models.DwarfModel;

public class DataService {

    public List<DwarfModel> dwarfs;

    public DataService(Context c) {
        InputStream inputStream = c.getResources().openRawResource(R.raw.krasnale);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int  len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        String raw = outputStream.toString();

        dwarfs = new Gson().fromJson(raw, DataModel.class).objects;
    }
}
