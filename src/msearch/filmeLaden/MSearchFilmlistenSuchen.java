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
package msearch.filmeLaden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import msearch.daten.MSearchConfig;
import msearch.tool.DatumZeit;
import msearch.tool.GuiFunktionen;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchListenerMediathekView;
import msearch.tool.MSearchLog;

public class MSearchFilmlistenSuchen {
    // damit werden die DownloadURLs zum Laden einer Filmliste
    // gesucht
    //
    //Tags FilmUpdateServer Filmliste

    public static final String FILM_UPDATE_SERVER_PRIO_1 = "1";
    public static final String FILM_UPDATE_SERVER = "film-update-server";
    public static final int FILM_UPDATE_SERVER_MAX_ELEM = 5;
    public static final String FILM_UPDATE_SERVER_NR = "film-update-server-nr";
    public static final int FILM_UPDATE_SERVER_NR_NR = 0;
    public static final String FILM_UPDATE_SERVER_URL = "film-update-server-url";
    public static final int FILM_UPDATE_SERVER_URL_NR = 1;
    public static final String FILM_UPDATE_SERVER_DATUM = "film-update-server-datum"; // Datum in UTC
    public static final int FILM_UPDATE_SERVER_DATUM_NR = 2;
    public static final String FILM_UPDATE_SERVER_ZEIT = "film-update-server-zeit"; // Zeit in UTC
    public static final int FILM_UPDATE_SERVER_ZEIT_NR = 3;
    public static final String FILM_UPDATE_SERVER_PRIO = "film-update-server-prio";
    public static final int FILM_UPDATE_SERVER_PRIO_NR = 4;
    public static final String[] FILM_UPDATE_SERVER_COLUMN_NAMES = {FILM_UPDATE_SERVER_NR, FILM_UPDATE_SERVER_URL,
        FILM_UPDATE_SERVER_DATUM, FILM_UPDATE_SERVER_ZEIT, FILM_UPDATE_SERVER_PRIO};
    public static final String[] FILM_UPDATE_SERVER_COLUMN_NAMES_ANZEIGE = {"Nr", "Update-Url", "Datum", "Zeit", "Prio"};
    // Liste mit den Servern die Filmlisten anbieten
    public ListeFilmlistenServer listeFilmlistenServer = new ListeFilmlistenServer();
    // Liste mit den URLs zum Download der Filmliste
    public ListeDownloadUrlsFilmlisten listeDownloadUrlsFilmlisten = new ListeDownloadUrlsFilmlisten();

    public String suchen(ArrayList<String> bereitsVersucht) {
        // passende URL zum Laden der Filmliste suchen
        String retUrl;
        ListeDownloadUrlsFilmlisten tmp = new ListeDownloadUrlsFilmlisten();
        try {
            // Ausweichen auf andere Listenserver bei Bedarf
            getDownloadUrlsFilmlisten(MSearchConst.ADRESSE_FILMLISTEN_SERVER_XML, tmp, MSearchConfig.getUserAgent());
            if (tmp.size() > 0) {
                // dann die Liste Filmlistenserver aktualisieren
                updateListeFilmlistenServer(tmp);
                MSearchListenerMediathekView.notify(MSearchListenerMediathekView.EREIGNIS_LISTE_FILMLISTEN_SERVER, this.getClass().getSimpleName());
            }
            if (tmp.size() == 0) {
                // mit den Backuplisten versuchen
                getDownloadUrlsFilmlisten__backuplisten(tmp, MSearchConfig.getUserAgent());
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(347895642, MSearchLog.FEHLER_ART_PROG, "FilmUpdateServer.suchen", ex);
        }
        if (tmp.size() == 0) {
            MSearchLog.systemMeldung(new String[]{"Es ist ein Fehler aufgetreten!",
                "Es konnten keine Updateserver zum aktualisieren der Filme",
                "gefunden werden."});
        } else {
            listeDownloadUrlsFilmlisten = tmp;
        }
        if (listeDownloadUrlsFilmlisten.size() < 5) {
            // dann gibts ein paar fest hinterlegt URLs
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek1/Filmliste_09_00.bz2", "1", "09:20:00", getTag("09:20:00")));
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek2/Filmliste_12_00.bz2", "1", "12:20:00", getTag("12:20:00")));
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek1/Filmliste_16_00.bz2", "1", "16:20:00", getTag("16:20:00")));
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek2/Filmliste_19_00.bz2", "1", "19:20:00", getTag("19:20:00")));
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek1/Filmliste_20_00.bz2", "1", "20:20:00", getTag("20:20:00")));
            listeDownloadUrlsFilmlisten.add(new DatenUrlFilmliste("http://176.28.8.161/mediathek2/Filmliste_22_00.bz2", "1", "22:20:00", getTag("22:20:00")));
        }
        listeDownloadUrlsFilmlisten.sort();
        retUrl = listeDownloadUrlsFilmlisten.getRand(bereitsVersucht, 0); //eine Zufällige Adresse wählen
        MSearchListenerMediathekView.notify(MSearchListenerMediathekView.EREIGNIS_LISTE_URL_FILMLISTEN, this.getClass().getSimpleName());
        if (bereitsVersucht != null) {
            bereitsVersucht.add(retUrl);
        }
        return retUrl;
    }

    private void updateListeFilmlistenServer(ListeDownloadUrlsFilmlisten tmp) {
        Iterator<DatenUrlFilmliste> it = tmp.iterator();
        //listeFilmlistenServer.clear();
        while (it.hasNext()) {
            String serverUrl = it.next().arr[FILM_UPDATE_SERVER_URL_NR];
            String url = serverUrl.replace(GuiFunktionen.getDateiName(serverUrl), "");
            url = GuiFunktionen.addUrl(url, MSearchConst.DATEINAME_LISTE_FILMLISTEN);
            listeFilmlistenServer.addCheck(new DatenFilmlistenServer(url));
        }
        // die Liste der Filmlistenserver aufräumen
        listeFilmlistenServer.alteLoeschen();
    }

    private String getTag(String zeit) {
        Date tmp;
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("dd.MM.yyyy__HH:mm:ss");
        try {
            tmp = sdf_zeit.parse(DatumZeit.getHeute_dd_MM_yyyy() + "__" + zeit);
            if (tmp.compareTo(new Date()) > 0) {
                return DatumZeit.getGestern_dd_MM_yyyy();
            } else {
                return DatumZeit.getHeute_dd_MM_yyyy();
            }
        } catch (Exception ex) {
        }
        return DatumZeit.getHeute_dd_MM_yyyy();
    }

    private void getDownloadUrlsFilmlisten__backuplisten(ListeDownloadUrlsFilmlisten sListe, String userAgent) {
        // für den Notfall fest hinterlegte Downloadserver
        getDownloadUrlsFilmlisten(GuiFunktionen.addUrl("http://176.28.8.161/mediathek1", MSearchConst.DATEINAME_LISTE_FILMLISTEN), sListe, userAgent);
        getDownloadUrlsFilmlisten(GuiFunktionen.addUrl("http://176.28.8.161/mediathek2", MSearchConst.DATEINAME_LISTE_FILMLISTEN), sListe, userAgent);
        getDownloadUrlsFilmlisten(GuiFunktionen.addUrl("http://176.28.8.161/mediathek3", MSearchConst.DATEINAME_LISTE_FILMLISTEN), sListe, userAgent);
        Iterator<DatenFilmlistenServer> it = listeFilmlistenServer.iterator();
        while (it.hasNext()) {
            if (sListe.size() > 100) {
                // genug
                break;
            }
            DatenFilmlistenServer fs = it.next();
            getDownloadUrlsFilmlisten(fs.arr[DatenFilmlistenServer.FILM_LISTEN_SERVER_URL_NR], sListe, userAgent);
        }
    }

    public static void getDownloadUrlsFilmlisten(String dateiUrl, ListeDownloadUrlsFilmlisten sListe, String userAgent) {
        //String[] ret = new String[]{""/* version */, ""/* release */, ""/* updateUrl */};
        try {
            int event;
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            XMLStreamReader parser;
            InputStreamReader inReader;
            if (GuiFunktionen.istUrl(dateiUrl)) {
                // eine URL verarbeiten
                int timeout = 20000; //ms
                URLConnection conn;
                conn = new URL(dateiUrl).openConnection();
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                inReader = new InputStreamReader(conn.getInputStream(), MSearchConst.KODIERUNG_UTF);
            } else {
                // eine Datei verarbeiten
                File f = new File(dateiUrl);
                if (!f.exists()) {
                    return;
                }
                inReader = new InputStreamReader(new FileInputStream(f), MSearchConst.KODIERUNG_UTF);
            }
            parser = inFactory.createXMLStreamReader(inReader);
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String parsername = parser.getLocalName();
                    if (parsername.equals("Server")) {
                        //wieder ein neuer Server, toll
                        getServer(parser, sListe);
                    }
                }
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(821069874, MSearchLog.FEHLER_ART_PROG, MSearchFilmlistenSuchen.class.getName(), ex, "Die URL-Filmlisten konnte nicht geladen werden: " + dateiUrl);
        }
    }

    private static void getServer(XMLStreamReader parser, ListeDownloadUrlsFilmlisten sListe) {
        String zeit = "";
        String datum = "";
        String serverUrl = "";
        String prio = "";
        int event;
        try {
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    //parsername = parser.getLocalName();
                    if (parser.getLocalName().equals("URL")) {
                        serverUrl = parser.getElementText();
                    } else if (parser.getLocalName().equals("Prio")) {
                        prio = parser.getElementText();
                    } else if (parser.getLocalName().equals("Datum")) {
                        datum = parser.getElementText();
                    } else if (parser.getLocalName().equals("Zeit")) {
                        zeit = parser.getElementText();
                    }
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    //parsername = parser.getLocalName();
                    if (parser.getLocalName().equals("Server")) {
                        if (!serverUrl.equals("")) {
                            //public DatenFilmUpdate(String url, String prio, String zeit, String datum, String anzahl) {
                            if (prio.equals("")) {
                                prio = MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_PRIO_1;
                            }
                            sListe.addWithCheck(new DatenUrlFilmliste(serverUrl, prio, zeit, datum));
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException ex) {
        }

    }

    public static File ListeFilmlistenSchreiben(ListeDownloadUrlsFilmlisten listeFilmUpdateServer) {
        File tmpFile = null;
        XMLOutputFactory outFactory;
        XMLStreamWriter writer;
        OutputStreamWriter out = null;
        final String TAG_LISTE = "Mediathek";
        final String TAG_SERVER = "Server";
        final String TAG_SERVER_URL = "URL";
        final String TAG_SERVER_DATUM = "Datum";
        final String TAG_SERVER_ZEIT = "Zeit";
        try {
            tmpFile = File.createTempFile("mediathek", null);
            tmpFile.deleteOnExit();
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(tmpFile), MSearchConst.KODIERUNG_UTF);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(TAG_LISTE);
            writer.writeCharacters("\n");//neue Zeile
            Iterator<DatenUrlFilmliste> it = listeFilmUpdateServer.iterator();
            while (it.hasNext()) {
                DatenUrlFilmliste d = it.next();
                writer.writeStartElement(TAG_SERVER);
                writer.writeCharacters("\n");
                // Tags schreiben: URL
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_URL);
                writer.writeCharacters(d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Datum
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_DATUM);
                writer.writeCharacters(d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_DATUM_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Zeit
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_ZEIT);
                writer.writeCharacters(d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_ZEIT_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(634978521, MSearchLog.FEHLER_ART_PROG, MSearchFilmlistenSuchen.class.getName(), ex, "Die URL-Filmlisten konnten nicht geschrieben werden");
        }
        return tmpFile;
    }
}
