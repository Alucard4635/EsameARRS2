package dataAnalysis;

public class NumberHomophilia<DataType> implements HomophiliaAnalysisStrategy<DataType> {

	/**
	 * less di value return 1
	 */
	private double perfectDifference = 5;
	/**
	 * over di value return 0
	 */
	private double worstDifference = 20;


	/* (non-Javadoc)
	 * @see dataAnalysis.HomophiliaAnalysisStrategy#evaluateHomophilia(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double evaluateHomophilia(DataType data, DataType data2) {
		if (data!=null&&data2!=null) {
			 if (data instanceof Number) {
				double i=((Number)data).doubleValue();
				double i2=((Number)data2).doubleValue();
				double diff = i-i2;
				return evaluateDifference(diff);
				
			}else{
				if (data.equals(data2)) {
					return 1;
				}
			}
		}
		return 0;
	}

	private double evaluateDifference(double diff) {
		if (diff<0) {
			diff=-diff;
		}
		if (diff<perfectDifference) {
			return 1;
		} else {
			if (diff<getWorstDifference() ) {
				return 1-(diff-perfectDifference)/worstDifference;
			}
		}
		return 0;
	}

	public double getPerfectDifference() {
		return perfectDifference;
	}

	public void setPerfectDifference(double perfectDifference) {
		this.perfectDifference = perfectDifference;
	}

	public double getWorstDifference() {
		return worstDifference;
	}

	public void setWorstDifference(double worstDifference) {
		this.worstDifference = worstDifference;
	}
	
}
