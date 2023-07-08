package service.audio;

public class ROGData {

    private int truePositive = 0;

    private int falseNegative = 0;

    private int falsePositive = 0;

    private int trueNegative = 0;

    public static String getCSVHeader() {
        return "Threshold,TPR,FPR\n";
    }


    public int getTotals() {
        return truePositive + falseNegative + falsePositive + trueNegative;
    }


    public void addTruePositive() {
        truePositive++;
    }

    public void addFalseNegative() {
        falseNegative++;
    }

    public void addFalsePositive() {
        falsePositive++;
    }

    public void addTrueNegative() {
        trueNegative++;
    }

    public double getTruePositiveRate() {
        return (double) truePositive / (double) (truePositive + falseNegative);
    }

    public double getFalsePositiveRate() {
        return (double) falsePositive / (double) (falsePositive + trueNegative);
    }

    public String toCSVRate() {
        return String.format("%.3f", getTruePositiveRate()) + ", " + String.format("%.3f", getFalsePositiveRate()) + "\n";
    }

    @Override
    public String toString() {

        return " TPR: " + getTruePositiveRate() + ", FPR: " + getFalsePositiveRate();
//        return "ROGData{" +
//                "totals=" + getTotals() +
//                ", truePositiveRate=" + getTruePositiveRate() +
//                ", falsePositiveRate=" + getFalsePositiveRate() +
//                ", truePositive=" + truePositive +
//                ", falseNegative=" + falseNegative +
//                ", falsePositive=" + falsePositive +
//                ", trueNegative=" + trueNegative +
//                '}';
    }
}
