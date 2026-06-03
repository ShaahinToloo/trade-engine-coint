package engine.cointegration.johansen;

public class JohansenResult {
    public double[] eig;          // (m x 1)
    public double[][] evec;       // (m x m)
    public double[] lr1;          // Trace
    public double[] lr2;          // Eigen
    public double[][] cvt;        // Trace Crits (m x 3) : 90% 95% 99%
    public double[][] cvm;        // Eigen Crits (m x 3) : 90% 95% 99%
    public int[] ind;
    public String meth = "johansen";
}