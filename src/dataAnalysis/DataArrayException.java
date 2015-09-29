package dataAnalysis;


public class DataArrayException extends Exception{
	public enum DataExceptionType {INCORRECT_FORMAT,INDEX_NOT_FOUND}
	private DataExceptionType type;
	public DataArrayException(DataExceptionType type) {
		this.type = type;
	}
	public DataExceptionType getType() {
		return type;
	}
}
