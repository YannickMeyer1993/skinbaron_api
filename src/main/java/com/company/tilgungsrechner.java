package com.company;

import com.company.common.PostgresHelper;
import com.company.metrics.Metric;
import com.company.metrics.MetricsPusher;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class tilgungsrechner {
    public static double compute(double kreditHoehe, double zins,double rate, int maxLength, String startDate) throws SQLException {

        System.out.print("Höhe: "+kreditHoehe);
        System.out.print(" Zins: "+zins);
        double nochZuZahlen = kreditHoehe;
        int i = 0;
        double summeDerZinsen = 0;
        ArrayList<Metric> MetricsList = new ArrayList<>();
        double tilgung= (rate - nochZuZahlen * zins / 12.0 / 100.0)*12.0/kreditHoehe*100.0;
        while (nochZuZahlen > 0 ) {

            double zinsrate = nochZuZahlen * zins / 12 / 100; //Zinsen pro Monat
            summeDerZinsen += zinsrate;
            nochZuZahlen = nochZuZahlen - rate + zinsrate;

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(MetricsPusher.getLongfromDateString(startDate));
            cal.add(Calendar.MONTH, i);
            java.util.Date dt = cal.getTime();
            Timestamp MetricsTS = new Timestamp(dt.getTime());
            MetricsList.add(new Metric(""+kreditHoehe+" "+maxLength,(""+(nochZuZahlen<0?0:Math.round(nochZuZahlen*100.0)/100.0)).replace(".",","),MetricsTS));
            MetricsList.add(new Metric(""+kreditHoehe+" "+maxLength+" Zinsen",(""+Math.round(zinsrate*100.0)/100.0).replace(".",","),MetricsTS));
            MetricsList.add(new Metric(""+kreditHoehe+" "+maxLength+" Tilgung",(""+Math.round((rate-zinsrate)*100.0)/100.0).replace(".",","),MetricsTS));

            i++;
            if (maxLength <=i) {
                break;
            }
        }

        System.out.print(" Anfängliche Tilgung: "+Math.round(tilgung*100)/100.0);
        System.out.print(" Höhe der gezahlten Zinsen: "+Math.round(summeDerZinsen*100.0)/100.0);
        System.out.print(" Rate: "+rate);
        System.out.print(" Restschuld: "+(nochZuZahlen<0?0:Math.round(nochZuZahlen*100.0)/100.0));
        System.out.print(" Laufzeit: "+i/12+ " Jahre und "+i%12+" Monate");
        System.out.println();

        MetricsPusher pusher = new MetricsPusher(MetricsList);

        return Math.round(nochZuZahlen*100.0)/100.0;
    }

    public static void main(String[] args) throws SQLException {

        PostgresHelper.executeDDL("truncate table monitoring.metrics;");

        //System.out.println("Höhe nach 9 Monaten");
        //double nochZuZahlen = compute(25578.66d, 3.69d,263.33d,6, "31.12.2021");//50k
        //compute(nochZuZahlen, 2.12d,158.80d,180, "30.06.2022");//50k
        //compute(10000.00d, 5.00d, 41.67d,9);//10k
        //compute(17042.61d, 5.46d,115.82d,9);//21.7k
        //compute(58604.63d, 2.00d,204.17d,9);//70k
        //System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");
        //108k ca
        //

        //System.out.println("70000 Euro (Kostenlos tilgen)");
        //compute(57639.75d, 2.00d,204.17d,1200);//70k
        //compute(57639.75d, 2.00d,204.17d,60);// 5 Jahre bei Zinsbindung
        //compute(50824.09d, 2.00d,470.17d,120);// 10 Jahre Vollzilgung
        //System.out.println();

        //Neu
        //Annahme: Umstellung 01.10.2021

        //System.out.print("50000 Euro ");
        //compute(23895.99d, 2.12d,155.33d,1200);//50k Neue Rate, sodass 15 Jahre Volltilgung

        //System.out.print("10000 Euro ");
        //compute(10000.00d, 5.00d, 41.67d,1200);//10k Keine Tilgung
        //compute(10000.00d, 2.30d, 66d,180,"30.06.2022");//Neue Rate, sodass 15 Jahre Volltilgung

        //System.out.println();
        //Keine Info über Volltilgung
        System.out.println("21700 Euro (Kostenlos tilgen)");
        compute(16691.79d, 2.49d,62.57d,180,"30.06.2022");//21.7k
        //compute(10000d, 2.89d,68.42d,120,"30.06.2022");//21.7k

    }

}
