/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import msearch.daten.MSConfig;
import org.apache.commons.lang3.StringUtils;

public class MSLog {

    public static final int FEHLER_ART_PROG = 0;
    public static final String FEHLER_ART_PROG_TEXT = "   Prog: ";
    public static final int FEHLER_ART_GETURL = 1;
    public static final String FEHLER_ART_GETURL_TEXT = " GetUrl: ";
    public static final int FEHLER_ART_MREADER = 2;
    public static final String FEHLER_ART_MREADER_TEXT = "MReader: ";
    public static final int FEHLER_ART_FILME_SUCHEN = 3;
    public static final String FEHLER_ART_FILME_SUCHEN_TEXT = "  Filme: ";
    private final static String FEHLER = "Fehler(" + MSConst.PROGRAMMNAME + "): ";

    // private
    private static final LinkedList<Integer[]> fehlerListe = new LinkedList<>(); // [Art, Fehlernummer, Anzahl, Exception(0,1 für ja, nein)]
    private static boolean progress = false;
    private static final Date startZeit = new Date(System.currentTimeMillis());
    private static File logFile = null;
    private static final ArrayList<String> logList = new ArrayList<>();

    public static synchronized void setLogfile(String logFileString) {
        logFile = new File(logFileString);
        File dir = new File(logFile.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logFile = null;
                MSLog.fehlerMeldung(632012165, MSLog.FEHLER_ART_PROG, "Search.logSchreiben", "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }

    }

    public static synchronized void versionsMeldungen(String classname) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Programmstart: " + sdf.format(startZeit));
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("##################################################################################");
        long totalMem = Runtime.getRuntime().totalMemory();
        MSLog.systemMeldung("totalMemory: " + totalMem / (1024L * 1024L) + " MiB");
        long maxMem = Runtime.getRuntime().maxMemory();
        MSLog.systemMeldung("maxMemory: " + maxMem / (1024L * 1024L) + " MiB");
        long freeMem = Runtime.getRuntime().freeMemory();
        MSLog.systemMeldung("freeMemory: " + freeMem / (1024L * 1024L) + " MiB");
        MSLog.systemMeldung("##################################################################################");
        //Version
        MSLog.systemMeldung(MSConst.PROGRAMMNAME + MSFunktionen.getProgVersionString());
        MSLog.systemMeldung("Compiled: " + MSFunktionen.getCompileDate());
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Java");
        MSLog.systemMeldung("Classname: " + classname);
        String[] java = MSFunktionen.getJavaVersion();
        for (String ja : java) {
            MSLog.systemMeldung(ja);
        }
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
    }

    public static synchronized void startMeldungen(String classname) {
        startZeit.setTime(System.currentTimeMillis());
        versionsMeldungen(classname);
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Programmpfad: " + MSFunktionen.getPathJar());
        MSLog.systemMeldung("Filmliste: " + MSConfig.getPathFilmlist_json_akt(true /*aktDate*/));
        MSLog.systemMeldung("Useragent: " + MSConfig.getUserAgent());
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        if (MSConfig.senderAllesLaden) {
            MSLog.systemMeldung("Laden:  alles");
        } else {
            MSLog.systemMeldung("Laden:  nur update");
        }
        if (MSConfig.updateFilmliste) {
            MSLog.systemMeldung("Filmliste:  nur updaten");
        } else {
            MSLog.systemMeldung("Filmliste:  neu erstellen");
        }
        MSLog.systemMeldung("Import (ersetzen):  " + MSConfig.importUrl__ersetzen);
        MSLog.systemMeldung("Import (anhängen):  " + MSConfig.importUrl__anhaengen);
        if (MSConfig.nurSenderLaden != null) {
            MSLog.systemMeldung("Nur Sender laden:  " + StringUtils.join(MSConfig.nurSenderLaden, ','));
        }
        MSLog.systemMeldung("##################################################################################");
    }

    public static synchronized void endeMeldung() {
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        ArrayList<String> ret = fehlerMeldungen();
        for (String s : ret) {
            systemMeldung(s);
        }

        // Laufzeit ausgeben
        Date stopZeit = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int minuten;
        try {
            minuten = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000 * 60));
        } catch (Exception ex) {
            minuten = -1;
        }
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("   --> Beginn: " + sdf.format(startZeit));
        systemMeldung("   --> Fertig: " + sdf.format(stopZeit));
        systemMeldung("   --> Dauer[Min]: " + (minuten == 0 ? "<1" : minuten));
        systemMeldung("##################################################################################");
        systemMeldung("");
        systemMeldung("   und Tschuess");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
    }

    public static synchronized ArrayList<String> fehlerMeldungen() {
        ArrayList<String> retList = new ArrayList<>();
        retList.add("");
        retList.add("##################################################################################");
        if (fehlerListe.size() == 0) {
            retList.add(" Keine Fehler :)");
        } else {
            // Fehler ausgeben
            int i_1;
            int i_2;
            for (int i = 1; i < fehlerListe.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    i_1 = fehlerListe.get(k - 1)[1];
                    i_2 = fehlerListe.get(k)[1];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (i_1 < i_2) {
                        fehlerListe.add(k - 1, fehlerListe.remove(k));
                    } else {
                        break;
                    }
                }
            }
            for (Integer[] integers : fehlerListe) {
                String z;
                switch (integers[0]) {
                    case FEHLER_ART_MREADER:
                        z = FEHLER_ART_MREADER_TEXT;
                        break;
                    case FEHLER_ART_FILME_SUCHEN:
                        z = FEHLER_ART_FILME_SUCHEN_TEXT;
                        break;
                    case FEHLER_ART_GETURL:
                        z = FEHLER_ART_GETURL_TEXT;
                        break;
                    case FEHLER_ART_PROG:
                        z = FEHLER_ART_PROG_TEXT;
                        break;
                    default:
                        z = "";
                }
                boolean ex = integers[3] == 1;
                String strEx;
                if (ex) {
                    strEx = "Ex! ";
                } else {
                    strEx = "    ";
                }
                if (integers[1] < 0) {
                    retList.add(strEx + z + " Fehlernummer: " + integers[1] + " Anzahl: " + integers[2]);
                } else {
                    retList.add(strEx + z + " Fehlernummer:  " + integers[1] + " Anzahl: " + integers[2]);
                }
            }
        }
        retList.add("##################################################################################");
        return retList;
    }

    // Fehlermeldung mit Exceptions
    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, new String[]{});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex, String text) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex, String text[]) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, text);
    }

    // Fehlermeldungen
    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, String text) {
        fehlermeldung_(fehlerNummer, art, klasse, null, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, String[] text) {
        fehlermeldung_(fehlerNummer, art, klasse, null, text);
    }

    public static synchronized void systemMeldung(String[] text) {
        systemmeldung_(text);
    }

    public static synchronized void systemMeldung(String text) {
        systemmeldung_(new String[]{text});
    }

    public static synchronized void debugMeldung(String text) {
        if (MSConfig.debug) {
            resetProgress();
            System.out.println("|||| " + text);
        }
    }

    public static synchronized void progress(String texte) {
        progress = true;
        if (!texte.isEmpty()) {
            System.out.print(texte + "\r");
        }
    }

    private static void resetProgress() {
        // Leerzeile um die Progresszeile zu löschen
        if (progress) {
            System.out.print("                                                                                                             \r");
            progress = false;
        }
    }

    private static void addFehlerNummer(int nr, int art, boolean exception) {
        int ex = exception ? 1 : 2;
        for (Integer[] i : fehlerListe) {
            if (i[1] == nr) {
                i[0] = art;
                i[2]++;
                i[3] = ex;
                return;
            }
        }
        // dann gibts die Nummer noch nicht
        fehlerListe.add(new Integer[]{art, nr, 1, ex});
    }

    private static void fehlermeldung_(int fehlerNummer, int art, String klasse, Exception ex, String[] texte) {
        addFehlerNummer(fehlerNummer, art, ex != null);
        if (ex != null || MSConfig.debug) {
            // Exceptions immer ausgeben
            resetProgress();
            String x, z;
            if (ex != null) {
                x = "!";
            } else {
                x = "=";
            }
            switch (art) {
                case FEHLER_ART_MREADER:
                    z = "  ==>";
                    break;
                case FEHLER_ART_FILME_SUCHEN:
                    z = "   >>";
                    break;
                case FEHLER_ART_GETURL:
                    z = "  ++>";
                    break;
                case FEHLER_ART_PROG:
                default:
                    z = "*";
            }
            logList.add(x + x + x + x + x + x + x + x + x + x
                    + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x);

            try {
                // Stacktrace
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    if (ex != null) {
                        ex.printStackTrace(pw);
                    }
                    pw.flush();
                    sw.flush();
                    logList.add(sw.toString());
                }
            } catch (Exception ignored) {
            }

            logList.add(z + " Fehlernr: " + fehlerNummer);
            if (ex != null) {
                logList.add(z + " Exception: " + ex.getMessage());
            }
            logList.add(z + " " + FEHLER + klasse);
            for (String aTexte : texte) {
                logList.add(z + "           " + aTexte);
            }
            logList.add("");
            printLog();
        }
    }

    private static void systemmeldung_(String[] texte) {
        resetProgress();
        final String z = ". ";
        if (texte.length <= 1) {
            logList.add(z + " " + texte[0]);
        } else {
            String zeile = "---------------------------------------";
            String txt;
            logList.add(z + zeile);
            for (String aTexte : texte) {
                txt = "| " + aTexte;
                logList.add(z + txt);
            }
            logList.add(z + zeile);
        }
        printLog();
    }

    private static void printLog() {
        for (String s : logList) {
            System.out.println(s);
        }
        if (logFile != null) {
            try {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(logFile, true), MSConst.KODIERUNG_UTF);
                for (String s : logList) {
                    out.write(s);
                    out.write("\n");
                }
                out.write("\n");
                out.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        logList.clear();
    }
}
