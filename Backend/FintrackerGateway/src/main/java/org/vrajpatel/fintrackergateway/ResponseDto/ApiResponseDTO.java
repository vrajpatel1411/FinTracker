package org.vrajpatel.fintrackergateway.ResponseDto;


public class ApiResponseDTO<T> {

    private boolean success;
    private String message;

    private T data;



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "ApiResponseDTO{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", data=" + data +
                '}';
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean valid) {
        this.success = valid;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data=data;
    }
}
