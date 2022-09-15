package moi.com.apps.alarcal;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Moi on 26/02/2018.
 */

public class CreaEvento extends AppCompatActivity {

    /**************
    *  VARIABLES  *
    ***************/
    final BBDD helper = new BBDD(this);

    private EditText nombre, descripcion;
    private EditText fecha_evento, fecha_fin;
    private EditText hora_evento, hora_fin;
    private EditText n_repeticiones, recordatorios;
    private TextInputLayout layout1,layout2,layout3,layout4,layout5;
    private Boolean fechas_iguales;


    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la fecha
    final int mes = c.get(Calendar.MONTH);
    final int dia = c.get(Calendar.DAY_OF_MONTH);
    final int anio = c.get(Calendar.YEAR);

    //Variables para obtener la hora hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);

    private String repeticion, record;
    private String fecha_elegida;


    /************
    *  METODOS  *
    *************/
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.evento_crea);


        fecha_evento        = (EditText) findViewById(R.id.fecha_evento);
        hora_evento         = (EditText) findViewById(R.id.hora_evento);
        fecha_fin           = (EditText) findViewById(R.id.fecha_fin);
        hora_fin            = (EditText) findViewById(R.id.hora_fin);
        n_repeticiones      = (EditText) findViewById(R.id.editTextEleccion);
        recordatorios       = (EditText) findViewById(R.id.editTextEleccionRecordatorio);


        //onclick events
        fecha_evento.setOnClickListener(new View.OnClickListener()  { public void onClick(View v) { obtenerFecha(findViewById(R.id.image_fecha_evento)); }});
        fecha_fin.setOnClickListener(new View.OnClickListener()     { public void onClick(View v) { obtenerFecha(findViewById(R.id.image_fecha_aviso)); }});
        hora_evento.setOnClickListener(new View.OnClickListener()   { public void onClick(View v) { obtenerHora(findViewById(R.id.image_hora_evento)); }});
        hora_fin.setOnClickListener(new View.OnClickListener()      { public void onClick(View v) { obtenerHora(findViewById(R.id.image_hora_aviso)); }});
    }



    /*
    * FUNCION QUE CAPTURA LOS RESULTADOS DE LAS ACTIVIDADES QUE SON LLAMADAS DESDE ESTA CON UN REQUESTCODE ESPECIFICO
    * */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            if (requestCode == 200) {
                Bundle datos = data.getExtras();
                repeticion    = datos.getString("opcion");

                if(repeticion != null)
                    n_repeticiones.setText(repeticion);
            }

            if(requestCode == 201){
                Bundle datos = data.getExtras();
                record       = datos.getString("opcion");

                if(record != null)
                    recordatorios.setText(record);
            }
        }
    }



    /*
    * GUARDA UN EVENTO EN LA BASE DE DATOS
    * */
    public void GuardaEvento(View vista){

        descripcion         = (EditText) findViewById(R.id.descripcion);
        layout1             = (TextInputLayout) findViewById(R.id.textinputlayout1);
        layout2             = (TextInputLayout) findViewById(R.id.textinputlayout2);
        layout3             = (TextInputLayout) findViewById(R.id.textinputlayout3);
        layout4             = (TextInputLayout) findViewById(R.id.textinputlayout4);
        layout5             = (TextInputLayout) findViewById(R.id.textinputlayout5);

        if( !ValidaFormulario() ){
            return;
        }

        Log.d("TAG", "Validacion completada");


        Object[] objeto = calculaRepeticiones();
        int repeticiones_evento  = (int)objeto[0];
        ArrayList<String> fechas = (ArrayList<String>)objeto[1];

        for (int i=0;i<repeticiones_evento; i++){
                Log.d("TAG", "FechaTAG: "+fechas.get(i));
                guardaEventoBBDD(i, fechas.get(i));
        }


        // Mensaje para verificar la insercion
        Toast.makeText(getApplicationContext(), "Evento creado correctamente ", Toast.LENGTH_LONG).show();

        // Vuelve a la ventana principal
        cancelaCreaEvento(vista);
    }


    public void guardaEventoBBDD(int n, String fecha){

        // Obtenemos la base de datos en modo escritura
        SQLiteDatabase db = helper.getWritableDatabase();


        //Obtenemos la fecha de aviso a partir de la opcion de recordatorios seleccionada
        String fecha_aviso = calculaFechaAviso(fecha, hora_evento.getText().toString() );
        Log.d("TAG", "Fecha aviso: " + fecha_aviso);

        // Crear un contenedor de valores con las parejas clave-valor de datos que queremos guardar en la bbdd
        if(fecha.contains("-")) {
            String[] fecha_arr  = fecha.split("-");
            fecha = fecha_arr[2] + "/" + fecha_arr[1] + "/" + fecha_arr[0];
        }

        ContentValues values = new ContentValues();
        values.put(Estructura_BBDD.COLUMNA1, nombre.getText().toString());
        values.put(Estructura_BBDD.COLUMNA2, fecha);
        values.put(Estructura_BBDD.COLUMNA3, hora_evento.getText().toString());
        if(n == 0)
            values.put(Estructura_BBDD.COLUMNA4, fecha_fin.getText().toString());
        else
            values.put(Estructura_BBDD.COLUMNA4, fecha);
        values.put(Estructura_BBDD.COLUMNA5, hora_fin.getText().toString());
        values.put(Estructura_BBDD.COLUMNA6, descripcion.getText().toString());
        values.put(Estructura_BBDD.COLUMNA7, fecha_aviso);
        values.put(Estructura_BBDD.COLUMNA8, n);
        values.put(Estructura_BBDD.COLUMNA9, n_repeticiones.getText().toString());
        values.put(Estructura_BBDD.COLUMNA10, recordatorios.getText().toString());


        // Insert the new row, returning the primary key value of the new row
        db.insert(Estructura_BBDD.TABLE_NAME, null, values);
        db.close();
    }



    /*
    * CALCULA LA FECHA DE AVISO DEL EVENTO A PARTIR DE LA OPCION DE RECORDATORIO ELEGIDA Y LA FECHA DEL EVENTO
    * */
    public String calculaFechaAviso(String fecha_evento, String hora_evento){


        String[] keys = {"minutos", "dias", "dia", "hora", "semana"};
        String match = "default";
        int value    = 0;
        String fecha_aviso = "";

        for (int i = 0; i < keys.length; i++) {
            if(record == null)
                match = "default";
            else if (record.contains(keys[i])) {
                match = keys[i];
                value = Integer.parseInt(record.split(keys[i])[0].trim());
                break;
            }
        }


        final SimpleDateFormat df  = new SimpleDateFormat( "yyyy-MM-dd" );
        final SimpleDateFormat df2 = new SimpleDateFormat( "HH:mm:ss" );
        Date fecha = null, date_hora = null;
        String[] fecha_arr;

        String hora         = hora_evento.substring(0,5);
        if(fecha_evento.contains("/"))
            fecha_arr  = fecha_evento.split("/");
        else
            fecha_arr  = fecha_evento.split("-");


        try {
            fecha       = df.parse( fecha_arr[2] + "-" + fecha_arr[1] + "-" + fecha_arr[0] + " " + hora.substring(0,2) + ":" + hora.substring(3,5) + ":00" );
            date_hora   = df2.parse( hora.substring(0,2) + ":" + hora.substring(3,5) + ":00" );
        } catch (ParseException e) {
            e.printStackTrace();
        }


        final Calendar cal = Calendar.getInstance();            cal.setTime( fecha );
        final Calendar cal2 = Calendar.getInstance();           cal2.setTime( date_hora);
        switch (match) {
            case "minutos":
                cal2.add( Calendar.MINUTE, -value );
                fecha_aviso = fecha_arr[2] + "-" + fecha_arr[1] + "-" + fecha_arr[0] + " " + df2.format(cal2.getTime());
                Log.d("TAG","fecha: "+ fecha_aviso);
                break;

            case "hora":
                cal2.add( Calendar.HOUR_OF_DAY, -value );
                fecha_aviso = fecha_arr[2] + "-" + fecha_arr[1] + "-" + fecha_arr[0] + " " + df2.format(cal2.getTime());
                Log.d("TAG","fecha: "+ fecha_aviso);
                break;

            case "dia":
                cal.add( Calendar.DAY_OF_MONTH, -value );
                fecha_aviso = df.format(cal.getTime()) + " " + hora.substring(0,2) + ":" + hora.substring(3,5) + ":00";
                Log.d("TAG","fecha: " +  fecha_aviso);
                break;

            case "dias":
                cal.add( Calendar.DAY_OF_MONTH, -value );
                fecha_aviso = df.format(cal.getTime()) + " " + hora.substring(0,2) + ":" + hora.substring(3,5) + ":00";
                Log.d("TAG","fecha: " +  fecha_aviso);
                break;

            case "semana":
                cal.add( Calendar.WEEK_OF_MONTH, -value );
                fecha_aviso = df.format(cal.getTime()) + " " + hora.substring(0,2) + ":" + hora.substring(3,5) + ":00";
                Log.d("TAG","fecha: " +  fecha_aviso);
                break;

            default:
                fecha_aviso = fecha_arr[2] + "-" + fecha_arr[1] + "-" + fecha_arr[0] + " " + hora.substring(0,2) + ":" + hora.substring(3,5) + ":00";
                Log.d("TAG","A la hora del evento "+ fecha_aviso);
                break;
        }

        fecha_arr   = fecha_aviso.split(" ");
        fecha_aviso = fecha_arr[0].split("-")[2] + "-" + fecha_arr[0].split("-")[1] + "-" + fecha_arr[0].split("-")[0] + " " + fecha_arr[1];
        return fecha_aviso;
    }



    /*CALCULA CUANTAS REPETICIONES TENDRA EL EVENTO; DEVUELVE EL NUMERO DE REPETICIONES Y UN ARRAYLIST CON LAS FECHAS*/
    public Object[] calculaRepeticiones(){

        String[] keys = {"Diariamente", "Dias de semana", "Semanalmente", "Cada dos semanas", "Mensualmente", "Anualmente"};
        ArrayList<String> fechas = new ArrayList<String>();
        String match = "default";
        int numero_repeticiones = 0;

        for (int i = 0; i < keys.length; i++) {
            if(repeticion == null)
                match = "default";
            else if (repeticion.contains(keys[i])) {
                match = keys[i];
                break;
            }
        }

        //Creamos objetos de tipo Date a partir de las fechas selccionadas por el usuario
        final SimpleDateFormat df  = new SimpleDateFormat( "yyyy-MM-dd" );
        Date fecha_inicial = null, fecha_final = null;
        String[] fecha_ini  = fecha_evento.getText().toString().split("/");
        String[] fecha_f  = fecha_fin.getText().toString().split("/");
        try {
            fecha_inicial   = df.parse( fecha_ini[2] + "-" + fecha_ini[1] + "-" + fecha_ini[0]);
            fecha_final     = df.parse( fecha_f[2] + "-" + fecha_f[1] + "-" + fecha_f[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar cal  = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        cal.setTime( fecha_inicial );
        cal2.setTime( fecha_final );


        //Determinamos la opcion elegida y comparamos las fechas para saber el numero de repeticiones del evento antes de que finalice
        switch (match) {
            case "Diariamente":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.DAY_OF_MONTH, 1);
                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            case "Dias de semana":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.DAY_OF_MONTH, 1);

                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SATURDAY) {               // If it's Saturday skip to Monday
                        cal.add(Calendar.DATE, 2);
                    } else if (dayOfWeek == Calendar.SUNDAY){           // If it's Sunday skip to Monday
                        cal.add(Calendar.DATE, 1);
                    }

                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            case "Semanalmente":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.DAY_OF_MONTH, 7);
                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            case "Cada dos semanas":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.DAY_OF_MONTH, 14);
                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            case "Mensualmente":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.MONTH, 1);
                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            case "Anualmente":
                do{
                    Log.d("TAG", "Fecha nueva: " + df.format(cal.getTime()));
                    fechas.add(df.format(cal.getTime()));
                    cal.add( Calendar.YEAR, 1);
                    numero_repeticiones++;

                }while (cal.before(cal2) || cal.compareTo(cal2)==0);
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;


            default:
                numero_repeticiones = 1;
                fechas.add(df.format(cal.getTime()));
                Log.d("TAG", "repeticiones: " + numero_repeticiones );
                break;
        }

        Object[] mixedArray = new Object[2];
        mixedArray[0]=numero_repeticiones;
        mixedArray[1]=fechas;

        return mixedArray;
    }



    /*
    * REGRESA AL USUARIO A LA VENTANA PRINCIPAL DE LA APP
    * */
    public void cancelaCreaEvento(View vista) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }


    /*
    * FUNCION PARA OBTENER LA FECHA SELECCIONADA EN EL DATEPICKER Y ESTABLECER ESA FECHA EN EL TEXTVIEW
    * */
    public void obtenerFecha(final View vista_cal){

        class DatePickerListener implements DatePickerDialog.OnDateSetListener {

            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //Esta variable lo que realiza es aumentar en uno el mes ya que comienza desde 0 = enero
                final int mesActual = month + 1;

                //Formateo el día obtenido: antepone el 0 si son menores de 10
                String diaFormateado = (dayOfMonth < 10)? "0" + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);

                //Formateo el mes obtenido: antepone el 0 si son menores de 10
                String mesFormateado = (mesActual < 10)? "0" + String.valueOf(mesActual):String.valueOf(mesActual);


                String id_vista     = getResources().getResourceEntryName(vista_cal.getId());
                String subcadena    = id_vista.substring(12);

                if(subcadena.equals("evento")){
                    fecha_evento.setText(diaFormateado + "/" + mesFormateado + "/" + year);
                }else{
                    fecha_fin.setText(diaFormateado + "/" + mesFormateado + "/" + year);
                }
            }
        }

        final DatePickerDialog calendario = new DatePickerDialog(this, new DatePickerListener(),anio,mes,dia); //inicializa el datepicker en el dia actual
        calendario.show();
    }



    /*
    * FUNCION PARA OBTENER LA HORA SELECCIONADA EN EL DATEPICKER Y ESTABLECER ESA HORA EN EL TEXTVIEW
    * */
    public void obtenerHora(final View vista_time){

        class TimePickerListener implements TimePickerDialog.OnTimeSetListener {

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                //Formateo el hora obtenido: antepone el 0 si son menores de 10
                String horaFormateada =  (hourOfDay < 10)? String.valueOf("0" + hourOfDay) : String.valueOf(hourOfDay);

                //Formateo el minuto obtenido: antepone el 0 si son menores de 10
                String minutoFormateado = (minute < 10)? String.valueOf("0" + minute):String.valueOf(minute);

                //Obtengo el valor a.m. o p.m., dependiendo de la selección del usuario
                String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                //Muestro la hora con el formato deseado
                String id_vista     = getResources().getResourceEntryName(vista_time.getId());
                String subcadena    = id_vista.substring(11);

                if(subcadena.equals("evento")){
                    hora_evento.setText(horaFormateada + ":" + minutoFormateado + " " + AM_PM);
                }else{
                    hora_fin.setText(horaFormateada + ":" + minutoFormateado + " " + AM_PM);
                }
            }
        }

        final TimePickerDialog timedialog = new TimePickerDialog(this, new TimePickerListener(),hora, minuto, false); //inicializa el datepicker en el dia actual
        timedialog.show();
    }



    /*
    *  LLEVA AL USUARIO A LA ACTIVITY DE REPETICIONES PARA QUE ELIJA CUANDO QUIERE QUE SE REPITA EL EVENTO
    * */
    public void ejecutaActivityRepeticiones(View vista){
        Intent i = new Intent(this, Repeticiones.class);
        i.putExtra("Clase", "CreaEvento");
        i.putExtra("Fecha_elegida", fecha_evento.getText().toString());

        startActivityForResult(i,200);
    }


    /*
    *  LLEVA AL USUARIO A LA ACTIVITY DE RECORDATORIOS PARA QUE ELIJA CUANDO QUIERE QUE SE LE RECUERDE EL EVENTO
    * */
    public void ejecutaActivityRecordatorios(View vista){
        Intent i = new Intent(this, Recordatorios.class);
        i.putExtra("Clase", "CreaEvento");
        startActivityForResult(i,201);
    }


    /*
    *  VALIDACIONES DEL FORMULARIO DE INTRODUCCION DE DATOS DEL EVENTO
    * */
    public boolean ValidaFormulario(){

        if(validaNombre() && validaFechaEvento() && validaHoraEvento() && validaFechaFinEvento()  && validaHoraFin() ){
            return true;
        }
        else{
            return false;
        }
    }


    public boolean validaNombre(){

        nombre = (EditText) findViewById(R.id.nombre_evento);
        String nombre_bbdd = null;
        Cursor consulta = helper.consultaEventos(Estructura_BBDD.COLUMNA1, nombre.getText().toString(),"=", null, true, false);
        if (consulta.moveToFirst()) {
            nombre_bbdd = consulta.getString(0);
        }

        if( nombre.getText().toString().trim().isEmpty() || nombre_bbdd != null ){
            layout1.setErrorEnabled(true);
            layout1.setError("Campo vacío o nombre existente");
            nombre.requestFocus();
            return false;
        }
        else{
            layout1.setErrorEnabled(false);
            return true;
        }
    }


    public boolean validaFechaEvento(){

        fecha_evento = (EditText) findViewById(R.id.fecha_evento);
        if( fecha_evento.getText().toString().trim().isEmpty() ){
            layout2.setErrorEnabled(true);
            layout2.setError("Campo vacío");
            fecha_evento.requestFocus();
            return false;
        }
        else{
            layout2.setErrorEnabled(false);
            return true;
        }
    }


    public boolean validaHoraEvento(){

        hora_evento = (EditText) findViewById(R.id.hora_evento);
        if( hora_evento.getText().toString().trim().isEmpty() ){
            layout3.setErrorEnabled(true);
            layout3.setError("Campo vacío");
            hora_evento.requestFocus();
            return false;
        }
        else{
            layout3.setErrorEnabled(false);
            return true;
        }
    }


    public boolean validaFechaFinEvento(){

        fecha_evento    = (EditText) findViewById(R.id.fecha_evento);
        fecha_fin       = (EditText) findViewById(R.id.fecha_fin);

        if( fecha_fin.getText().toString().trim().isEmpty() ){
            layout4.setErrorEnabled(true);
            layout4.setError("Campo vacío");
            fecha_fin.requestFocus();
            return false;
        }
        else{
            //Creamos objetos de tipo Date a partir de las fechas selccionadas por el usuario
            final SimpleDateFormat df  = new SimpleDateFormat( "yyyy-MM-dd" );
            Date fecha_inicial = null, fecha_final = null;
            String[] fecha_ini  = fecha_evento.getText().toString().split("/");
            String[] fecha_f  = fecha_fin.getText().toString().split("/");
            try {
                fecha_inicial   = df.parse( fecha_ini[2] + "-" + fecha_ini[1] + "-" + fecha_ini[0]);
                fecha_final     = df.parse( fecha_f[2] + "-" + fecha_f[1] + "-" + fecha_f[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final Calendar eventoDate  = Calendar.getInstance();
            final Calendar finalDate = Calendar.getInstance();
            eventoDate.setTime( fecha_inicial );
            finalDate.setTime( fecha_final );

            if( finalDate.before(eventoDate) ){
                layout4.setErrorEnabled(true);
                layout4.setError("La fecha final debe ser mayor a la del evento");
                fecha_fin.requestFocus();
                return false;
            }else {
                fechas_iguales = false;
                if(finalDate.compareTo(eventoDate) == 0)
                    fechas_iguales = true;

                layout4.setErrorEnabled(false);
                return true;
            }
        }
    }


    public boolean validaHoraFin(){

        hora_evento = (EditText) findViewById(R.id.hora_evento);
        hora_fin    = (EditText) findViewById(R.id.hora_fin);

        if( hora_fin.getText().toString().trim().isEmpty() ){
            layout5.setErrorEnabled(true);
            layout5.setError("Campo vacío");
            hora_fin.requestFocus();
            return false;
        }
        else{
            if(fechas_iguales) {
                SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm");
                Date finalDate = null, eventoDate = null;
                try {
                    Log.d("TAG", "aviso0: "+ hora_fin.getText().toString() + " evento0: " + hora_evento.getText().toString());
                    finalDate  = inputParser.parse(hora_fin.getText().toString());
                    eventoDate = inputParser.parse(hora_evento.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.d("TAG", "aviso2: "+ finalDate + " evento2: " + eventoDate);
                if (finalDate.before(eventoDate)) {
                    layout5.setErrorEnabled(true);
                    layout5.setError("La hora final debe ser mayor a la del evento");
                    hora_fin.requestFocus();
                    return false;
                } else {
                    layout5.setErrorEnabled(false);
                    return true;
                }
            }else {
                layout5.setErrorEnabled(false);
                return true;
            }
        }
    }
}
