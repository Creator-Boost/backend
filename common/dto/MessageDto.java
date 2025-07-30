package dto;


public class MessageDto {
    private String message;
    private String timestamp;
    private String serviceFrom;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getServiceFrom() {
        return serviceFrom;
    }
    public void setServiceFrom(String serviceFrom) {
        this.serviceFrom = serviceFrom;
    }



    public MessageDto() {}




    public MessageDto(String message, String timestamp, String serviceFrom) {
        this.message = message;
        this.timestamp = timestamp;
        this.serviceFrom = serviceFrom;
    }



    @Override
    public String toString() {
        return "MessageDto{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", serviceFrom='" + serviceFrom + '\'' +
                '}';
    }
}