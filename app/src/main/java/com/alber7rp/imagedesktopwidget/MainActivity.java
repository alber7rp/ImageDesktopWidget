package com.alber7rp.imagedesktopwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 1;
    private int wd; //id del widget que nos llamó

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wd = getIntent().getIntExtra("wd", -1);

        abrirGaleria(); //automáticamente abrimos la galeria

    }

    public void abrirGaleria(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), this.SELECT_FILE);
    }


    protected void onActivityResult(int requestCode, int resultCode,Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Uri selectedImage;

        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath=selectedImage.getPath();

                    if (requestCode == SELECT_FILE) {




                        if (selectedPath != null) {
                            InputStream imageStream = null;

                            //Abrimos la base de datos 'DBUsuarios' en modo escritura
                            ImageDesktopWidgetSQLiteHelper usdbh = new ImageDesktopWidgetSQLiteHelper(getApplicationContext(), "DBImageDesktopWidget", null, 1);

                            SQLiteDatabase db = usdbh.getWritableDatabase();

                            //Si hemos abierto correctamente la base de datos
                            if(db != null)
                            {
                                //Insertamos los datos en la tabla Usuarios
                                db.execSQL("UPDATE ImageWidget SET image='"+selectedImage.toString()+"' WHERE id="+wd);
                            }

                            //Cerramos la base de datos
                            db.close();


                            final int takeFlags = imageReturnedIntent.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            // Check for the freshest data.
                            getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);




                            try {
                                imageStream = getContentResolver().openInputStream(
                                        selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                            Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                            int width = d.getWidth();

                            //Toast.makeText(getApplicationContext(), width +" "+height, Toast.LENGTH_SHORT).show();

                            //Reducimos el tamaño del bitmap, ya que tiene límite y suele excederse
                            //Reescalamos la imagen segun las dimensiones de la pantalla
                            if(d.getHeight() >1300) {
                                bmp = this.scaleDownBitmap(bmp, ((int) (width * 0.35)), getApplicationContext());
                            }
                            else{
                                bmp = this.scaleDownBitmap(bmp, ((int) (width * 0.8)), getApplicationContext());
                            }

                            // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.broadcast_widget);
                            views.setImageViewBitmap(R.id.imagen, bmp );
                            // This time we dont have widgetId. Reaching our widget with that way.
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                            // Instruct the widget manager to update the widget

                            //Actualizamos el widget que nos llamó con la foto que se escogió
                            appWidgetManager.partiallyUpdateAppWidget(wd, views);

                            //Salimos de la activity para volver al escritorio


                        }
                    }
                }
                break;
        }

        finish();
    }

    //Reducir el tamaño de un bitmap, ya que a veces se excede
    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

}
