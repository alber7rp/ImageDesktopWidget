package com.alber7rp.imagedesktopwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.alber7rp.imagedesktopwidget.R;

/**
 * Implementation of App Widget functionality.
 */
public class BroadcastWidget extends AppWidgetProvider {
    private static final String ACTION_TOCARIMAGEN = "ACTION_TOCARIMAGEN";
    private static final int SELECT_FILE = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

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
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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
        //Toast.makeText(context, "primer " + idwidget, Toast.LENGTH_SHORT).show();
        context.startActivity(intent);
    }




}

