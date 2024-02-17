// paul
// 2-16-24
// working on: ranked win loss counter for session

/// logic:
///     request amount of wins / losses stored into an integer
///     request data from api every 30 seconds
///     subtract wins/losses from wins/losses at start of session
import java.io.*;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONObject;

import java.io.IOException;


public class RankedWL
{
    String winFileName;
    String lossFileName;
    String eloRankFileName;
    String eloFileName;
    PrintWriter winFile, lossFile, eloRankFile, eloFile;
    Scanner webRead;
    String userInput;
    int winsSes, lossesSes; // wins and losses for the session
    int winsSeason, lossSeason; // total wins and losses for the season
    int eloRank, eloRate; // elo rank and elo
    // vars below are for file out
    public RankedWL()
    {
        webRead = null;
        winFile = lossFile = eloRankFile = eloFile = null;
        userInput = "";
        winFileName = "wins.txt";
        lossFileName = "losses.txt";
        eloRankFileName = "eloRank.txt";
        eloFileName = "elo.txt";
        winsSes = lossesSes = winsSeason = lossSeason = eloRank = eloRate = -1;
    }

    public static void main(String[] args)
    {
        RankedWL rwl = new RankedWL();
        rwl.doIt();
    }

    public void doIt()
    {
        getUserName();
        requestData();

    }
    public void getUserName()
    {
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter the username to track! -> ");
        userInput = kb.next();
    }
    public void requestData()
    {
        Timer t = new Timer();
        doWork();
        while(true)
        {
            if(t.hasElapsed(30000, true))
                doWork();
        }

    }
    public void makeWinFile()
    {
        File file = new File(winFileName);
        try
        {
            winFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + winFileName);
            System.exit(1);
        }
    }
    public void makeLossFile()
    {
        File file = new File(lossFileName);
        try
        {
            lossFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + lossFileName);
            System.exit(1);
        }
    }
    public void makeEloFile()
    {
        File file = new File(eloFileName);
        try
        {
            eloFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + eloFile);
            System.exit(1);
        }
    }
    public void makeEloRankFile()
    {
        File file = new File(eloRankFileName);
        try
        {
            eloRankFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + eloRankFileName);
            System.exit(1);
        }
    }


    public void doWork()
    {
        String mcIgn = userInput;
        String apiUrl = "http://mcsrranked.com/api/users/" + mcIgn;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }

                scanner.close();

                JSONObject jsonObject = new JSONObject(informationString.toString());
                JSONObject data = jsonObject.getJSONObject("data");
                JSONObject statistics = data.getJSONObject("statistics");
                JSONObject season = statistics.getJSONObject("season");
                JSONObject wins = season.getJSONObject("wins");
                JSONObject loses = season.getJSONObject("loses");
                if(winsSeason == -1 && lossSeason == -1)
                {
                    winsSeason = wins.getInt("ranked");
                    lossSeason = loses.getInt("ranked");
                }
                eloRank = data.getInt("eloRank");
                eloRate = data.getInt("eloRate");
                winsSes = wins.getInt("ranked") - winsSeason;
                lossesSes = loses.getInt("ranked") - lossSeason;
                System.out.printf("WINS: %d LOSSES: %d ELO: %d RANK: %d\n",
                        winsSes,
                        lossesSes,
                        eloRate,
                        eloRank);
                makeWinFile();
                makeLossFile();
                makeEloFile();
                makeEloRankFile();
                winFile.println(winsSes + "W");
                winFile.close();
                lossFile.println(lossesSes + "L");
                lossFile.close();
                eloRankFile.println("Rank: " + eloRank);
                eloRankFile.close();
                eloFile.println("Elo: " + eloRate);
                eloFile.close();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

class Timer
{
    public long lastMS = System.currentTimeMillis();

    public void reset()
    {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasElapsed(long time, boolean doReset)
    {
        if (System.currentTimeMillis() - lastMS > time) {
            if (doReset)
                reset();


            return true;
        }
        return false;
    }


    public long getTime()
    {
        return System.currentTimeMillis() - lastMS;
    }
}