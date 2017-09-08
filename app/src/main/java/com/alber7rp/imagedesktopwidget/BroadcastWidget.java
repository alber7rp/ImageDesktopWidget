package com.alber7rp.imagedesktopwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.alber7rp.imagedesktopwidget.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Implementation of App Widget functionality.
 */
public class BroadcastWidget extends AppWidgetProvider {
    private static final String ACTION_TOCARIMAGEN = "ACTION_TOCARIMAGEN";
    private static final int SELECT_FILE = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.broadcast_widget);
        // Construct an Intent which is pointing this class.
        Intent intent = new Intent(context, BroadcastWidget.class);
        intent.setAction(ACTION_TOCARIMAGEN+String.valueOf(appWidgetId)); //Le indicamos la acción mas el id del widget, para tratar a cada widget por separado
        // And this time we are sending a broadcast with getBroadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.imagen, pendingIntent);
        // Instruct the widget manager to update the widget

        //Abrimos la base de datos 'DBUsuarios' en modo escritura
        ImageDesktopWidgetSQLiteHelper usdbh =
                new ImageDesktopWidgetSQLiteHelper(context, "DBImageDesktopWidget", null, 1);



        SQLiteDatabase db = usdbh.getReadableDatabase();

        //Si hemos abierto correctamente la base de datos
        if(db != null)
        {

            Cursor c = db.rawQuery("SELECT * FROM ImageWidget WHERE id="+appWidgetId, null);

            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                if (c.getString(1) != "default") {

                    Uri selectedImageUri = null;
                    Uri selectedImage = Uri.parse(c.getString(1));
                    InputStream imageStream = null;

                    String selectedPath = selectedImage.getPath();

                    try {
                        imageStream = context.getContentResolver().openInputStream(
                                selectedImage);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(context, "El archivo no existe o se ha modificado", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                    Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int width = d.getWidth();


                    //Reducimos el tamaño del bitmap, ya que tiene límite y suele excederse
                    //Reescalamos la imagen segun las dimensiones de la pantalla
                    if(d.getHeight() >1300) {
                        bmp = BroadcastWidget.scaleDownBitmap(bmp, ((int) (width * 0.35)), context);
                    }
                    else{
                        bmp = BroadcastWidget.scaleDownBitmap(bmp, ((int) (width * 0.8)), context);
                    }


                    views.setImageViewBitmap(R.id.imagen, bmp);
                    // This time we dont have widgetId. Reaching our widget with that way.
                    appWidgetManager = AppWidgetManager.getInstance(context);
                    // Instruct the widget manager to update the widget

                    //Actualizamos el widget que nos llamó con la foto que se escogió
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);


                }

                else {
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
            else{
                db = usdbh.getWritableDatabase();
                db.execSQL("INSERT INTO ImageWidget VALUES ("+appWidgetId+",'default')");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }

            //Cerramos la base de datos
            db.close();



    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().startsWith(ACTION_TOCARIMAGEN)) {

            //Cuando se haya pulsado enviamos a su vez la id de dicho widget
            int widgetId = Integer.parseInt(intent.getAction().substring(ACTION_TOCARIMAGEN.length()));
            abrirActivityabrirGaleria(context, widgetId);

        }
    }

    //Abrimos una nueva activity para abrir desde alli la galería, ya que directamente no se puede
    public void abrirActivityabrirGaleria(Context context, int idwidget){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("wd", idwidget);
        context.startActivity(intent);
    }

    public void onDisabled(Context context){
        ImageDesktopWidgetSQLiteHelper usdbh = new ImageDesktopWidgetSQLiteHelper(context, "DBImageDesktopWidget", null, 1);

        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.delete("ImageWidget", "1=1",null);

        Toast.makeText(context, "Se han eliminado todas las instancias de ImageDesktopWidget", Toast.LENGTH_SHORT).show();
    }




}

