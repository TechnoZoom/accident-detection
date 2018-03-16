package accidentdetection.zinka.com.truckaccidentdetection;

/**
 * Created by kapilbakshi on 16/03/18.
 */

public class ConfigResponse {

    public long getAcc_threshold() {
        return acc_threshold;
    }

    public long getBreak_threshold() {
        return break_threshold;
    }

    public long getAccident_threshold() {
        return accident_threshold;
    }

    private long acc_threshold;
    private long break_threshold;
    private long accident_threshold;
}
