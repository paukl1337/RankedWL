// paul
// 2-16-24
// working on: ranked win loss counter for session

/// logic:
///     request amount of wins / losses stored into an integer
///     request data from api every 30 seconds
///     subtract wins/losses from wins/losses at start of session

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class RankedWL
{
    String winFileName, eloFileName, eloDiffFileName, avgFileName;
    PrintWriter winFile, eloDiffFile, eloFile, avgFile;
    String userInput;
    String userId;
    static int winsSes;
    static int lossesSes; // wins and losses for the session
    int winsSeason, lossSeason; // total wins and losses for the season
    int eloRank, eloRate; // elo rank and elo
    int startingElo, eloDiff; //starting elo and elo difference from starting and current elo
    int[] completions;
    int completionAvg;
    String completionOut;
    int startId; // id of first match in tree ( I can't explain )
    int toAdd;
    static GUI gui;

    public RankedWL()
    {
        winFile = eloDiffFile = eloFile = null;
        completionOut = "";
        winFileName = "WL.txt";
        avgFileName = "avgComp.txt";
        eloDiffFileName = "eloDiff.txt";
        eloFileName = "elo.txt";
        winsSes = lossesSes = winsSeason = lossSeason = eloRank = eloRate = startingElo = completionAvg = startId = - 1;
        toAdd = 0;
        completions = new int[ 100 ];
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> gui = new GUI());
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

    public void makeEloDiffFile()
    {
        File file = new File(eloDiffFileName);
        try
        {
            eloDiffFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + eloDiffFileName);
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
            System.err.println("ERROR: IOException caught! " + eloFileName);
            System.exit(1);
        }
    }

    public void makeAvgFile()
    {
        File file = new File(avgFileName);
        try
        {
            avgFile = new PrintWriter(file);
        } catch (IOException e)
        {
            System.err.println("ERROR: IOException caught! " + avgFileName);
            System.exit(1);
        }
    }

    public void doWork()
    {
        String mcIgn = userInput;
        String apiUrl = "https://mcsrranked.com/api/users/" + mcIgn;
        try
        {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200)
            {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else
            {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext())
                {
                    informationString.append(scanner.nextLine());
                }

                scanner.close();

                JSONObject jsonObject = new JSONObject(informationString.toString());
                JSONObject data = jsonObject.getJSONObject("data");
                JSONObject statistics = data.getJSONObject("statistics");
                JSONObject season = statistics.getJSONObject("season");
                JSONObject wins = season.getJSONObject("wins");
                JSONObject loses = season.getJSONObject("loses");
                if (winsSeason == - 1 && lossSeason == - 1 && startingElo == - 1)
                {
                    winsSeason = wins.getInt("ranked");
                    lossSeason = loses.getInt("ranked");
                    startingElo = data.getInt("eloRate");
                }
                eloRank = data.getInt("eloRank");
                eloRate = data.getInt("eloRate");
                winsSes = wins.getInt("ranked") - winsSeason;
                lossesSes = loses.getInt("ranked") - lossSeason;
                eloDiff = eloRate - startingElo;
                System.out.printf("WINS: %d LOSSES: %d ELO: %d RANK: %d\n",
                        winsSes,
                        lossesSes,
                        eloRate,
                        eloRank);
                makeWinFile();
                makeEloFile();
                ;
                makeEloDiffFile();
                if (eloDiff == 0)
                    eloDiffFile.println("Up - elo");
                else if (eloDiff < 0)
                {
                    System.out.printf("Down %d elo\n", Math.abs(eloDiff));
                    eloDiffFile.printf("Down %d elo", Math.abs(eloDiff));
                } else
                {
                    System.out.printf("Up %d elo\n", Math.abs(eloDiff));
                    eloDiffFile.printf("Up %d elo", Math.abs(eloDiff));
                }
                eloDiffFile.close();
                winFile.println(winsSes + "W" + " - " + lossesSes + "L");
                winFile.close();
                eloFile.println("#" + eloRank + " - " + eloRate);
                eloFile.close();
                getCompletionAvg();
            }

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // I put this in a separate method just to make it easier for me to code..
    public void getCompletionAvg()
    {
        String apiUrl = "https://mcsrranked.com/api/users/" + userInput + "/matches";
        try
        {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200)
            {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else
            {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext())
                {
                    informationString.append(scanner.nextLine());
                }

                scanner.close();

                JSONObject jsonObject = new JSONObject(informationString.toString());
                JSONArray data = jsonObject.getJSONArray("data");
                JSONObject lastMatch = data.getJSONObject(0);
                JSONObject matchResult = lastMatch.getJSONObject("result");
                if (startId == - 1)
                    startId = lastMatch.getInt("id");
                boolean forfeited = lastMatch.getBoolean("forfeited");
                String gotIT = matchResult.get("uuid") + "";
                int lastMatchId = lastMatch.getInt("id");
                if (lastMatchId != startId)
                    if (lastMatch.getInt("type") == 2 && ! forfeited)
                    {
                        if (gotIT.equals(userId))
                        {
                            completions[ toAdd ] = matchResult.getInt("time");
                            System.out.printf("Completed with a time of %02d:%02d\n",
                                    TimeUnit.MILLISECONDS.toMinutes(matchResult.getInt("time")),
                                    TimeUnit.MILLISECONDS.toSeconds(matchResult.getInt("time")) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(matchResult.getInt("time"))));
                            toAdd++;
                            startId = lastMatch.getInt("id");
                        }
                    }
                int sum = 0;
                int length = 0;
                for (int i = 0; i < completions.length; i++)
                {
                    if (completions[ i ] > 0)
                    {
                        sum += completions[ i ];
                        length++;
                    }
                }
                int average = 0;
                if (length != 0)
                    average = sum / length;
                System.out.println(String.format("Average: %02d min, %02d sec",
                        TimeUnit.MILLISECONDS.toMinutes(average),
                        TimeUnit.MILLISECONDS.toSeconds(average) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(average))
                ));
                makeAvgFile();
                if (average != 0)
                    avgFile.println(String.format("Average: %02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(average),
                            TimeUnit.MILLISECONDS.toSeconds(average) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(average))
                    ));
                else
                    avgFile.println("Average: -");
                avgFile.close();
            }
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}