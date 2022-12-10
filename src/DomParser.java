import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.util.concurrent.*;


public class DomParser implements Runnable {
    private DataSource dataSource;
    private static Map genres;
    public HashMap<String,Set<String>> genres_in_films;
    private Integer movieCount = 0;
    private Integer starCount = 0;
    private FileWriter txtWriter;
    private Integer workerId;

    public static ConcurrentMap<String,String> movieIds = new ConcurrentHashMap<>();
    public static ConcurrentMap<String,String> actorIds = new ConcurrentHashMap<>();
    String loginUser = "root";
    String loginPasswd = "1DrSm8939Ktf";
    String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

    private String insertMovieQuery = "INSERT INTO movies (id,title,year,director,price)" +
            " VALUES (?,?,?,?,?);";
    private String insertStarQuery = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
    private String simQry = "INSERT INTO stars_in_movies VALUES(?,?);";
    private String genreQry = "call add_genres(?,?);";
    private BufferedWriter writer;

    Document dom,dom2,dom3;

    @Override
    public void run() {
        try {
            writer = new BufferedWriter(new FileWriter("parseresult.txt", true));

        if(workerId<3){
            try {
                runParser();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }}else{
            runCastParser();
        }
        writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void runCastParser(){
        System.out.println("info:");
        System.out.println(DomParser.actorIds);
        System.out.println(movieIds);
        parseCastsFile();
        try (Connection conn=DriverManager.getConnection(loginUrl, loginUser, loginPasswd)){
            conn.setAutoCommit(false);
            PreparedStatement sim_statement = conn.prepareStatement(simQry);
            parseCast(conn,sim_statement);
            sim_statement.close();

        }catch (Exception e){
            System.out.println("Error:"+e.getMessage());
        }

    }
    public DomParser(Integer id){
        workerId = id;
    }
    private String newMovieId()
    {
        if(workerId==0) {
            return "film" + (++movieCount);
        }else{
            return "filmm"+(++movieCount);
        }
    }
    private String newStarId()
    {
        if(workerId==0) {
            return "actor" + (++starCount);
        }else{
            return "actorr"+(++starCount);
        }
    }

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    synchronized public void insertGenres(Connection conn) throws SQLException {
        for (Map.Entry<String, Set<String>> entry : genres_in_films.entrySet()) {
            String movieId = entry.getKey();
            for (String g : entry.getValue()) {
                PreparedStatement genre_statement = conn.prepareStatement(genreQry);
                genre_statement.setString(1, movieId);
                genre_statement.setString(2, g);
                //System.out.println("statement:\n"+genre_statement);
                genre_statement.executeQuery();
                genre_statement.close();
                conn.commit();


            }
        }

    }
    public void runParser() throws IOException {

        File f =new File("../","parse_result.txt");
        f.createNewFile();
        txtWriter = new FileWriter(f);
        txtWriter.write("parsing report");
        genres_in_films = new HashMap<String,Set<String>>();
        genres = new HashMap<>();
        genreMapping();

        parseXmlFile();
        parseActorsFile();
        //System.out.println(dom);
        try (Connection conn=DriverManager.getConnection(loginUrl, loginUser, loginPasswd)){
            conn.setAutoCommit(false);
            PreparedStatement mv_statement = conn.prepareStatement(insertMovieQuery);
            PreparedStatement star_statement = conn.prepareStatement(insertStarQuery);

            parseMovies(conn,mv_statement);
            parseActors(conn,star_statement);


            mv_statement.close();
            star_statement.close();
            insertGenres(conn);



        }catch (Exception e){
            System.out.println("Error:"+e.getMessage());

        }finally {


        }
        txtWriter.close();


    }
    private void parseCastsFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom3 = documentBuilder.parse("stanford-movies/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private void parseActorsFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom2 = documentBuilder.parse("stanford-movies/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("src/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }
    private void parseCast(Connection conn, PreparedStatement statement) throws SQLException {
        Element documentElement = dom3.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("filmc");
        if(nodeList!=null){
            for(int i=0;i<nodeList.getLength();i++){
                if(i%2==workerId-3){
                    continue;
                }
                Element filmc = (Element) nodeList.item(i);
                NodeList movieLists = filmc.getElementsByTagName("m");
                if(movieLists!=null){
                    for (int j = 0; j<movieLists.getLength();j++){
                        Element m = (Element) movieLists.item(j);
                        String movieId = getTextValue(m,"f");
                        String starId = null;
                        if(movieId==null){
                            continue;
                        }
                        String starName = getTextValue(m,"a");
                        if(starName==null){
                            continue;
                        }
                       // System.out.println(movieId+": "+starName);
                        if(movieIds.containsKey(movieId) && actorIds.containsKey(starName)){
                            movieId = movieIds.get(movieId);
                            starId = actorIds.get(starName);
                            //System.out.println(movieId+":"+starId);
                            //System.out.println("-------");
                            statement.setString(1,starId);
                            statement.setString(2, movieId);
                            statement.addBatch();



                        }

                    }
                }


            }
            statement.executeBatch();
            conn.commit();
        }

    }
    private void parseMovies(Connection conn, PreparedStatement statement) throws IOException, SQLException {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        System.out.println(nodeList.getLength());
        if (nodeList != null) {
            int count = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if(i%2==workerId){
                    continue;
                }

                count += 1;
                Element director = (Element) nodeList.item(i);
                String dirid = getTextValue(director, "dirid");
                String dirname = getTextValue(director, "dirname");
                if(dirname==null){
                    continue;
                }

                //System.out.println(dirid+" "+dirname);
                NodeList movieList = director.getElementsByTagName("film");
                if(movieList!=null){
                    for(int j =0; j<movieList.getLength();j++){
                        Element film = (Element) movieList.item(j);
                        String title = getTextValue(film,"t");
                        if((title=="") ||title==null){
                            continue;
                        }
                        //System.out.println(title);

                        Set<String> genre_set = new HashSet<String>();
                        txtWriter.append("title:"+title+"\n");
                        Integer year = getIntValue(film,"year");
                        if(year==-1){
                            String yearString = getTextValue(film,"year");
                            if(yearString!="" && yearString!=null){
                           writer.append("ilegel year value for movie" +title+": "+yearString+"\n");
                            }else{
                                writer.append("missing year value for movie "+title+"\n");
                            }
                        }
                        NodeList genreList = film.getElementsByTagName("cat");

                        if(genreList!=null) {
                            txtWriter.append("genres:\n");
                            for (int g = 0; g<genreList.getLength();g++){
                                Element cat = (Element) genreList.item(g);
                                if(cat.getFirstChild()!=null) {
                                    String genre = cat.getFirstChild().getNodeValue();
                                    if (genre==null){
                                        continue;
                                    }
                                    if (genres.containsKey(genre.trim())){
                                        genre =(String)genres.get(genre.trim());
                                        for(int z = 0; z<genre.split(",").length;z++){
                                            genre_set.add(genre.split(",")[z]);
                                        }

                                    }else{
                                        //System.out.print(genre + ", ");
                                        writer.append("invalid genre name: "+genre+"\n");
                                    }

                                    txtWriter.append(genre+",");

                                }
                            }
                            txtWriter.append("");

                        }
                        String movie_id = newMovieId();
                        //System.out.println();
                        genres_in_films.put(movie_id,genre_set);
                        String xmlid = getTextValue(film,"fid");
                        if(xmlid!=null){
                            DomParser.movieIds.putIfAbsent(xmlid,movie_id);

                        }


                        /**
                         System.out.println("parser#"+workerId);
                         System.out.println(title);
                         System.out.println(year);
                         System.out.println(genre_set);
                         System.out.println(dirname);
                         **/
                        statement.setString(1,movie_id);
                        statement.setString(2, title);
                        statement.setInt(3,year);
                        statement.setString(4,dirname);
                        statement.setBigDecimal(5, BigDecimal.valueOf(5.0));

                        statement.addBatch();

                        //System.out.println("-------------");


                    }

                }
                //getFilms(director);
            }
            statement.executeBatch();
            conn.commit();
            System.out.println();
            System.out.println(count);


        }
    }
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            if (nodeList.item(0).getFirstChild()==null){
                return "";
            }
            textVal = nodeList.item(0).getFirstChild().getNodeValue();

        }
        return textVal;
    }
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        try{
            return Integer.parseInt(getTextValue(ele, tagName));}
        catch (NumberFormatException e){
            return -1;
        }
    }
    private void parseActors(Connection conn,PreparedStatement statement) throws IOException, SQLException {
        Set<String> actors = new HashSet<>();
        // get the document root Element
        Element documentElement = dom2.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        System.out.println(nodeList.getLength());
        if (nodeList != null) {
            int count = 0;
            for (int i = 0; i < nodeList.getLength(); i++) {
                if(i%2==workerId){
                    continue;
                }

                count += 1;
                Element element = (Element) nodeList.item(i);
                String actName = getTextValue(element,"stagename");
                if(actName==null){
                    continue;
                }
                Integer dob = getIntValue(element, "dob");
                if(actorIds.containsKey(actName)){
                    System.out.println("deplicated actor name:"+actName);
                    continue;
                }
                //actors.add(actName);
                String actor_id = newStarId();
                statement.setString(1,actor_id);
                statement.setString(2,actName);
                if(dob==-1){
                    statement.setNull(3, Types.INTEGER);
                }else{
                    statement.setInt(3,dob);
                }
                statement.addBatch();
                actorIds.putIfAbsent(actName,actor_id);
                //actors_in_films.put(actName,dob);
                /**
                 System.out.println(actName);
                 System.out.println(dob);
                 System.out.println("-------------");
                 **/

                //getFilms(director);
            }
            statement.executeBatch();
            conn.commit();
            System.out.println();
            System.out.println("Count: " + count);


        }

    }
    private void genreMapping() {
        genres.put("Dram","Drama");
        genres.put("DraM","Drama");
        genres.put("DRam","Drama");
        genres.put("dram","Drama");
        genres.put("Drama","Drama");
        genres.put("Draam","Drama");
        genres.put("Susp","Suspense");
        genres.put("susp","Suspense");
        genres.put("Epic","Epic");
        genres.put("Fant","Fantasy");
        genres.put("ScFi","Sci-Fi");
        genres.put("SciF","Sci-Fi");
        genres.put("BioP","Biography");
        genres.put("Biop","Biography");
        genres.put("BiopP","Biography");
        genres.put("Musc","Music");
        genres.put("stage musical","Musical");
        genres.put("Advt","Adventure");
        genres.put("Horr","Horror");
        genres.put("Hor","Horror");
        genres.put("Romt","Romance");
        genres.put("Docu","Documentary");
        genres.put("Comd","Comedy");
        genres.put("Actn","Action");
        genres.put("Faml","Family");
        genres.put("Hist","History");
        genres.put("Cart","Animation");
        genres.put("Romt Comd","Romantic,Comedy");
        genres.put("West","Western");
        genres.put("Noir","Noir");
        genres.put("Porn","Pornography");
        genres.put("porn","Pornography");
        genres.put("Myst","Mystery");
        genres.put("surreal","Surreal");
        genres.put("Surr","Surreal");
        genres.put("Scat","Scat");
        genres.put("CnRb","CnRb");
        genres.put("disa","Disaster");
        genres.put("Disa","Disaster");
        genres.put("Crim","Crime");
        genres.put("CmR","Crime");
        genres.put("Noir Comd","Noir,Comedy");
        genres.put("Adct","Adventure");
        genres.put("Bio","Biography");
        genres.put("BioB","Biography");
        genres.put("BioG","Biography");
        genres.put("BioPP","Biography");
        genres.put("BioPx","Biography");
        genres.put("Noir Comd Romt","Noir,Comedy,Romantic");
        genres.put("Muusc","Musical");
        genres.put("Scfi","Sci-Fi");
        genres.put("West1","Western");
        genres.put("Dram Docu","Drama");
        genres.put("Dram.Actn","Drama,Action");
        genres.put("Act", "Action");
        genres.put("AvGa", "Avant-Garde");
        genres.put("Avant Garde", "Avant-Garde");
        genres.put("Axtn", "Action");
        genres.put("Comd Noir", "Comedy,Noir");
        genres.put("Comd West", "Comedy,Western");
        genres.put("Comdx", "Comedy");
        genres.put("Cult", "Cult");
        genres.put("DRAM", "Drama");
        genres.put("Adctx", "Adventure");
        genres.put("Allegory", "Allegory");
        genres.put("CA", "Camp");
        genres.put("Camp", "Camp");
        genres.put("Docu Dram", "Documentary,Drama");
        genres.put("Dram>", "Documentary");
        genres.put("Dramd", "Drama");
        genres.put("Dramn", "Drama");
        genres.put("Duco", "Documentary,Comedy");
        genres.put("Ducu", "Documentary,Cult");
        genres.put("Kinky", "Kinky");
        genres.put("Muscl", "Musical");
        genres.put("Porb", "Pornography");
        genres.put("Psyc", "Psychological");
        genres.put("Psych", "Psychological");
        genres.put("Dram", "Drama");
        genres.put("Road", "Romance,Adventure");
        genres.put("Romt Actn", "Romance,Action");
        genres.put("Romt Dram", "Romance,Drama");
        genres.put("Romt Fant", "Romance,Fantasy");
        genres.put("Romt. Comd", "Romance,Comedy");
        genres.put("RomtAdvt", "Romance,Adventure");
        genres.put("Romtx", "Romance");
        genres.put("S.F.", "Sci-Fi");
        genres.put("Surl", "Surreal");
        genres.put("SxFi", "Sci-Fi");
        genres.put("TV", "Reality-TV");
        genres.put("TVmini", "Reality-TV");
        genres.put("Weird", "Weird");
        genres.put("Viol", "Violent");
        genres.put("actn", "Action");
        genres.put("comd", "Comedy");
        genres.put("fant", "Fantasy");
        genres.put("musc", "Music");
        genres.put("noir", "Noir");
        genres.put("romt", "Romance");
        genres.put("verite", "Verite");
        genres.put("Psych Dram", "Psychology,Drama");
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        // create an instance
        DomParser domParser0 = new DomParser(0);
        DomParser domParser1 = new DomParser(1);

        // call run example
        Thread thread0 = new Thread(domParser0);
        Thread thread1 = new Thread(domParser1);
        thread0.start();
        thread1.start();
        //waiting for both threads to finish
        thread0.join();
        thread1.join();

        DomParser castParser = new DomParser(3);
        DomParser castParser2 = new DomParser(4);
        Thread castThread = new Thread(castParser);
        Thread castThread2 = new Thread(castParser2);
        castThread.start();
        castThread2.start();
        //System.out.println("joined, concureent");


    }


}