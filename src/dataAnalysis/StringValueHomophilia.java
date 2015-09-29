package dataAnalysis;

public class StringValueHomophilia<DataType> implements HomophiliaAnalysisStrategy<DataType> {

	@Override
	public double evaluateHomophilia(DataType data, DataType data2) {
		if (data instanceof String) {
			String dataString=((String) data).toLowerCase();
			String data2String = ((String) data2).toLowerCase();
			if (dataString.contains(data2String)) {
				return 1;
			}else {
				if (data2String.contains(dataString)) {
					return 1;
				}
			}
		}
		return 0;
	}

}
