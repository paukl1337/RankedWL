// paul
// 2-16-24
// working on: ranked win loss counter for session

/// logic:
///     request amount of wins / losses stored into an integer
///     request data from api every 1 minute
///     subtract wins/losses from wins/losses at start of session
import java.util.Scanner;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class RankedWL
{
    Scanner webRead;
    String userInput;
    String wins;
    String losses;

    public RankedWL()
    {
        webRead = null;
        userInput = "";
        wins = "";
        losses = "";
    }

    public static void main(String[] args)
    {
        RankedWL rwl = new RankedWL();
        rwl.doIt();
    }

    public void doIt()
    {
        getUserName();
        doWork();

    }
    public void getUserName()
    {
        Scanner kb = new Scanner(System.in);
        userInput = kb.next();
    }
    public void requestData()
    {
        Timer t = new Timer();
        if(t.hasElapsed(30000, true))
        {
        }
    }
    public void doWork()
    {
        String mcIgn = userInput; // Replace with actual IGN
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

                String stats = String.format(
                        "stats for %s - elo: %d | rank: %d | record: %d W - %d L",
                        data.getString("nickname"),
                        data.getInt("eloRate"),
                        data.getInt("eloRank"),
                        wins.getInt("ranked"),
                        loses.getInt("ranked")
                );

                System.out.println(stats);
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