package dataAnalysis;

import java.io.Serializable;

public class AnalyzableData<DataType> implements Serializable {
	private HomophiliaAnalysisStrategy<DataType> analyzer; 
	private DataType data;
	private double weight=1;
	public AnalyzableData(DataType d,HomophiliaAnalysisStrategy<DataType> strategy) {
		data = d;
		analyzer = strategy;
	}
	public DataType getData() {
		return data;
	}
	public void setData(DataType data) {
		this.data = data;
	}
	
	public double homophiliaAnalysis(AnalyzableData<DataType> aData){
		return analyzer.evaluateHomophilia(this.data, aData.getData());
	}
	
	public double homophiliaAnalysis(DataType data){
		return analyzer.evaluateHomophilia(this.data, data);
	}
	public HomophiliaAnalysisStrategy<DataType> getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(HomophiliaAnalysisStrategy<DataType> analyzer) {
		this.analyzer = analyzer;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	@Override
	public String toString() {
		if (data!=null) {
			return data.toString();
		}else {
			return null;
		}
	}
}
