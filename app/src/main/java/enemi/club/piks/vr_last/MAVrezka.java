package enemi.club.piks.vr_last;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MAVrezka extends ActionBarActivity {

    public static final float MM = 72.0f/25.4f;
    public static final float HEIGHT = MM*165;
    public static final float WIDTH = MM*250;
    public static final String DirName = "vrezka";

    Bitmap bitmap;
    Canvas canvas;

    float DO;
    float DU;
    float Angle;
    float Wall;

    float MAX;
    float MIN;

    Uri uriImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavrezka);
        DO = 0;
        DU = 0;
        Angle = 0;
        Wall = 0;
        bitmap = Bitmap.createBitmap((int)WIDTH, (int)HEIGHT, Bitmap.Config.RGB_565);
        canvas = new Canvas(bitmap);
        Button button = (Button)findViewById(R.id.buttonCalc);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExternalStorageWritable()){
                    Log.i("onClick", "External memory is writable");
                    //Toast.makeText(MAVrezka.this, "Начинаю запись изображения", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MAVrezka.this, "Память только читается. Не могу записать изображение", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEmptyEditText((EditText)findViewById(R.id.DiametrOUT)))
                {
                    Toast toast = Toast.makeText(MAVrezka.this, "Введите диаметр врезаемой трубы", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                if (isEmptyEditText((EditText)findViewById(R.id.DiametrIN)))
                {
                    Toast toast = Toast.makeText(MAVrezka.this, "Введите диаметр трубы в которую будет производиться врезка", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    toast.show();
                    return;

                }

                if (isEmptyEditText((EditText)findViewById(R.id.Angle)))
                {
                    Toast toast = Toast.makeText(MAVrezka.this, "Введите угол врезки", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                if (isEmptyEditText((EditText)findViewById(R.id.WallHeight)))
                {
                    Toast toast = Toast.makeText(MAVrezka.this, "Введите толщину стенки врезаемой трубы", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                try {
                    DO = Float.parseFloat(((EditText) findViewById(R.id.DiametrOUT)).getText().toString());
                    DU = Float.parseFloat(((EditText) findViewById(R.id.DiametrIN)).getText().toString());
                    Angle = Float.parseFloat(((EditText) findViewById(R.id.Angle)).getText().toString());
                    Wall = Float.parseFloat(((EditText) findViewById(R.id.WallHeight)).getText().toString());
                }
                catch (NumberFormatException e){
                    Toast toast = Toast.makeText(MAVrezka.this, "Введены некорректные данные или вообще данные не введены " + e.toString(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                if (DU < DO){
                    Toast toast = Toast.makeText(MAVrezka.this, "Диаметр врезаемой трубы меньше либо равен диаметру трубы в которую будет производиться врезка\nВведите корректные данные", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                String fileName = "VREZKA_" + DO + "_W_" + Wall + "_IN_" + DU + "_ANGLE_" + Angle + ".jpg";
                CreateIMG();
                getMinimunm();
                PaintAxes();
                DrawVrezka();
                saveImage(fileName);
            }
        });

    }

    protected boolean isEmptyEditText(EditText text){

        String ed_text = text.getText().toString().trim();

        if(ed_text.length() == 0 || ed_text.equals("") || ed_text == null)
        {
           return true;
        }
        else
        {
            return false;
        }
    }

    protected void DrawVrezka(){
        float RO = DO / 2;
        float RI = RO - Wall;
        float RU = DU / 2;
        float angle = getRadian(Angle);
        float angle90Minus = getRadian(90-Angle);
        Log.i("DW point", "RO = " + RO + " RI = " + RI + " RU = " + RU + " angle = " + angle + " 90 - angle = " + angle90Minus);

        float DoInMM = DO * (float)Math.PI * MM;
        Log.i("DW point", "DoInMM = " + DoInMM/MM);
        float DeltaX = DoInMM / 360;
        Log.i("DW point", "Delta X " + DeltaX);
        float x1, x2;
        float y1, y2;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4.0f);



        x1 = MM;
        y1 = getYO(RO, RI, RU, angle, angle90Minus, 0) * MM + 1*MM - MIN;

        for (int i = 1; i < 360; i++){
            x2 = DeltaX*i + MM;
            y2 = getYO(RO, RI, RU, angle, angle90Minus, i) * MM + 1.2f*MM - MIN;
            canvas.drawLine(x1, y1, x2, y2, paint);
            x1 = x2;
            y1 = y2;
        }
    }

    float getRadian(float degres){
        return degres*(float)Math.PI/180;
    }

    protected float getYO(float RO, float RI, float RU, float angle, float angle90Minus, int CurrentAngle){
        float cangle = CurrentAngle;
        cangle = getRadian(cangle);
        return (float)(Math.sqrt(RU*RU-RI*RI+(RO*Math.cos(cangle))*(RO*Math.cos(cangle)))/Math.sin(angle)-Math.tan(angle90Minus)*RO*Math.cos(cangle));
    }

    public void getMinimunm(){
        float RO = DO / 2;
        float RI = RO - Wall;
        float RU = DU / 2;
        float angle = getRadian(Angle);
        float angle90Minus = getRadian(90-Angle);
        float y;

        MIN = getYO(RO, RI, RU, angle, angle90Minus, 0) * MM;
        MAX = MIN;

        for (int i = 1; i < 360; i++){
            y = getYO(RO, RI, RU, angle, angle90Minus, i) * MM;
            if (MIN > y) MIN = y;
            if (MAX < y) MAX = y;
        }
        Log.i("Minimum", "MIN " + MIN + "  MAX = " + MAX);
    }

    protected void PaintAxes(){
        Paint fontPaint = new Paint();
        fontPaint.setTextSize(12);
        String text = "Врезка трубы " + DO + " с толщиной стенки " + Wall + " в трубу " + DU + " под углом " + Angle + "(165 на 250)";

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        canvas.drawLine(1*MM, 1*MM, 1*MM, HEIGHT, paint);
        canvas.drawLine((DO*(float)Math.PI + 1)*MM, 1*MM, (DO*(float)Math.PI + 1)*MM, HEIGHT, paint);

        float part90 = ((DO*(float)Math.PI)*MM)/4.0f;

        canvas.drawLine(part90 + 1*MM, 1*MM, part90 + 1*MM, (HEIGHT - 40), paint);
        canvas.drawLine(2*part90 + 1*MM, 1*MM, 2*part90 + 1*MM, (HEIGHT - 40), paint);
        canvas.drawLine(3*part90 + 1*MM, 1*MM, 3*part90 + 1*MM, (HEIGHT - 40), paint);

        canvas.drawLine(1*MM, 1.5f*MM + MAX, (DO*(float)Math.PI + 1)*MM, 1.5f*MM + MAX, paint);
        canvas.drawLine(1*MM, 1*MM, (DO*(float)Math.PI + 1)*MM, 1*MM, paint);

        canvas.drawText(text, 2*MM, MAX + 20, fontPaint);
    }

    protected void CreateIMG(){
        Paint paint = new Paint();
        bitmap = Bitmap.createBitmap((int)Math.round(WIDTH), (int)Math.round(HEIGHT), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getDirVrezka(){
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), DirName);
        if (!file.exists())
            if(!file.mkdirs()){
                Log.e("getDirVrezka", "Directory not created");
                Toast.makeText(MAVrezka.this, "Directory not created", Toast.LENGTH_SHORT).show();
            }
        Log.i("getDirVrezka", file.toString());
        return file;
    }

    public void saveImage(String nameFile){
        File file = new File(getDirVrezka(), File.separator + nameFile);
        if (file.exists()) file.delete();
        try{
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            uriImage = uri;
                        }
                    });

            Toast toast = Toast.makeText(MAVrezka.this, "Файл " + nameFile + " сохранен", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();


        }catch (IOException e){
            Log.w("saveImage", "Error writing " + file, e);
            Toast toast = Toast.makeText(MAVrezka.this, "Ошибка записи файла " + file, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
