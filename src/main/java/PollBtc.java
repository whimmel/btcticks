import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by whimmelstoss on 11/9/16.
 */
public class PollBtc {

    private Jedis redis;
    private Timer timer;
    private static String url = "https://btc-e.com/api/3/ticker/btc_usd-btc_rur-btc_eur-ltc_btc-ltc_usd-ltc_rur";
    CloseableHttpClient httpclient;

    public PollBtc(int seconds) {
        redis = new Jedis("db.stagingcharts.markettraders.com");

        httpclient = HttpClients.createDefault();

        timer = new Timer();
        timer.schedule(new PollTask(), 0, seconds*1000);
    }

    // {"btc_usd":{"high":730,"low":695.745,"avg":712.8725,"vol":4246706.0131,"vol_cur":5943.8896,"last":710.974,"buy":711.256,"sell":710.975,"updated":1478728471},
    //  "btc_rur":{"high":44887.99995,"low":42660.04037,"avg":43774.02016,"vol":14322400.40422,"vol_cur":325.50531,"last":43596.04046,"buy":43843.35,"sell":43450.52758,"updated":1478728471},
    //  "btc_eur":{"high":668,"low":644,"avg":656,"vol":51879.0891,"vol_cur":79.0063,"last":658.74358,"buy":658.74358,"sell":654.27743,"updated":1478728471},
    //  "ltc_btc":{"high":0.00543,"low":0.00529,"avg":0.00536,"vol":202.56812,"vol_cur":37871.40106,"last":0.00535,"buy":0.00536,"sell":0.00535,"updated":1478728471},
    //  "ltc_usd":{"high":3.89,"low":3.4851,"avg":3.68755,"vol":147082.18451,"vol_cur":39458.03899,"last":3.809,"buy":3.809,"sell":3.79224,"updated":1478728471},
    //  "ltc_rur":{"high":239.68,"low":230.3,"avg":234.99,"vol":211365.9923,"vol_cur":897.26854,"last":232.22116,"buy":234.46996,"sell":232.22115,"updated":1478728471}}


    public class PriceData {
        public double high;
        public double low;
        public double avg;
        public double vol;
        public double vol_cur;
        public double last;
        public double buy;
        public double sell;
        public long updated;
    }

    public class Tick {
        public PriceData btc_usd;
        public PriceData btc_rur;
        public PriceData btc_eur;
        public PriceData ltc_btc;
        public PriceData ltc_usd;
        public PriceData ltc_rur;
    }

    class PollTask extends TimerTask {
        public void run() {
            String body = "";

            // get data from btce
            try {
                HttpGet httpGet = new HttpGet(url);

                HttpResponse response = httpclient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();

                //get response as String or what ever way you need
                body = EntityUtils.toString(httpEntity);

                // deserialize json
                Tick tick = new ObjectMapper().readValue(body, Tick.class);

                System.out.printf("Symbol BTC/USD Last {1} {2} \n", tick.btc_usd.last, new Date(tick.btc_usd.updated).toString());
            } catch (IOException e) {
                System.out.println(e.toString());
            }



            // print out the ticks

            // send to redis
        }
    }

    public static void main(String args[]) {
        new PollBtc(2);
        System.out.format("Task scheduled.%n");
    }

}
