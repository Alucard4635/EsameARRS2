package dataAnalysis;


public class UserMapException extends Exception{
	public enum DataExceptionType {INCORRECT_FORMAT,INDEX_NOT_FOUND}
	private DataExceptionType type;
	public UserMapException(DataExceptionType type) {
		this.type = type;
	}
	public DataExceptionType getType() {
		return type;
	}
}
