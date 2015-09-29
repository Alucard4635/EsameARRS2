package dataAnalysis;

import java.io.Serializable;

public interface HomophiliaAnalysisStrategy<DataType> extends Serializable {
	public double evaluateHomophilia(DataType data,DataType data2);
}
